package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.data.remote.NetworkResult
import com.example.myapplication.data.remote.dto.TecnicoDto
import com.example.myapplication.data.remote.dto.TecnicosResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TecnicoRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: TecnicoRepository

    private val tecnicosMock = listOf(
        TecnicoDto(
            id = 1,
            nombre = "Carlos Rodriguez",
            especialidad = "gasfiteria",
            calificacion = 4.8,
            experiencia = 10,
            telefono = "+56912345678",
            disponible = true,
            zona = "Santiago Centro"
        ),
        TecnicoDto(
            id = 2,
            nombre = "Maria Gonzalez",
            especialidad = "gasfiteria",
            calificacion = 4.5,
            experiencia = 7,
            telefono = "+56987654321",
            disponible = true,
            zona = "Providencia"
        )
    )

    @Before
    fun setUp() {
        apiService = mockk()
        repository = TecnicoRepository(apiService)
    }

    // ===================== Casos positivos =====================

    @Test
    fun getTecnicosByTipo_respuestaExitosa_retornaSuccess() = runTest {
        val response = TecnicosResponse(
            success = true,
            data = tecnicosMock,
            count = tecnicosMock.size,
            timestamp = System.currentTimeMillis()
        )
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } returns response

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(2, data.size)
        assertEquals("Carlos Rodriguez", data[0].nombre)
        assertEquals("Maria Gonzalez", data[1].nombre)
    }

    @Test
    fun getTecnicosByTipo_listaVacia_retornaSuccessVacio() = runTest {
        val response = TecnicosResponse(
            success = true,
            data = emptyList(),
            count = 0,
            timestamp = System.currentTimeMillis()
        )
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } returns response

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.isEmpty())
    }

    @Test
    fun getTecnicosByTipo_verificaLlamadaCorrecta() = runTest {
        val response = TecnicosResponse(
            success = true,
            data = tecnicosMock,
            count = tecnicosMock.size,
            timestamp = System.currentTimeMillis()
        )
        coEvery { apiService.getTecnicosByTipo("electricidad") } returns response

        repository.getTecnicosByTipo("electricidad")

        coVerify(exactly = 1) { apiService.getTecnicosByTipo("electricidad") }
    }

    // ===================== Casos negativos =====================

    @Test
    fun getTecnicosByTipo_successFalse_retornaError() = runTest {
        val response = TecnicosResponse(
            success = false,
            data = emptyList(),
            count = 0,
            timestamp = System.currentTimeMillis()
        )
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } returns response

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertTrue(message.contains("servidor"))
    }

    @Test
    fun getTecnicosByTipo_httpException_retornaError() = runTest {
        val httpException = HttpException(
            Response.error<TecnicosResponse>(500, "Server Error".toResponseBody(null))
        )
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } throws httpException

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertTrue(message.contains("500"))
    }

    @Test
    fun getTecnicosByTipo_ioException_retornaErrorConexion() = runTest {
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } throws IOException("No internet")

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertTrue(message.contains("conexion", ignoreCase = true))
    }

    @Test
    fun getTecnicosByTipo_excepcionGenerica_retornaError() = runTest {
        coEvery { apiService.getTecnicosByTipo("gasfiteria") } throws RuntimeException("Error inesperado")

        val result = repository.getTecnicosByTipo("gasfiteria")

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertTrue(message.contains("inesperado", ignoreCase = true))
    }
}
