package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.ui.tema.DonChambitasTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la pantalla Mis servicios (P-12).
 * Cubren el resumen, la tarjeta pausada y el precio de la tabla 3.4, la hoja de
 * acciones, la confirmacion de eliminar, el vacio y los accesos. El orden de la
 * lista se prueba sin emulador en EstadoMisServiciosTest.
 */
@RunWith(AndroidJUnit4::class)
class MisServiciosPantallaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun texto(@StringRes id: Int, vararg argumentos: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *argumentos)

    private fun servicio(id: String, titulo: String, desde: Int?, hasta: Int?, unidad: String? = null, activo: Boolean = true) =
        Servicio(
            id = id,
            perfilId = "usr-trab-1",
            categoriaId = 1,
            titulo = titulo,
            descripcion = "",
            precioDesde = desde?.let(::BigDecimal),
            precioHasta = hasta?.let(::BigDecimal),
            unidadPrecio = unidad,
            activo = activo,
            creadoEn = Instant.EPOCH,
            actualizadoEn = Instant.EPOCH
        )

    private val fugas = servicio("s1", "Reparación de fugas", 300, 800, "por trabajo")
    private val boiler = servicio("s2", "Instalación de boiler", 1200, null)
    private val drenaje = servicio("s3", "Destape de drenaje", null, null, activo = false)

    private fun conServicios(vararg servicios: Servicio) = EstadoMisServicios(
        cargando = false,
        servicios = servicios.toList(),
        categorias = mapOf(1 to "Plomería")
    )

    /** Lo que la pantalla pidio, para comprobar cada callback sin ViewModel. */
    private class Eventos {
        var acciones: Servicio? = null
        var editado: Servicio? = null
        var cambiado: Servicio? = null
        var porEliminar: Servicio? = null
        var eliminarConfirmado = 0
        var publicar = 0
        var miPerfil = 0
        var postulaciones = 0
    }

    private fun montar(estado: EstadoMisServicios): Eventos {
        val eventos = Eventos()
        composeTestRule.setContent {
            DonChambitasTema {
                MisServiciosContenido(
                    estado = estado,
                    alAbrirAcciones = { eventos.acciones = it },
                    alCerrarAcciones = {},
                    alEditar = { eventos.editado = it },
                    alCambiarActivo = { eventos.cambiado = it },
                    alPedirEliminar = { eventos.porEliminar = it },
                    alConfirmarEliminar = { eventos.eliminarConfirmado++ },
                    alCancelarEliminar = {},
                    alPublicar = { eventos.publicar++ },
                    alIrAMiPerfil = { eventos.miPerfil++ },
                    alIrAMisPostulaciones = { eventos.postulaciones++ }
                )
            }
        }
        return eventos
    }

    private fun boton(@StringRes id: Int) =
        composeTestRule.onNode(hasText(texto(id)) and hasClickAction())

    @Test
    fun resumen_cuentaServiciosYPausados() {
        montar(conServicios(fugas, boiler, drenaje))
        composeTestRule.onNodeWithText("3 servicios · 1 pausado").assertIsDisplayed()
    }

    @Test
    fun resumen_sinPausados_soloCuentaServicios() {
        montar(conServicios(fugas, boiler))
        composeTestRule.onNodeWithText("2 servicios").assertIsDisplayed()
    }

    @Test
    fun tarjetas_escribenElPrecioComoLaTabla34YMarcanElPausado() {
        montar(conServicios(fugas, boiler, drenaje))

        composeTestRule.onNodeWithText("$300 – $800 por trabajo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desde $1,200").assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.tarjeta_servicio_categoria_pausado, "Plomería")).assertIsDisplayed()
    }

    @Test
    fun tarjeta_alTocarla_abreSusAcciones() {
        val eventos = montar(conServicios(fugas))
        composeTestRule.onNodeWithText("Reparación de fugas").performClick()
        assertEquals("s1", eventos.acciones?.id)
    }

    @Test
    fun tarjeta_procesandose_noResponde() {
        val eventos = montar(conServicios(fugas).copy(procesandoId = "s1"))
        composeTestRule.onNodeWithText("Reparación de fugas").performClick()
        assertNull(eventos.acciones)
    }

    @Test
    fun hojaDeUnActivo_ofrecePausarYEditar() {
        val eventos = montar(conServicios(fugas).copy(servicioEnAcciones = fugas))

        boton(R.string.mis_servicios_pausar).assertIsDisplayed().performClick()
        assertEquals("s1", eventos.cambiado?.id)
    }

    @Test
    fun hojaDeUnPausado_ofreceReactivar() {
        val eventos = montar(conServicios(drenaje).copy(servicioEnAcciones = drenaje))

        boton(R.string.mis_servicios_reactivar).assertIsDisplayed()
        boton(R.string.mis_servicios_editar).performClick()
        assertEquals("s3", eventos.editado?.id)
    }

    @Test
    fun hoja_eliminar_pideConfirmacion() {
        val eventos = montar(conServicios(fugas).copy(servicioEnAcciones = fugas))
        boton(R.string.mis_servicios_eliminar).performClick()
        assertEquals("s1", eventos.porEliminar?.id)
    }

    @Test
    fun confirmacionDeEliminar_nombraElServicio() {
        val eventos = montar(conServicios(fugas).copy(servicioPorEliminar = fugas))

        composeTestRule.onNodeWithText(texto(R.string.mis_servicios_eliminar_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mis_servicios_eliminar_texto, "Reparación de fugas")).assertIsDisplayed()
        boton(R.string.mis_servicios_eliminar).performClick()

        assertEquals(1, eventos.eliminarConfirmado)
    }

    @Test
    fun conServicios_muestraElBotonFlotante() {
        val eventos = montar(conServicios(fugas))
        boton(R.string.mis_servicios_publicar).performClick()
        assertEquals(1, eventos.publicar)
    }

    @Test
    fun vacia_invitaAPublicarSinBotonFlotante() {
        val eventos = montar(EstadoMisServicios(cargando = false))

        composeTestRule.onNodeWithText(texto(R.string.mis_servicios_vacio_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(texto(R.string.mis_servicios_publicar)).assertDoesNotExist()
        boton(R.string.mis_servicios_vacio_accion).performClick()

        assertEquals(1, eventos.publicar)
    }

    @Test
    fun accesos_llevanAMiPerfilYAMisPostulaciones() {
        val eventos = montar(EstadoMisServicios(cargando = false))

        boton(R.string.mi_cuenta_mi_perfil).performClick()
        boton(R.string.mi_cuenta_mis_postulaciones).performClick()

        assertEquals(1, eventos.miPerfil)
        assertEquals(1, eventos.postulaciones)
    }
}
