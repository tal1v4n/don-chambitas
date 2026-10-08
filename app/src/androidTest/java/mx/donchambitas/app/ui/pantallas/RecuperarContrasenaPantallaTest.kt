package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla de recuperar contrasena (P-04).
 * Cubren la anatomia de las dos vistas, que la confirmacion no repite el
 * correo, cuando aparece "Reintentar", el bloqueo durante la carga y el
 * regreso a P-02. La normalizacion y la tabla 4.4 se prueban sin emulador en
 * RecuperarContrasenaViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class RecuperarContrasenaPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    /** Sin retraso: la espera de 300 ms del repositorio falso no aporta nada aqui. */
    private fun montarPantalla(alRegresar: () -> Unit = {}) {
        val repositorio = RepositorioAuthFalso(FuenteDatosFalsa()).apply { retrasoMs = 0 }
        composeTestRule.setContent {
            DonChambitasTema {
                RecuperarContrasenaPantalla(
                    alRegresar = alRegresar,
                    viewModel = RecuperarContrasenaViewModel(repositorio)
                )
            }
        }
    }

    private fun montarContenido(
        estado: EstadoRecuperarContrasena,
        alRegresar: () -> Unit = {},
        alReintentar: (() -> Unit)? = null
    ) {
        composeTestRule.setContent {
            DonChambitasTema {
                RecuperarContrasenaContenido(
                    estado = estado,
                    alCambiarCorreo = {},
                    alEnviar = {},
                    alRegresar = alRegresar,
                    alReintentar = alReintentar
                )
            }
        }
    }

    @Test
    fun debeMostrarLaAnatomiaDelFormulario_cuandoSeAbreLaPantalla() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.recuperar_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_explicacion)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_accion)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_volver)).assertIsDisplayed()
    }

    @Test
    fun debeMarcarElCorreoYDejarleElFoco_cuandoSeEnviaVacio() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.recuperar_accion)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.validacion_correo_vacio)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsFocused()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_enviado_titulo)).assertDoesNotExist()
    }

    @Test
    fun debePasarALaConfirmacionSinMostrarElCorreo_cuandoSeEnviaUnCorreoValido() {
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.auth_correo))
            .performTextInput("Refugio@Ejemplo.MX")
        composeTestRule.onNodeWithText(texto(R.string.recuperar_accion)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(texto(R.string.recuperar_enviado_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Refugio@Ejemplo.MX", substring = true, ignoreCase = true)
            .assertDoesNotExist()
    }

    @Test
    fun debeMostrarLaAnatomiaDeLaConfirmacion_cuandoYaSeEnvio() {
        montarContenido(EstadoRecuperarContrasena(correo = "refugio@ejemplo.mx", enviado = true))

        composeTestRule.onNodeWithText(texto(R.string.recuperar_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(texto(R.string.recuperar_enviado_descripcion))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_enviado_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_enviado_mensaje)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_vigencia)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_volver)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_accion)).assertDoesNotExist()
    }

    /**
     * Mostrar el correo en la confirmacion refuerza la lectura de que la
     * cuenta existe, y el contrato no verifico nada (4.2).
     */
    @Test
    fun debeNoMostrarElCorreoEscrito_cuandoEstaEnLaConfirmacion() {
        montarContenido(EstadoRecuperarContrasena(correo = "refugio@ejemplo.mx", enviado = true))

        composeTestRule.onNodeWithText("refugio@ejemplo.mx", substring = true).assertDoesNotExist()
    }

    @Test
    fun debeOfrecerReintentarYConservarElCorreo_cuandoElErrorEsDeRed() {
        var reintentos = 0
        montarContenido(
            estado = EstadoRecuperarContrasena(
                correo = "refugio@ejemplo.mx",
                errorPantalla = TipoError.RED
            ),
            alReintentar = { reintentos++ }
        )

        composeTestRule.onNodeWithText("refugio@ejemplo.mx", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).performClick()

        assertEquals(1, reintentos)
    }

    @Test
    fun debeNoOfrecerReintentar_cuandoElErrorEsDeValidacion() {
        montarContenido(
            estado = EstadoRecuperarContrasena(errorPantalla = TipoError.VALIDACION),
            alReintentar = {}
        )

        composeTestRule.onNodeWithText(texto(R.string.error_validacion)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).assertDoesNotExist()
    }

    /**
     * Igual que en P-02: mientras carga, el boton cambia su etiqueta por el
     * indicador, asi que deja de existir un nodo con ese texto.
     */
    @Test
    fun debeBloquearElCampoYElEnlace_cuandoEstaCargando() {
        montarContenido(EstadoRecuperarContrasena(correo = "refugio@ejemplo.mx", cargando = true))

        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_volver)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_accion)).assertDoesNotExist()
    }

    @Test
    fun debeRegresar_cuandoSeTocaLaFlechaOVolver() {
        var regresos = 0
        montarContenido(EstadoRecuperarContrasena(), alRegresar = { regresos++ })

        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).performClick()
        composeTestRule.onNodeWithText(texto(R.string.recuperar_volver)).performClick()

        assertEquals(2, regresos)
    }

    @Test
    fun debeRegresar_cuandoSeTocaVolverDesdeLaConfirmacion() {
        var regresos = 0
        montarContenido(EstadoRecuperarContrasena(enviado = true), alRegresar = { regresos++ })

        composeTestRule.onNodeWithText(texto(R.string.recuperar_volver)).performClick()

        assertEquals(1, regresos)
    }
}
