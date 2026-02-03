package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.SolicitudDao
import com.example.myapplication.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(private val solicitudDao: SolicitudDao) {

    companion object {
        private const val TAG = "SolicitudRepository"
        private const val TAG_DB = "SolicitudDB"
    }

    val allSolicitudes: Flow<List<SolicitudEntity>> = solicitudDao.getAllSolicitudes()

    init {
        Log.d(TAG, "Repository inicializado - Flow de solicitudes configurado")
    }

    suspend fun getSolicitudById(id: Long): SolicitudEntity? {
        Log.d(TAG_DB, "SELECT solicitud por ID: $id")
        val startTime = System.currentTimeMillis()

        return try {
            val result = solicitudDao.getSolicitudById(id)
            val queryTime = System.currentTimeMillis() - startTime

            if (result != null) {
                Log.d(TAG_DB, "Solicitud encontrada en ${queryTime}ms - ID: $id, Cliente: ${result.nombreCliente}")
            } else {
                Log.w(TAG_DB, "Solicitud NO encontrada en ${queryTime}ms - ID: $id")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en SELECT por ID: $id - ${e.message}", e)
            throw e
        }
    }

    suspend fun insertSolicitud(solicitud: SolicitudEntity): Long {
        Log.d(TAG_DB, "INSERT nueva solicitud - Cliente: ${solicitud.nombreCliente}, Tipo: ${solicitud.tipoServicio}")
        val startTime = System.currentTimeMillis()

        return try {
            val newId = solicitudDao.insertSolicitud(solicitud)
            val insertTime = System.currentTimeMillis() - startTime
            Log.i(TAG_DB, "INSERT exitoso en ${insertTime}ms - Nuevo ID: $newId")
            newId
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en INSERT - Cliente: ${solicitud.nombreCliente}", e)
            throw e
        }
    }

    suspend fun updateSolicitud(solicitud: SolicitudEntity) {
        Log.d(TAG_DB, "UPDATE solicitud - ID: ${solicitud.id}, Cliente: ${solicitud.nombreCliente}")
        val startTime = System.currentTimeMillis()

        try {
            solicitudDao.updateSolicitud(solicitud)
            val updateTime = System.currentTimeMillis() - startTime
            Log.i(TAG_DB, "UPDATE exitoso en ${updateTime}ms - ID: ${solicitud.id}")
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en UPDATE - ID: ${solicitud.id}", e)
            throw e
        }
    }

    suspend fun deleteSolicitud(solicitud: SolicitudEntity) {
        Log.d(TAG_DB, "DELETE solicitud por entidad - ID: ${solicitud.id}")
        val startTime = System.currentTimeMillis()

        try {
            solicitudDao.deleteSolicitud(solicitud)
            val deleteTime = System.currentTimeMillis() - startTime
            Log.i(TAG_DB, "DELETE exitoso en ${deleteTime}ms - ID: ${solicitud.id}")
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en DELETE por entidad - ID: ${solicitud.id}", e)
            throw e
        }
    }

    suspend fun updateEstado(id: Long, estado: String) {
        Log.d(TAG_DB, "UPDATE estado - ID: $id, Nuevo estado: $estado")
        val startTime = System.currentTimeMillis()

        try {
            solicitudDao.updateEstado(id, estado)
            val updateTime = System.currentTimeMillis() - startTime
            Log.i(TAG_DB, "UPDATE estado exitoso en ${updateTime}ms - ID: $id → $estado")
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en UPDATE estado - ID: $id, Estado: $estado", e)
            throw e
        }
    }

    suspend fun deleteSolicitudById(id: Long) {
        Log.d(TAG_DB, "DELETE solicitud por ID: $id")
        val startTime = System.currentTimeMillis()

        try {
            solicitudDao.deleteSolicitudById(id)
            val deleteTime = System.currentTimeMillis() - startTime
            Log.i(TAG_DB, "DELETE exitoso en ${deleteTime}ms - ID: $id")
        } catch (e: Exception) {
            Log.e(TAG_DB, "Error en DELETE por ID: $id", e)
            throw e
        }
    }
}
