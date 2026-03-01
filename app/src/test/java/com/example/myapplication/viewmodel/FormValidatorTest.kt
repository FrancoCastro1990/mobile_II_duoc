package com.example.myapplication.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FormValidatorTest {

    private lateinit var formValidator: FormValidator

    @Before
    fun setUp() {
        formValidator = FormValidator()
    }

    // ===================== Casos positivos =====================

    @Test
    fun validate_todosLosCamposCompletos_retornaValido() {
        val result = formValidator.validate(
            tipoServicio = "gasfiteria",
            nombreCliente = "Juan Perez",
            telefono = "+56912345678",
            direccion = "Av. Providencia 1234"
        )

        assertTrue(result.isValid)
        assertTrue(result.camposVacios.isEmpty())
    }

    @Test
    fun validate_camposConEspaciosInternos_retornaValido() {
        val result = formValidator.validate(
            tipoServicio = "electricidad",
            nombreCliente = "Maria Jose Lopez",
            telefono = "+56 9 8765 4321",
            direccion = "Calle Los Alerces 567, Depto 12B"
        )

        assertTrue(result.isValid)
        assertTrue(result.camposVacios.isEmpty())
    }

    @Test
    fun validate_camposDeUnCaracter_retornaValido() {
        val result = formValidator.validate(
            tipoServicio = "g",
            nombreCliente = "J",
            telefono = "9",
            direccion = "A"
        )

        assertTrue(result.isValid)
        assertTrue(result.camposVacios.isEmpty())
    }

    // ===================== Casos negativos =====================

    @Test
    fun validate_todosLosCamposVacios_retornaInvalido() {
        val result = formValidator.validate(
            tipoServicio = "",
            nombreCliente = "",
            telefono = "",
            direccion = ""
        )

        assertFalse(result.isValid)
        assertEquals(4, result.camposVacios.size)
        assertTrue(result.camposVacios.contains("tipoServicio"))
        assertTrue(result.camposVacios.contains("nombreCliente"))
        assertTrue(result.camposVacios.contains("telefono"))
        assertTrue(result.camposVacios.contains("direccion"))
    }

    @Test
    fun validate_tipoServicioVacio_retornaCampoFaltante() {
        val result = formValidator.validate(
            tipoServicio = "",
            nombreCliente = "Juan Perez",
            telefono = "+56912345678",
            direccion = "Av. Providencia 1234"
        )

        assertFalse(result.isValid)
        assertEquals(1, result.camposVacios.size)
        assertEquals("tipoServicio", result.camposVacios[0])
    }

    @Test
    fun validate_nombreClienteVacio_retornaCampoFaltante() {
        val result = formValidator.validate(
            tipoServicio = "gasfiteria",
            nombreCliente = "",
            telefono = "+56912345678",
            direccion = "Av. Providencia 1234"
        )

        assertFalse(result.isValid)
        assertEquals(1, result.camposVacios.size)
        assertEquals("nombreCliente", result.camposVacios[0])
    }

    @Test
    fun validate_telefonoVacio_retornaCampoFaltante() {
        val result = formValidator.validate(
            tipoServicio = "gasfiteria",
            nombreCliente = "Juan Perez",
            telefono = "",
            direccion = "Av. Providencia 1234"
        )

        assertFalse(result.isValid)
        assertEquals(1, result.camposVacios.size)
        assertEquals("telefono", result.camposVacios[0])
    }

    @Test
    fun validate_direccionVacia_retornaCampoFaltante() {
        val result = formValidator.validate(
            tipoServicio = "gasfiteria",
            nombreCliente = "Juan Perez",
            telefono = "+56912345678",
            direccion = ""
        )

        assertFalse(result.isValid)
        assertEquals(1, result.camposVacios.size)
        assertEquals("direccion", result.camposVacios[0])
    }

    @Test
    fun validate_camposConSoloEspacios_retornaInvalido() {
        val result = formValidator.validate(
            tipoServicio = "   ",
            nombreCliente = "  ",
            telefono = " ",
            direccion = "    "
        )

        assertFalse(result.isValid)
        assertEquals(4, result.camposVacios.size)
    }

    @Test
    fun validate_dosCamposFaltantes_retornaAmbos() {
        val result = formValidator.validate(
            tipoServicio = "",
            nombreCliente = "Juan Perez",
            telefono = "",
            direccion = "Av. Providencia 1234"
        )

        assertFalse(result.isValid)
        assertEquals(2, result.camposVacios.size)
        assertTrue(result.camposVacios.contains("tipoServicio"))
        assertTrue(result.camposVacios.contains("telefono"))
    }
}
