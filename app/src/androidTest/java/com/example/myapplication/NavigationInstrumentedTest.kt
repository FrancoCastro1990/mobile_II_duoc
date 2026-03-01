package com.example.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_muestraTitulo() {
        composeRule.onNodeWithText("Necesitas Ayuda?").assertIsDisplayed()
    }

    @Test
    fun homeScreen_fabNavegaAFormulario() {
        composeRule.onNodeWithTag("fab_nueva_solicitud").performClick()
        composeRule.onNodeWithText("Nueva Solicitud").assertIsDisplayed()
    }

    @Test
    fun formScreen_volverAHome() {
        // Navegar al formulario
        composeRule.onNodeWithTag("fab_nueva_solicitud").performClick()
        composeRule.onNodeWithText("Nueva Solicitud").assertIsDisplayed()

        // Volver atras
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        // Verificar que volvimos a Home
        composeRule.onNodeWithText("Necesitas Ayuda?").assertIsDisplayed()
    }

    @Test
    fun formScreen_muestraSeccionesFormulario() {
        composeRule.onNodeWithTag("fab_nueva_solicitud").performClick()

        composeRule.onNodeWithText("Tipo de Servicio").assertIsDisplayed()
        composeRule.onNodeWithText("Datos del Cliente").assertIsDisplayed()
        composeRule.onNodeWithText("Descripcion del Problema").assertIsDisplayed()
    }

    @Test
    fun formScreen_selectorAbreDialogo() {
        composeRule.onNodeWithTag("fab_nueva_solicitud").performClick()
        composeRule.onNodeWithTag("selector_tipo_servicio").performClick()

        composeRule.onNodeWithText("Seleccione el tipo de servicio").assertIsDisplayed()
        composeRule.onNodeWithText("Gasfiteria").assertIsDisplayed()
        composeRule.onNodeWithText("Electricidad").assertIsDisplayed()
        composeRule.onNodeWithText("Electrodomesticos").assertIsDisplayed()
    }
}
