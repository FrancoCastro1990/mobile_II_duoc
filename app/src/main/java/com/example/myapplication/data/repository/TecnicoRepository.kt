package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.data.remote.NetworkResult
import com.example.myapplication.data.remote.dto.TecnicoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class TecnicoRepository(private val apiService: ApiService) {

    companion object {
        private const val TAG = "SolicitudAPI"
        private const val TAG_PERF = "SolicitudPerformance"
    }

    suspend fun getTecnicosByTipo(tipo: String): NetworkResult<List<TecnicoDto>> {
        Log.d(TAG, "═══════════════════════════════════════════════════")
        Log.d(TAG, "Iniciando consulta API - Tipo: $tipo")
        val startTime = System.currentTimeMillis()

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Ejecutando request en Dispatchers.IO")
                val response = apiService.getTecnicosByTipo(tipo)
                val elapsed = System.currentTimeMillis() - startTime

                if (response.success) {
                    Log.i(TAG_PERF, "API call exitosa en ${elapsed}ms - ${response.count} tecnicos encontrados")
                    Log.d(TAG, "Tecnicos recibidos para '$tipo': ${response.data.map { it.nombre }}")
                    Log.d(TAG, "═══════════════════════════════════════════════════")
                    NetworkResult.Success(response.data)
                } else {
                    Log.w(TAG, "API respondio con success=false en ${elapsed}ms")
                    Log.d(TAG, "═══════════════════════════════════════════════════")
                    NetworkResult.Error("El servidor no pudo procesar la solicitud")
                }
            } catch (e: HttpException) {
                val elapsed = System.currentTimeMillis() - startTime
                Log.e(TAG, "HttpException en ${elapsed}ms - Code: ${e.code()}, Message: ${e.message()}", e)
                Log.d(TAG, "═══════════════════════════════════════════════════")
                NetworkResult.Error("Error del servidor: ${e.code()}")
            } catch (e: IOException) {
                val elapsed = System.currentTimeMillis() - startTime
                Log.e(TAG, "IOException en ${elapsed}ms - Sin conexion a internet", e)
                Log.d(TAG, "═══════════════════════════════════════════════════")
                NetworkResult.Error("Error de conexion: Verifique su internet")
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                Log.e(TAG, "Exception inesperada en ${elapsed}ms - ${e.javaClass.simpleName}: ${e.message}", e)
                Log.d(TAG, "═══════════════════════════════════════════════════")
                NetworkResult.Error("Error inesperado: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }
}
