package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
import mx.donchambitas.app.datos.falso.RepositorioUsuarioFalso
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.tema.DonChambitasTema
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla Mi cuenta (P-18).
 * Cubren la anatomia de las tres vistas, los accesos por rol, que el correo y
 * el rol no se editan, el paso entre vistas y la confirmacion del cierre. Las
 * reglas de validacion y cada resultado de los repositorios se prueban sin
 * emulador en MiCuentaViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class MiCuentaPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fuente = FuenteDatosFalsa()
    private val repositorioAuth = RepositorioAuthFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioUsuario = RepositorioUsuarioFalso(fuente).apply { retrasoMs = 0 }

    private fun texto(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun entrarComo(rol: RolUsuario) = fuente.usuarios.first { it.rol == rol }
        .also { fuente.fijarSesionActiva(Sesion(it)) }

    private fun montarPantalla(
        llegaPorRecuperacion: Boolean = false,
        alIrAMiPerfil: () -> Unit = {},
        alIrAMisPostulaciones: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            DonChambitasTema {
                MiCuentaPantalla(
                    alIrAMiPerfil = alIrAMiPerfil,
                    alIrAMisPostulaciones = alIrAMisPostulaciones,
                    llegaPorRecuperacion = llegaPorRecuperacion,
                    viewModel = MiCuentaViewModel(repositorioUsuario, repositorioAuth)
                )
            }
        }
    }

    private fun pulsar(@StringRes id: Int) {
        composeTestRule.onNodeWithText(texto(id)).performScrollTo().performClick()
    }

    private fun campo(@StringRes etiqueta: Int) =
        composeTestRule.onNode(hasSetTextAction() and hasText(texto(etiqueta)))

    // ---- resumen ----------------------------------------------------------

    @Test
    fun debeMostrarLaFichaSinAccesosDeTrabajador_cuandoEntraUnCliente() {
        val cliente = entrarComo(RolUsuario.CLIENTE)
        montarPantalla()

        composeTestRule.onNodeWithText(texto(R.string.cuenta_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText("${cliente.nombre} ${cliente.apellidos}").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_rol_cliente)).assertIsDisplayed()
        composeTestRule.onNodeWithText(cliente.correo).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_editar_datos)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_mi_perfil)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_mis_postulaciones)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).assertDoesNotExist()
    }

    @Test
    fun debeOfrecerMiPerfilYMisPostulaciones_cuandoEntraUnTrabajador() {
        entrarComo(RolUsuario.TRABAJADOR)
        var destinos = emptyList<String>()
        montarPantalla(
            alIrAMiPerfil = { destinos = destinos + "P-11" },
            alIrAMisPostulaciones = { destinos = destinos + "P-14" }
        )

        composeTestRule.onNodeWithText(texto(R.string.cuenta_rol_trabajador)).assertIsDisplayed()
        pulsar(R.string.cuenta_mi_perfil)
        pulsar(R.string.cuenta_mis_postulaciones)

        assertEquals(listOf("P-11", "P-14"), destinos)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaFichaNoCarga() {
        var reintentos = 0
        composeTestRule.setContent {
            DonChambitasTema {
                MiCuentaContenido(
                    estado = EstadoMiCuenta(cargando = false, errorCarga = TipoError.RED),
                    alReintentarCarga = { reintentos++ }
                )
            }
        }

        composeTestRule.onNodeWithText(texto(R.string.error_titulo_red)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.reintentar)).performClick()

        assertEquals(1, reintentos)
    }

    // ---- editar datos -----------------------------------------------------

    @Test
    fun debeAbrirLaEdicionConLosDatosYSinCorreoNiRol_cuandoSePulsaEditar() {
        val cliente = entrarComo(RolUsuario.CLIENTE)
        montarPantalla()

        pulsar(R.string.cuenta_editar_datos)

        campo(R.string.registro_nombre).assertIsDisplayed()
        composeTestRule.onNodeWithText(cliente.nombre).assertIsDisplayed()
        composeTestRule.onNodeWithText(cliente.apellidos).assertIsDisplayed()
        composeTestRule.onNodeWithText(cliente.correo).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.auth_correo)).assertDoesNotExist()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_rol_cliente)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).assertIsDisplayed()
    }

    @Test
    fun debeGuardarYMostrarLosDatosNuevosEnElResumen_cuandoSeEditaElNombre() {
        val cliente = entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        pulsar(R.string.cuenta_editar_datos)

        campo(R.string.registro_nombre).performTextClearance()
        campo(R.string.registro_nombre).performTextInput("Refugio")
        pulsar(R.string.cuenta_guardar)

        composeTestRule.onNodeWithText("Refugio ${cliente.apellidos}").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_aviso_datos)).assertIsDisplayed()
        assertEquals("Refugio", fuente.usuarios.first { it.id == cliente.id }.nombre)
    }

    @Test
    fun debeMarcarElNombreYDejarleElFoco_cuandoSeGuardaVacio() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        pulsar(R.string.cuenta_editar_datos)

        campo(R.string.registro_nombre).performTextClearance()
        pulsar(R.string.cuenta_guardar)

        composeTestRule.onNodeWithText(texto(R.string.validacion_nombre_vacio)).assertIsDisplayed()
        campo(R.string.registro_nombre).assertIsFocused()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_guardar)).assertIsDisplayed()
    }

    @Test
    fun debeVolverAlResumenSinGuardar_cuandoSePulsaLaFlecha() {
        val cliente = entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        pulsar(R.string.cuenta_editar_datos)
        campo(R.string.registro_nombre).performTextClearance()

        composeTestRule.onNodeWithContentDescription(texto(R.string.regresar)).performClick()

        composeTestRule.onNodeWithText("${cliente.nombre} ${cliente.apellidos}").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_aviso_datos)).assertDoesNotExist()
    }

    // ---- cambiar contrasena -----------------------------------------------

    @Test
    fun debeAbrirDirectoEnLaContrasena_cuandoSeLlegaPorRecuperacion() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla(llegaPorRecuperacion = true)

        composeTestRule.onNodeWithText(texto(R.string.cuenta_contrasena_explicacion)).assertIsDisplayed()
        campo(R.string.cuenta_contrasena_nueva).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_contrasena_guardar)).assertIsDisplayed()
    }

    @Test
    fun debeMarcarLaContrasena_cuandoTieneMenosDeOchoCaracteres() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        pulsar(R.string.cuenta_cambiar_contrasena)

        campo(R.string.cuenta_contrasena_nueva).performTextInput("corta")
        pulsar(R.string.cuenta_contrasena_guardar)

        composeTestRule.onNodeWithText(texto(R.string.validacion_contrasena_corta)).assertIsDisplayed()
    }

    @Test
    fun debeVolverAlResumenConElAviso_cuandoSeCambiaLaContrasena() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        pulsar(R.string.cuenta_cambiar_contrasena)

        campo(R.string.cuenta_contrasena_nueva).performTextInput("nueva-secreta")
        pulsar(R.string.cuenta_contrasena_guardar)

        composeTestRule.onNodeWithText(texto(R.string.cuenta_aviso_contrasena)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_editar_datos)).assertIsDisplayed()
    }

    // ---- cerrar sesion ----------------------------------------------------

    @Test
    fun debePedirConfirmacionYNoCerrar_cuandoSeCancela() {
        val cliente = entrarComo(RolUsuario.CLIENTE)
        montarPantalla()

        pulsar(R.string.cuenta_cerrar_sesion)
        composeTestRule.onNodeWithText(texto(R.string.cuenta_cerrar_pregunta)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_cancelar)).performClick()

        composeTestRule.onNodeWithText(texto(R.string.cuenta_cerrar_pregunta)).assertDoesNotExist()
        assertEquals(cliente.id, fuente.obtenerUsuarioActivoId())
    }

    @Test
    fun debeCerrarLaSesion_cuandoSeConfirma() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla()

        pulsar(R.string.cuenta_cerrar_sesion)
        // El boton de la pantalla y el del dialogo dicen lo mismo: el del
        // dialogo es el ultimo.
        composeTestRule.onAllNodesWithText(texto(R.string.cuenta_cerrar_sesion))[1].performClick()
        composeTestRule.waitForIdle()

        assertNull(fuente.obtenerUsuarioActivoId())
    }

    @Test
    fun debeMostrarElErrorYSeguirEnLaPantalla_cuandoElCierreFalla() {
        entrarComo(RolUsuario.CLIENTE)
        montarPantalla()
        repositorioAuth.errorForzado = TipoError.RED

        pulsar(R.string.cuenta_cerrar_sesion)
        composeTestRule.onAllNodesWithText(texto(R.string.cuenta_cerrar_sesion))[1].performClick()

        composeTestRule.onNodeWithText(texto(R.string.error_titulo_red)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.cuenta_editar_datos)).assertExists()
    }
}
