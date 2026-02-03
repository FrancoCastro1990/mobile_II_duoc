package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.SolicitudEntity
import com.example.myapplication.data.repository.SolicitudRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FormState(
    val tipoServicio: String = "",
    val descripcion: String = "",
    val nombreCliente: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class NavigateToDetail(val id: Long) : UiEvent()
}

class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SolicitudRepository

    val solicitudes: StateFlow<List<SolicitudEntity>>

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _selectedSolicitud = MutableStateFlow<SolicitudEntity?>(null)
    val selectedSolicitud: StateFlow<SolicitudEntity?> = _selectedSolicitud.asStateFlow()

    // Estados de carga para UI
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SolicitudRepository(database.solicitudDao())
        solicitudes = repository.allSolicitudes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateTipoServicio(value: String) {
        _formState.value = _formState.value.copy(tipoServicio = value)
    }

    fun updateDescripcion(value: String) {
        _formState.value = _formState.value.copy(descripcion = value)
    }

    fun updateNombreCliente(value: String) {
        _formState.value = _formState.value.copy(nombreCliente = value)
    }

    fun updateTelefono(value: String) {
        _formState.value = _formState.value.copy(telefono = value)
    }

    fun updateDireccion(value: String) {
        _formState.value = _formState.value.copy(direccion = value)
    }

    fun loadSolicitudForEditing(id: Long) {
        viewModelScope.launch {
            repository.getSolicitudById(id)?.let { solicitud ->
                _formState.value = FormState(
                    tipoServicio = solicitud.tipoServicio,
                    descripcion = solicitud.descripcion,
                    nombreCliente = solicitud.nombreCliente,
                    telefono = solicitud.telefono,
                    direccion = solicitud.direccion,
                    isEditing = true,
                    editingId = solicitud.id
                )
            }
        }
    }

    fun loadSolicitudDetail(id: Long) {
        viewModelScope.launch {
            _selectedSolicitud.value = repository.getSolicitudById(id)
        }
    }

    fun clearForm() {
        _formState.value = FormState()
    }

    fun saveSolicitud() {
        val state = _formState.value

        // Validacion sincrona
        if (state.tipoServicio.isBlank() || state.nombreCliente.isBlank() ||
            state.telefono.isBlank() || state.direccion.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Por favor complete todos los campos obligatorios"))
            }
            return
        }

        // Prevenir multiples ejecuciones
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                // TODO: Remover delay - solo para demostrar el indicador de carga
                delay(4000)
                withContext(Dispatchers.IO) {
                    if (state.isEditing && state.editingId != null) {
                        val existingSolicitud = repository.getSolicitudById(state.editingId)
                        val updatedSolicitud = SolicitudEntity(
                            id = state.editingId,
                            tipoServicio = state.tipoServicio,
                            descripcion = state.descripcion,
                            nombreCliente = state.nombreCliente,
                            telefono = state.telefono,
                            direccion = state.direccion,
                            fechaSolicitud = existingSolicitud?.fechaSolicitud ?: System.currentTimeMillis(),
                            estado = existingSolicitud?.estado ?: "pendiente"
                        )
                        repository.updateSolicitud(updatedSolicitud)
                    } else {
                        val newSolicitud = SolicitudEntity(
                            tipoServicio = state.tipoServicio,
                            descripcion = state.descripcion,
                            nombreCliente = state.nombreCliente,
                            telefono = state.telefono,
                            direccion = state.direccion
                        )
                        repository.insertSolicitud(newSolicitud)
                    }
                }
                val message = if (state.isEditing) "Solicitud actualizada correctamente" else "Solicitud creada correctamente"
                _uiEvent.emit(UiEvent.ShowToast(message))
                clearForm()
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Error al guardar: ${e.localizedMessage}"))
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateEstado(id: Long, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateEstado(id, nuevoEstado)
                }
                loadSolicitudDetail(id)
                _uiEvent.emit(UiEvent.ShowToast("Estado actualizado a: $nuevoEstado"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Error al actualizar estado"))
            }
        }
    }

    fun deleteSolicitud(id: Long) {
        // Prevenir multiples ejecuciones
        if (_isDeleting.value) return

        viewModelScope.launch {
            _isDeleting.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteSolicitudById(id)
                }
                _uiEvent.emit(UiEvent.ShowToast("Solicitud eliminada"))
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Error al eliminar"))
            } finally {
                _isDeleting.value = false
            }
        }
    }
}
