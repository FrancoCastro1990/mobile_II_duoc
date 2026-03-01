package com.example.myapplication.viewmodel

data class ValidationResult(
    val isValid: Boolean,
    val camposVacios: List<String>
)

class FormValidator {

    fun validate(
        tipoServicio: String,
        nombreCliente: String,
        telefono: String,
        direccion: String
    ): ValidationResult {
        val camposVacios = mutableListOf<String>()

        if (tipoServicio.isBlank()) camposVacios.add("tipoServicio")
        if (nombreCliente.isBlank()) camposVacios.add("nombreCliente")
        if (telefono.isBlank()) camposVacios.add("telefono")
        if (direccion.isBlank()) camposVacios.add("direccion")

        return ValidationResult(
            isValid = camposVacios.isEmpty(),
            camposVacios = camposVacios
        )
    }
}
