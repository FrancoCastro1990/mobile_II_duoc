package com.example.myapplication.data.repository

import com.example.myapplication.data.local.SolicitudDao
import com.example.myapplication.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(private val solicitudDao: SolicitudDao) {

    val allSolicitudes: Flow<List<SolicitudEntity>> = solicitudDao.getAllSolicitudes()

    suspend fun getSolicitudById(id: Long): SolicitudEntity? {
        return solicitudDao.getSolicitudById(id)
    }

    suspend fun insertSolicitud(solicitud: SolicitudEntity): Long {
        return solicitudDao.insertSolicitud(solicitud)
    }

    suspend fun updateSolicitud(solicitud: SolicitudEntity) {
        solicitudDao.updateSolicitud(solicitud)
    }

    suspend fun deleteSolicitud(solicitud: SolicitudEntity) {
        solicitudDao.deleteSolicitud(solicitud)
    }

    suspend fun updateEstado(id: Long, estado: String) {
        solicitudDao.updateEstado(id, estado)
    }

    suspend fun deleteSolicitudById(id: Long) {
        solicitudDao.deleteSolicitudById(id)
    }
}
