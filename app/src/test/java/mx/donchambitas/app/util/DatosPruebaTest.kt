package mx.donchambitas.app.util

import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.RolUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias para validar que DatosPrueba y ReglaCorrutinas funcionan correctamente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DatosPruebaTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    @Test
    fun debeConfigurarDispatcherPrincipal_cuandoSeAplicaReglaCorrutinas() = runTest(reglaCorrutinas.testDispatcher) {
        var ejecutado = false
        launch(Dispatchers.Main) {
            ejecutado = true
        }
        advanceUntilIdle()
        assertTrue("La corrutina lanzada en Dispatchers.Main debio ejecutarse", ejecutado)
    }

    @Test
    fun debeCrearUsuarioPorDefectoYPersonalizado_cuandoSeLlamaFabrica() {
        val usuarioDefecto = DatosPrueba.crearUsuario()
        assertEquals("usr-prueba-001", usuarioDefecto.id)
        assertEquals(RolUsuario.CLIENTE, usuarioDefecto.rol)
        assertTrue(usuarioDefecto.activo)

        val usuarioPersonalizado = DatosPrueba.crearUsuario(
            id = "usr-custom",
            nombre = "María",
            rol = RolUsuario.TRABAJADOR
        )
        assertEquals("usr-custom", usuarioPersonalizado.id)
        assertEquals("María", usuarioPersonalizado.nombre)
        assertEquals(RolUsuario.TRABAJADOR, usuarioPersonalizado.rol)
    }

    @Test
    fun debeCrearPerfilTrabajadorYServicio_cuandoSeLlamaFabrica() {
        val perfil = DatosPrueba.crearPerfilTrabajador(titulo = "Electricista residencial")
        assertEquals("usr-trab-001", perfil.usuarioId)
        assertEquals("Electricista residencial", perfil.titulo)
        assertEquals(10, perfil.experienciaAnios)

        val servicio = DatosPrueba.crearServicio(
            precioDesde = BigDecimal("400.00")
        )
        assertEquals("srv-prueba-001", servicio.id)
        assertEquals(BigDecimal("400.00"), servicio.precioDesde)
    }

    @Test
    fun debeCrearSolicitudYPostulacion_cuandoSeLlamaFabrica() {
        val solicitud = DatosPrueba.crearSolicitud(
            estatus = EstadoSolicitud.ABIERTA
        )
        assertEquals(EstadoSolicitud.ABIERTA, solicitud.estatus)
        assertNull(solicitud.trabajadorId)

        val postulacion = DatosPrueba.crearPostulacion(
            precioPropuesto = BigDecimal("550.00"),
            estatus = EstadoPostulacion.ENVIADA
        )
        assertEquals(BigDecimal("550.00"), postulacion.precioPropuesto)
        assertEquals(EstadoPostulacion.ENVIADA, postulacion.estatus)
    }

    @Test
    fun debeCrearConversacionMensajeYResena_cuandoSeLlamaFabrica() {
        val conversacion = DatosPrueba.crearConversacion()
        assertEquals("conv-prueba-001", conversacion.id)

        val mensaje = DatosPrueba.crearMensaje(contenido = "Mensaje de prueba")
        assertEquals("Mensaje de prueba", mensaje.contenido)
        assertNull(mensaje.leidoEn)

        val resena = DatosPrueba.crearResena(calificacion = 4)
        assertEquals(4, resena.calificacion)
    }

    @Test
    fun debeCrearCatalogosYSesion_cuandoSeLlamaFabrica() {
        val cat = DatosPrueba.crearCategoria(nombre = "Carpintería")
        assertEquals("Carpintería", cat.nombre)

        val edo = DatosPrueba.crearEstado()
        assertEquals("CDMX", edo.clave)

        val mun = DatosPrueba.crearMunicipio()
        assertEquals("Cuauhtémoc", mun.nombre)

        val sesion = DatosPrueba.crearSesion()
        assertNotNull(sesion.usuario)
    }
}
