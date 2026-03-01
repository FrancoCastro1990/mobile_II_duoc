package com.example.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class FormularioInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun navegarAFormulario() {
        composeRule.onNodeWithTag("fab_nueva_solicitud").performClick()
        composeRule.onNodeWithText("Nueva Solicitud").assertIsDisplayed()
    }

    @Test
    fun formulario_ingresarTextoEnCampos() {
        navegarAFormulario()

        composeRule.onNodeWithTag("campo_nombre_cliente").performTextInput("Juan Perez")
        composeRule.onNodeWithTag("campo_telefono").performTextInput("+56912345678")
        composeRule.onNodeWithTag("campo_direccion").performTextInput("Av. Providencia 1234")
        composeRule.onNodeWithTag("campo_descripcion").performTextInput("Filtracion en cocina")

        composeRule.onNodeWithTag("campo_nombre_cliente").assertTextContains("Juan Perez")
        composeRule.onNodeWithTag("campo_telefono").assertTextContains("+56912345678")
        composeRule.onNodeWithTag("campo_direccion").assertTextContains("Av. Providencia 1234")
        composeRule.onNodeWithTag("campo_descripcion").assertTextContains("Filtracion en cocina")
    }

    @Test
    fun formulario_seleccionarTipoServicio() {
        navegarAFormulario()

        composeRule.onNodeWithTag("selector_tipo_servicio").performClick()
        composeRule.onNodeWithText("Gasfiteria").performClick()

        composeRule.onNodeWithText("Gasfiteria").assertIsDisplayed()
    }

    @Test
    fun formulario_guardarSinCampos_permaneceEnForm() {
        navegarAFormulario()

        composeRule.onNodeWithTag("btn_guardar").performClick()

        // El formulario sigue visible (no navego)
        composeRule.onNodeWithText("Nueva Solicitud").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_guardar").assertIsDisplayed()
    }

    @Test
    fun formulario_completarYGuardar() {
        navegarAFormulario()

        // Seleccionar tipo de servicio
        composeRule.onNodeWithTag("selector_tipo_servicio").performClick()
        composeRule.onNodeWithText("Gasfiteria").performClick()

        // Completar campos
        composeRule.onNodeWithTag("campo_nombre_cliente").performTextInput("Juan Perez")
        composeRule.onNodeWithTag("campo_telefono").performTextInput("+56912345678")
        composeRule.onNodeWithTag("campo_direccion").performTextInput("Av. Providencia 1234")
        composeRule.onNodeWithTag("campo_descripcion").performTextInput("Filtracion en cocina")

        // Guardar
        composeRule.onNodeWithTag("btn_guardar").performClick()

        // Esperar navegacion y verificar que volvimos a Home
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Necesitas Ayuda?")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Necesitas Ayuda?").assertIsDisplayed()
    }
}
