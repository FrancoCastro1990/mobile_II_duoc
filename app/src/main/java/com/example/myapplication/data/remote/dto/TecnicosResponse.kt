package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TecnicosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<TecnicoDto>,
    @SerializedName("count") val count: Int,
    @SerializedName("timestamp") val timestamp: Long
)
