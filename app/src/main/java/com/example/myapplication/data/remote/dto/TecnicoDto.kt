package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TecnicoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("especialidad") val especialidad: String,
    @SerializedName("calificacion") val calificacion: Double,
    @SerializedName("experiencia") val experiencia: Int,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("disponible") val disponible: Boolean,
    @SerializedName("zona") val zona: String
)
