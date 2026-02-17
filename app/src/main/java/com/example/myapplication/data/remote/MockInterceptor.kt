package com.example.myapplication.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {

    companion object {
        private const val TAG = "MockInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        Log.d(TAG, "Interceptando request: $path")

        // Simular delay de red (100-300ms)
        val delay = (100..300).random().toLong()
        Log.d(TAG, "Simulando delay de red: ${delay}ms")
        Thread.sleep(delay)

        val responseJson = when {
            path.contains("tecnicos/gasfiteria") -> getGasfiteriaJson()
            path.contains("tecnicos/electricidad") -> getElectricidadJson()
            path.contains("tecnicos/electrodomesticos") -> getElectrodomesticosJson()
            else -> getDefaultJson()
        }

        Log.d(TAG, "Respondiendo con mock data para: $path")

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(responseJson.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun getGasfiteriaJson(): String = """
    {
        "success": true,
        "data": [
            {"id": 1, "nombre": "Carlos Muñoz", "especialidad": "Gasfiteria", "calificacion": 4.8, "experiencia": 12, "telefono": "+56 9 1234 5678", "disponible": true, "zona": "Santiago Centro"},
            {"id": 2, "nombre": "Roberto Soto", "especialidad": "Gasfiteria", "calificacion": 4.5, "experiencia": 8, "telefono": "+56 9 2345 6789", "disponible": true, "zona": "Providencia"},
            {"id": 3, "nombre": "Miguel Torres", "especialidad": "Gasfiteria", "calificacion": 4.9, "experiencia": 15, "telefono": "+56 9 3456 7890", "disponible": false, "zona": "Las Condes"},
            {"id": 4, "nombre": "Juan Perez", "especialidad": "Gasfiteria", "calificacion": 4.2, "experiencia": 5, "telefono": "+56 9 4567 8901", "disponible": true, "zona": "Maipu"},
            {"id": 5, "nombre": "Andres Vargas", "especialidad": "Gasfiteria", "calificacion": 4.6, "experiencia": 10, "telefono": "+56 9 5678 9012", "disponible": true, "zona": "Ñuñoa"},
            {"id": 6, "nombre": "Felipe Rojas", "especialidad": "Gasfiteria", "calificacion": 4.3, "experiencia": 7, "telefono": "+56 9 6789 0123", "disponible": false, "zona": "La Florida"},
            {"id": 7, "nombre": "Patricio Diaz", "especialidad": "Gasfiteria", "calificacion": 4.7, "experiencia": 11, "telefono": "+56 9 7890 1234", "disponible": true, "zona": "Vitacura"},
            {"id": 8, "nombre": "Gonzalo Fuentes", "especialidad": "Gasfiteria", "calificacion": 4.1, "experiencia": 4, "telefono": "+56 9 8901 2345", "disponible": true, "zona": "Puente Alto"},
            {"id": 9, "nombre": "Sebastian Morales", "especialidad": "Gasfiteria", "calificacion": 4.4, "experiencia": 6, "telefono": "+56 9 9012 3456", "disponible": true, "zona": "San Bernardo"},
            {"id": 10, "nombre": "Diego Herrera", "especialidad": "Gasfiteria", "calificacion": 4.8, "experiencia": 14, "telefono": "+56 9 0123 4567", "disponible": false, "zona": "Macul"}
        ],
        "count": 10,
        "timestamp": ${System.currentTimeMillis()}
    }
    """.trimIndent()

    private fun getElectricidadJson(): String = """
    {
        "success": true,
        "data": [
            {"id": 11, "nombre": "Luis Gonzalez", "especialidad": "Electricidad", "calificacion": 4.9, "experiencia": 18, "telefono": "+56 9 1111 2222", "disponible": true, "zona": "Santiago Centro"},
            {"id": 12, "nombre": "Alejandro Silva", "especialidad": "Electricidad", "calificacion": 4.6, "experiencia": 9, "telefono": "+56 9 2222 3333", "disponible": true, "zona": "Providencia"},
            {"id": 13, "nombre": "Ricardo Castro", "especialidad": "Electricidad", "calificacion": 4.7, "experiencia": 13, "telefono": "+56 9 3333 4444", "disponible": true, "zona": "Las Condes"},
            {"id": 14, "nombre": "Fernando Reyes", "especialidad": "Electricidad", "calificacion": 4.3, "experiencia": 6, "telefono": "+56 9 4444 5555", "disponible": false, "zona": "Maipu"},
            {"id": 15, "nombre": "Eduardo Martinez", "especialidad": "Electricidad", "calificacion": 4.5, "experiencia": 10, "telefono": "+56 9 5555 6666", "disponible": true, "zona": "Ñuñoa"},
            {"id": 16, "nombre": "Pablo Gutierrez", "especialidad": "Electricidad", "calificacion": 4.8, "experiencia": 16, "telefono": "+56 9 6666 7777", "disponible": true, "zona": "La Florida"},
            {"id": 17, "nombre": "Cristian Flores", "especialidad": "Electricidad", "calificacion": 4.2, "experiencia": 5, "telefono": "+56 9 7777 8888", "disponible": false, "zona": "Vitacura"},
            {"id": 18, "nombre": "Marcos Sandoval", "especialidad": "Electricidad", "calificacion": 4.4, "experiencia": 7, "telefono": "+56 9 8888 9999", "disponible": true, "zona": "Puente Alto"},
            {"id": 19, "nombre": "Victor Araya", "especialidad": "Electricidad", "calificacion": 4.6, "experiencia": 11, "telefono": "+56 9 9999 0000", "disponible": true, "zona": "San Bernardo"},
            {"id": 20, "nombre": "Hector Navarro", "especialidad": "Electricidad", "calificacion": 4.1, "experiencia": 3, "telefono": "+56 9 0000 1111", "disponible": true, "zona": "Macul"}
        ],
        "count": 10,
        "timestamp": ${System.currentTimeMillis()}
    }
    """.trimIndent()

    private fun getElectrodomesticosJson(): String = """
    {
        "success": true,
        "data": [
            {"id": 21, "nombre": "Raul Espinoza", "especialidad": "Electrodomesticos", "calificacion": 4.7, "experiencia": 14, "telefono": "+56 9 1122 3344", "disponible": true, "zona": "Santiago Centro"},
            {"id": 22, "nombre": "Oscar Pizarro", "especialidad": "Electrodomesticos", "calificacion": 4.5, "experiencia": 9, "telefono": "+56 9 2233 4455", "disponible": true, "zona": "Providencia"},
            {"id": 23, "nombre": "Javier Contreras", "especialidad": "Electrodomesticos", "calificacion": 4.8, "experiencia": 16, "telefono": "+56 9 3344 5566", "disponible": false, "zona": "Las Condes"},
            {"id": 24, "nombre": "Alberto Figueroa", "especialidad": "Electrodomesticos", "calificacion": 4.3, "experiencia": 6, "telefono": "+56 9 4455 6677", "disponible": true, "zona": "Maipu"},
            {"id": 25, "nombre": "Manuel Bravo", "especialidad": "Electrodomesticos", "calificacion": 4.6, "experiencia": 11, "telefono": "+56 9 5566 7788", "disponible": true, "zona": "Ñuñoa"},
            {"id": 26, "nombre": "Ignacio Vega", "especialidad": "Electrodomesticos", "calificacion": 4.9, "experiencia": 20, "telefono": "+56 9 6677 8899", "disponible": true, "zona": "La Florida"},
            {"id": 27, "nombre": "Tomas Henriquez", "especialidad": "Electrodomesticos", "calificacion": 4.2, "experiencia": 4, "telefono": "+56 9 7788 9900", "disponible": false, "zona": "Vitacura"},
            {"id": 28, "nombre": "Nicolas Valenzuela", "especialidad": "Electrodomesticos", "calificacion": 4.4, "experiencia": 8, "telefono": "+56 9 8899 0011", "disponible": true, "zona": "Puente Alto"},
            {"id": 29, "nombre": "Rodrigo Acevedo", "especialidad": "Electrodomesticos", "calificacion": 4.7, "experiencia": 13, "telefono": "+56 9 9900 1122", "disponible": true, "zona": "San Bernardo"},
            {"id": 30, "nombre": "Daniel Palma", "especialidad": "Electrodomesticos", "calificacion": 4.0, "experiencia": 3, "telefono": "+56 9 0011 2233", "disponible": false, "zona": "Macul"}
        ],
        "count": 10,
        "timestamp": ${System.currentTimeMillis()}
    }
    """.trimIndent()

    private fun getDefaultJson(): String = """
    {
        "success": false,
        "data": [],
        "count": 0,
        "timestamp": ${System.currentTimeMillis()}
    }
    """.trimIndent()
}
