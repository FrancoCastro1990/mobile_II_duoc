package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Query("SELECT * FROM solicitudes ORDER BY fechaSolicitud DESC")
    fun getAllSolicitudes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun getSolicitudById(id: Long): SolicitudEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudEntity): Long

    @Update
    suspend fun updateSolicitud(solicitud: SolicitudEntity)

    @Delete
    suspend fun deleteSolicitud(solicitud: SolicitudEntity)

    @Query("UPDATE solicitudes SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Long, estado: String)

    @Query("DELETE FROM solicitudes WHERE id = :id")
    suspend fun deleteSolicitudById(id: Long)
}
