package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoServicio: String,
    val descripcion: String,
    val nombreCliente: String,
    val telefono: String,
    val direccion: String,
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val estado: String = "pendiente"
)
