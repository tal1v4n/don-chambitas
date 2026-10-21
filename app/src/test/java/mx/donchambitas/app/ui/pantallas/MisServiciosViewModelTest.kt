package mx.donchambitas.app.ui.pantallas

import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MisServiciosViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_TRABAJADOR }))
        // Tres servicios: el sembrado (serv-1), uno mas reciente y uno pausado.
        servicios += servicioDe("serv-nuevo", actualizado = Instant.now())
        servicios += servicioDe("serv-pausado", actualizado = Instant.now(), activo = false)
    }
    private val repositorioServicios = RepositorioServiciosFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel() = MisServiciosViewModel(repositorioServicios, repositorioCatalogos)

    @Test
    fun debeEsperarAlPrimerResume_yCargarOrdenado() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.estado.value.cargando)

        viewModel.alCargar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertEquals(listOf("serv-nuevo", "serv-1", "serv-pausado"), estado.servicios.map { it.id })
        assertEquals("Plomería", estado.categorias[1])
        assertEquals(1, estado.pausados)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaPrimeraCargaFalla() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioServicios.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.error)

        repositorioServicios.errorForzado = null
        viewModel.alCargar()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.error)
        assertEquals(3, viewModel.estado.value.servicios.size)
    }

    @Test
    fun debeConservarLaListaYAvisar_cuandoFallaUnaRecarga() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()

        repositorioServicios.errorForzado = TipoError.SERVIDOR
        viewModel.alCargar()
        assertFalse(viewModel.estado.value.cargando)
        advanceUntilIdle()

        assertNull(viewModel.estado.value.error)
        assertEquals(3, viewModel.estado.value.servicios.size)
        assertEquals(obtenerMensajeErrorRes(TipoError.SERVIDOR), viewModel.estado.value.aviso)
    }

    @Test
    fun debeMostrarLoRecienPublicado_alVolverDeP13() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()

        fuente.servicios += servicioDe("serv-otro", actualizado = Instant.now().plusSeconds(60))
        viewModel.alCargar()
        advanceUntilIdle()

        assertEquals("serv-otro", viewModel.estado.value.servicios.first().id)
    }

    @Test
    fun debeAvisarCambiosGuardados_soloCuandoSeVuelveDeGuardar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()

        viewModel.alCargar(cambiosGuardados = true)
        advanceUntilIdle()
        assertEquals(R.string.servicio_cambios_guardados, viewModel.estado.value.aviso)

        viewModel.alConsumirAviso()
        viewModel.alCargar()
        advanceUntilIdle()
        assertNull(viewModel.estado.value.aviso)
    }

    @Test
    fun debePausarYReactivar_moviendoLaTarjetaDeGrupo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()
        val nuevo = viewModel.estado.value.servicios.first { it.id == "serv-nuevo" }

        viewModel.alAbrirAcciones(nuevo)
        viewModel.alCambiarActivo(nuevo)
        assertNull(viewModel.estado.value.servicioEnAcciones)
        assertEquals("serv-nuevo", viewModel.estado.value.procesandoId)
        advanceUntilIdle()

        var estado = viewModel.estado.value
        assertNull(estado.procesandoId)
        assertEquals(R.string.mis_servicios_aviso_pausado, estado.aviso)
        assertEquals("serv-1", estado.servicios.first().id)
        assertEquals(2, estado.pausados)

        viewModel.alCambiarActivo(estado.servicios.first { it.id == "serv-nuevo" })
        advanceUntilIdle()

        estado = viewModel.estado.value
        assertEquals(R.string.mis_servicios_aviso_reactivado, estado.aviso)
        assertEquals("serv-nuevo", estado.servicios.first().id)
    }

    @Test
    fun debeDejarLaListaIgual_cuandoPausarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()
        val antes = viewModel.estado.value.servicios

        repositorioServicios.errorForzado = TipoError.RED
        viewModel.alCambiarActivo(antes.first())
        advanceUntilIdle()

        assertEquals(antes, viewModel.estado.value.servicios)
        assertNull(viewModel.estado.value.procesandoId)
        assertEquals(obtenerMensajeErrorRes(TipoError.RED), viewModel.estado.value.aviso)
    }

    @Test
    fun debeEliminarSoloTrasConfirmar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()
        val pausado = viewModel.estado.value.servicios.first { it.id == "serv-pausado" }

        viewModel.alPedirEliminar(pausado)
        assertEquals(pausado, viewModel.estado.value.servicioPorEliminar)
        viewModel.alCancelarEliminar()
        advanceUntilIdle()
        assertEquals(3, viewModel.estado.value.servicios.size)

        viewModel.alPedirEliminar(pausado)
        viewModel.alConfirmarEliminar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.servicioPorEliminar)
        assertEquals(listOf("serv-nuevo", "serv-1"), estado.servicios.map { it.id })
        assertEquals(R.string.mis_servicios_aviso_eliminado, estado.aviso)
        assertFalse(fuente.servicios.any { it.id == "serv-pausado" })
    }

    @Test
    fun noDebeAbrirAcciones_deLaTarjetaQueSeProcesa() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        viewModel.alCargar()
        advanceUntilIdle()
        val primero = viewModel.estado.value.servicios.first()

        viewModel.alCambiarActivo(primero)
        viewModel.alAbrirAcciones(primero)

        assertNull(viewModel.estado.value.servicioEnAcciones)
    }

    private fun servicioDe(id: String, actualizado: Instant, activo: Boolean = true) = Servicio(
        id = id,
        perfilId = ID_TRABAJADOR,
        categoriaId = 1,
        titulo = "Servicio $id",
        descripcion = "Descripcion de $id",
        activo = activo,
        creadoEn = actualizado,
        actualizadoEn = actualizado
    )

    private companion object {
        const val ID_TRABAJADOR = "usr-trab-1"
    }
}
