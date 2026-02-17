package com.example.myapplication.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.SolicitudEntity
import com.example.myapplication.data.repository.SolicitudRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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

    companion object {
        private const val TAG = "SolicitudViewModel"
        private const val TAG_CRUD = "SolicitudCRUD"
        private const val TAG_PERF = "SolicitudPerformance"
        private const val TAG_VALIDATION = "SolicitudValidation"
        private const val TAG_MEMORY = "SolicitudMemory"
    }

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
        Log.d(TAG, "Inicializando ViewModel...")
        val startTime = System.currentTimeMillis()

        val database = AppDatabase.getDatabase(application)
        repository = SolicitudRepository(database.solicitudDao())
        solicitudes = repository.allSolicitudes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val initTime = System.currentTimeMillis() - startTime
        Log.i(TAG_PERF, "ViewModel inicializado en ${initTime}ms")
        Log.d(TAG, "Base de datos y repositorio configurados correctamente")
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
        Log.d(TAG_CRUD, "Cargando solicitud para edición - ID: $id")
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val solicitud = repository.getSolicitudById(id)
                if (solicitud != null) {
                    _formState.value = FormState(
                        tipoServicio = solicitud.tipoServicio,
                        descripcion = solicitud.descripcion,
                        nombreCliente = solicitud.nombreCliente,
                        telefono = solicitud.telefono,
                        direccion = solicitud.direccion,
                        isEditing = true,
                        editingId = solicitud.id
                    )
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.i(TAG_PERF, "Solicitud cargada para edición en ${loadTime}ms - ID: $id")
                    Log.d(TAG_CRUD, "Datos cargados: cliente=${solicitud.nombreCliente}, tipo=${solicitud.tipoServicio}")
                } else {
                    Log.w(TAG_CRUD, "Solicitud no encontrada para edición - ID: $id")
                }
            } catch (e: SQLiteException) {
                Log.e(TAG_CRUD, "Error de base de datos al cargar solicitud ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Error de base de datos: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG_CRUD, "Error inesperado al cargar solicitud ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Error al cargar solicitud"))
            }
        }
    }

    fun loadSolicitudDetail(id: Long) {
        Log.d(TAG_CRUD, "Cargando detalle de solicitud - ID: $id")
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val solicitud = repository.getSolicitudById(id)
                _selectedSolicitud.value = solicitud

                val loadTime = System.currentTimeMillis() - startTime
                if (solicitud != null) {
                    Log.i(TAG_PERF, "Detalle cargado en ${loadTime}ms - ID: $id")
                    Log.d(TAG_CRUD, "Detalle: estado=${solicitud.estado}, cliente=${solicitud.nombreCliente}")
                } else {
                    Log.w(TAG_CRUD, "Solicitud no encontrada - ID: $id (tiempo: ${loadTime}ms)")
                }
            } catch (e: SQLiteException) {
                Log.e(TAG_CRUD, "Error de base de datos al cargar detalle - ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Error al cargar detalle: problema en base de datos"))
            } catch (e: Exception) {
                Log.e(TAG_CRUD, "Error inesperado al cargar detalle - ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Error al cargar detalle de solicitud"))
            }
        }
    }

    fun clearForm() {
        _formState.value = FormState()
    }

    fun saveSolicitud() {
        val state = _formState.value
        val operationType = if (state.isEditing) "ACTUALIZAR" else "CREAR"

        Log.d(TAG_CRUD, "═══════════════════════════════════════════════════")
        Log.d(TAG_CRUD, "Iniciando operación: $operationType solicitud")
        Log.d(TAG_CRUD, "Datos del formulario:")
        Log.d(TAG_CRUD, "  - Tipo servicio: '${state.tipoServicio}'")
        Log.d(TAG_CRUD, "  - Cliente: '${state.nombreCliente}'")
        Log.d(TAG_CRUD, "  - Teléfono: '${state.telefono}'")
        Log.d(TAG_CRUD, "  - Dirección: '${state.direccion}'")
        Log.d(TAG_CRUD, "  - Descripción: '${state.descripcion.take(50)}...'")
        if (state.isEditing) {
            Log.d(TAG_CRUD, "  - ID a editar: ${state.editingId}")
        }

        // Validacion sincrona con logging detallado
        val camposVacios = mutableListOf<String>()
        if (state.tipoServicio.isBlank()) camposVacios.add("tipoServicio")
        if (state.nombreCliente.isBlank()) camposVacios.add("nombreCliente")
        if (state.telefono.isBlank()) camposVacios.add("telefono")
        if (state.direccion.isBlank()) camposVacios.add("direccion")

        if (camposVacios.isNotEmpty()) {
            Log.w(TAG_VALIDATION, "Validación fallida - Campos vacíos: ${camposVacios.joinToString(", ")}")
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Por favor complete todos los campos obligatorios"))
            }
            return
        }
        Log.i(TAG_VALIDATION, "Validación exitosa - Todos los campos requeridos están completos")

        // Prevenir multiples ejecuciones
        if (_isSaving.value) {
            Log.w(TAG_CRUD, "Operación de guardado ya en progreso - Ignorando solicitud duplicada")
            return
        }

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _isSaving.value = true
            Log.d(TAG_CRUD, "Estado de guardado activado - Bloqueando UI")

            try {
                withContext(Dispatchers.IO) {
                    val dbStartTime = System.currentTimeMillis()

                    if (state.isEditing && state.editingId != null) {
                        Log.d(TAG_CRUD, "Modo EDICIÓN - Buscando solicitud existente ID: ${state.editingId}")

                        val existingSolicitud = repository.getSolicitudById(state.editingId)
                        if (existingSolicitud == null) {
                            Log.e(TAG_CRUD, "ERROR: Solicitud a editar no encontrada - ID: ${state.editingId}")
                            throw IllegalStateException("La solicitud a editar (ID: ${state.editingId}) no existe en la base de datos")
                        }

                        Log.d(TAG_CRUD, "Solicitud encontrada - Preparando actualización")
                        val updatedSolicitud = SolicitudEntity(
                            id = state.editingId,
                            tipoServicio = state.tipoServicio,
                            descripcion = state.descripcion,
                            nombreCliente = state.nombreCliente,
                            telefono = state.telefono,
                            direccion = state.direccion,
                            fechaSolicitud = existingSolicitud.fechaSolicitud,
                            estado = existingSolicitud.estado
                        )
                        repository.updateSolicitud(updatedSolicitud)

                        val dbTime = System.currentTimeMillis() - dbStartTime
                        Log.i(TAG_PERF, "UPDATE completado en ${dbTime}ms - ID: ${state.editingId}")
                    } else {
                        Log.d(TAG_CRUD, "Modo CREACIÓN - Insertando nueva solicitud")

                        val newSolicitud = SolicitudEntity(
                            tipoServicio = state.tipoServicio,
                            descripcion = state.descripcion,
                            nombreCliente = state.nombreCliente,
                            telefono = state.telefono,
                            direccion = state.direccion
                        )
                        val newId = repository.insertSolicitud(newSolicitud)

                        val dbTime = System.currentTimeMillis() - dbStartTime
                        Log.i(TAG_PERF, "INSERT completado en ${dbTime}ms - Nuevo ID: $newId")
                        Log.d(TAG_CRUD, "Nueva solicitud creada exitosamente - ID: $newId")
                    }
                }

                val totalTime = System.currentTimeMillis() - startTime
                Log.i(TAG_PERF, "═══ Operación $operationType completada en ${totalTime}ms ═══")

                val message = if (state.isEditing) "Solicitud actualizada correctamente" else "Solicitud creada correctamente"
                _uiEvent.emit(UiEvent.ShowToast(message))
                clearForm()
                Log.d(TAG_CRUD, "Formulario limpiado - Navegando hacia atrás")
                _uiEvent.emit(UiEvent.NavigateBack)

            } catch (e: SQLiteException) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG_CRUD, "═══ ERROR DE BASE DE DATOS después de ${errorTime}ms ═══")
                Log.e(TAG_CRUD, "SQLiteException en operación $operationType: ${e.message}", e)
                Log.e(TAG_CRUD, "Posible causa: Corrupción de datos, espacio insuficiente o restricción de integridad")
                _uiEvent.emit(UiEvent.ShowToast("Error de base de datos: No se pudo guardar. Verifique el espacio disponible."))

            } catch (e: IllegalStateException) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG_CRUD, "═══ ERROR DE ESTADO ILEGAL después de ${errorTime}ms ═══")
                Log.e(TAG_CRUD, "IllegalStateException: ${e.message}", e)
                _uiEvent.emit(UiEvent.ShowToast("Error: ${e.message}"))

            } catch (e: IOException) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG_CRUD, "═══ ERROR DE I/O después de ${errorTime}ms ═══")
                Log.e(TAG_CRUD, "IOException en operación $operationType: ${e.message}", e)
                Log.e(TAG_CRUD, "Posible causa: Problema de almacenamiento o permisos")
                _uiEvent.emit(UiEvent.ShowToast("Error de almacenamiento: Verifique los permisos de la aplicación."))

            } catch (e: Exception) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG_CRUD, "═══ ERROR INESPERADO después de ${errorTime}ms ═══")
                Log.e(TAG_CRUD, "Exception tipo ${e.javaClass.simpleName}: ${e.message}", e)
                Log.e(TAG_CRUD, "StackTrace:", e)
                _uiEvent.emit(UiEvent.ShowToast("Error inesperado al guardar: ${e.localizedMessage ?: "Error desconocido"}"))

            } finally {
                _isSaving.value = false
                Log.d(TAG_CRUD, "Estado de guardado desactivado - UI desbloqueada")
                Log.d(TAG_CRUD, "═══════════════════════════════════════════════════")
            }
        }
    }

    fun updateEstado(id: Long, nuevoEstado: String) {
        Log.d(TAG_CRUD, "───────────────────────────────────────────────────")
        Log.d(TAG_CRUD, "Actualizando estado de solicitud")
        Log.d(TAG_CRUD, "  - ID: $id")
        Log.d(TAG_CRUD, "  - Nuevo estado: $nuevoEstado")

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val dbStartTime = System.currentTimeMillis()
                    repository.updateEstado(id, nuevoEstado)
                    val dbTime = System.currentTimeMillis() - dbStartTime
                    Log.i(TAG_PERF, "UPDATE estado completado en ${dbTime}ms")
                }

                loadSolicitudDetail(id)

                val totalTime = System.currentTimeMillis() - startTime
                Log.i(TAG_PERF, "Actualización de estado total: ${totalTime}ms")
                Log.i(TAG_CRUD, "Estado actualizado exitosamente - ID: $id → $nuevoEstado")
                _uiEvent.emit(UiEvent.ShowToast("Estado actualizado a: $nuevoEstado"))

            } catch (e: SQLiteException) {
                Log.e(TAG_CRUD, "Error de BD al actualizar estado - ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Error de base de datos al actualizar estado"))

            } catch (e: IllegalArgumentException) {
                Log.e(TAG_CRUD, "Estado inválido: '$nuevoEstado' - ID: $id", e)
                _uiEvent.emit(UiEvent.ShowToast("Estado no válido: $nuevoEstado"))

            } catch (e: Exception) {
                Log.e(TAG_CRUD, "Error inesperado al actualizar estado - ID: $id", e)
                Log.e(TAG_CRUD, "Tipo: ${e.javaClass.simpleName}, Mensaje: ${e.message}")
                _uiEvent.emit(UiEvent.ShowToast("Error al actualizar estado: ${e.localizedMessage ?: "Error desconocido"}"))

            } finally {
                Log.d(TAG_CRUD, "───────────────────────────────────────────────────")
            }
        }
    }

    /**
     * Limpieza del ViewModel cuando se destruye.
     * viewModelScope se cancela automaticamente aqui (buena practica vs GlobalScope).
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG_MEMORY, "ViewModel onCleared - viewModelScope cancelado automaticamente")
        Log.d(TAG_MEMORY, "Coroutines activas finalizadas correctamente (viewModelScope)")
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        Log.i(TAG_MEMORY, "Memoria al limpiar ViewModel: ${usedMemory}MB / ${maxMemory}MB")
    }

    fun deleteSolicitud(id: Long) {
        Log.d(TAG_CRUD, "───────────────────────────────────────────────────")
        Log.d(TAG_CRUD, "Iniciando eliminación de solicitud - ID: $id")

        // Prevenir multiples ejecuciones
        if (_isDeleting.value) {
            Log.w(TAG_CRUD, "Operación de eliminación ya en progreso - Ignorando solicitud duplicada")
            return
        }

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _isDeleting.value = true
            Log.d(TAG_CRUD, "Estado de eliminación activado - Bloqueando UI")

            try {
                withContext(Dispatchers.IO) {
                    val dbStartTime = System.currentTimeMillis()
                    repository.deleteSolicitudById(id)
                    val dbTime = System.currentTimeMillis() - dbStartTime
                    Log.i(TAG_PERF, "DELETE completado en ${dbTime}ms - ID: $id")
                }

                val totalTime = System.currentTimeMillis() - startTime
                Log.i(TAG_PERF, "Eliminación total completada en ${totalTime}ms")
                Log.i(TAG_CRUD, "Solicitud eliminada exitosamente - ID: $id")

                _uiEvent.emit(UiEvent.ShowToast("Solicitud eliminada"))
                _uiEvent.emit(UiEvent.NavigateBack)

            } catch (e: SQLiteException) {
                Log.e(TAG_CRUD, "Error de BD al eliminar solicitud - ID: $id", e)
                Log.e(TAG_CRUD, "Posible causa: Restricción de integridad o base de datos bloqueada")
                _uiEvent.emit(UiEvent.ShowToast("Error de base de datos al eliminar"))

            } catch (e: Exception) {
                Log.e(TAG_CRUD, "Error inesperado al eliminar solicitud - ID: $id", e)
                Log.e(TAG_CRUD, "Tipo: ${e.javaClass.simpleName}, Mensaje: ${e.message}")
                _uiEvent.emit(UiEvent.ShowToast("Error al eliminar: ${e.localizedMessage ?: "Error desconocido"}"))

            } finally {
                _isDeleting.value = false
                Log.d(TAG_CRUD, "Estado de eliminación desactivado - UI desbloqueada")
                Log.d(TAG_CRUD, "───────────────────────────────────────────────────")
            }
        }
    }
}
