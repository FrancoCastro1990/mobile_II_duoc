package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.TecnicosResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("tecnicos/{tipo}")
    suspend fun getTecnicosByTipo(@Path("tipo") tipo: String): TecnicosResponse
}
