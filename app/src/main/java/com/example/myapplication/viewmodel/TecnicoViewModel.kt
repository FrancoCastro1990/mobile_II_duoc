package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.NetworkResult
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.dto.TecnicoDto
import com.example.myapplication.data.repository.TecnicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TecnicoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TecnicoViewModel"
        private const val TAG_PERF = "SolicitudPerformance"
        private const val TAG_MEMORY = "SolicitudMemory"
    }

    private val repository = TecnicoRepository(RetrofitClient.apiService)

    private val _tecnicosState = MutableStateFlow<NetworkResult<List<TecnicoDto>>>(NetworkResult.Loading)
    val tecnicosState: StateFlow<NetworkResult<List<TecnicoDto>>> = _tecnicosState.asStateFlow()

    init {
        Log.d(TAG, "TecnicoViewModel inicializado")
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        Log.i(TAG_MEMORY, "Memoria al iniciar TecnicoViewModel: ${usedMemory}MB")
    }

    fun loadTecnicos(tipoServicio: String) {
        Log.d(TAG, "Cargando tecnicos para: $tipoServicio")
        val startTime = System.currentTimeMillis()

        _tecnicosState.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = repository.getTecnicosByTipo(tipoServicio)
            _tecnicosState.value = result

            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG_PERF, "loadTecnicos completado en ${elapsed}ms para: $tipoServicio")
        }
    }

    fun reloadTecnicos(tipoServicio: String) {
        Log.d(TAG, "Recargando tecnicos para: $tipoServicio")
        loadTecnicos(tipoServicio)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG_MEMORY, "TecnicoViewModel onCleared - viewModelScope cancelado automaticamente")
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        Log.i(TAG_MEMORY, "Memoria al limpiar TecnicoViewModel: ${usedMemory}MB / ${maxMemory}MB")
    }
}
