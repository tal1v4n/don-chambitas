package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.SavedStateHandle
import java.math.BigDecimal
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServicioViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_TRABAJADOR }))
    }
    private val repositorioServicios = RepositorioServiciosFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel(servicioId: String? = null, estadoGuardado: SavedStateHandle = handle(servicioId)) =
        ServicioViewModel(repositorioServicios, repositorioCatalogos, estadoGuardado)

    private fun handle(servicioId: String?) =
        SavedStateHandle(mapOf(Ruta.CrearEditarServicio.ARG_SERVICIO_ID to servicioId))

    @Test
    fun debeArrancarEnBlancoConLasCategorias_alCrear() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertTrue(estado.creando)
        assertEquals(16, estado.categorias.size)
        assertEquals("", estado.titulo)
        assertEquals(EstadoBotonIa.FaltanDatos, estado.ia)
    }

    @Test
    fun debeHabilitarLaIa_conTituloYCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Instalación de boiler")
        assertEquals(EstadoBotonIa.FaltanDatos, viewModel.estado.value.ia)
        viewModel.alElegirCategoria(1)
        assertEquals(EstadoBotonIa.Disponible(null), viewModel.estado.value.ia)
    }

    @Test
    fun debeLlenarElFormulario_alEditar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.creando)
        assertFalse(estado.noExiste)
        assertEquals(1, estado.categoriaId)
        assertEquals("350", estado.precioDesde)
        assertEquals("800", estado.precioHasta)
        assertEquals("por trabajo", estado.unidadPrecio)
        assertEquals(EstadoBotonIa.Disponible(null), estado.ia)
        // H-18: las fotos que ya tenia no se pueden leer con su id.
        assertTrue(estado.fotos.isEmpty())
    }

    @Test
    fun debePintarNoExiste_cuandoElServicioYaNoEsta() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-borrado")
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.noExiste)
        assertNull(viewModel.estado.value.errorCarga)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaCargaFalla() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioCatalogos.errorForzado = TipoError.RED
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioCatalogos.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertEquals(1, viewModel.estado.value.categoriaId)
    }

    @Test
    fun debePasarAEditarSinNavegar_alPublicar() = runTest(reglaCorrutinas.testDispatcher) {
        val estadoGuardado = handle(null)
        val viewModel = crearViewModel(estadoGuardado = estadoGuardado)
        advanceUntilIdle()

        viewModel.alCambiarTitulo("  Instalación de boiler ")
        viewModel.alElegirCategoria(1)
        viewModel.alCambiarDescripcion("Instalo boilers de paso y de depósito.")
        viewModel.alCambiarPrecioDesde("350,5")
        viewModel.alCambiarUnidad("por trabajo")
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        val creado = fuente.servicios.first { it.id == estado.servicioId }
        assertFalse(estado.creando)
        assertFalse(estado.cambiosGuardados)
        assertEquals(R.string.servicio_publicado, estado.aviso)
        assertEquals(creado.id, estadoGuardado.get<String>(Ruta.CrearEditarServicio.ARG_SERVICIO_ID))
        assertEquals("Instalación de boiler", creado.titulo)
        assertEquals(BigDecimal("350.5"), creado.precioDesde)
        assertNull(creado.precioHasta)
        assertEquals(ID_TRABAJADOR, creado.perfilId)
    }

    @Test
    fun noDebeGuardar_sinCategoria() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        val antes = fuente.servicios.size

        viewModel.alCambiarTitulo("Instalación de boiler")
        viewModel.alGuardar()
        advanceUntilIdle()

        assertFalse(viewModel.estado.value.guardando)
        assertEquals(antes, fuente.servicios.size)
    }

    @Test
    fun noDebeMandarUnidad_sinPrecio() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarPrecioDesde("")
        viewModel.alCambiarPrecioHasta("")
        viewModel.alGuardar()
        advanceUntilIdle()

        val guardado = fuente.servicios.first { it.id == "serv-1" }
        assertNull(guardado.precioDesde)
        assertNull(guardado.unidadPrecio)
    }

    @Test
    fun debeAvisarQueRegresa_alGuardarCambios() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Fugas y grifería")
        viewModel.alGuardar()
        assertTrue(viewModel.estado.value.guardando)
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.cambiosGuardados)
        val guardado = fuente.servicios.first { it.id == "serv-1" }
        assertEquals("Fugas y grifería", guardado.titulo)
        assertTrue(guardado.activo)
    }

    @Test
    fun debeConservarLoEscrito_cuandoGuardarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Fugas y grifería")
        repositorioServicios.errorForzado = TipoError.SERVIDOR
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.guardando)
        assertFalse(estado.cambiosGuardados)
        assertEquals(TipoError.SERVIDOR, estado.errorPantalla)
        assertEquals("Fugas y grifería", estado.titulo)
    }

    @Test
    fun debeSubirEnLaPrimeraPosicionLibre() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        viewModel.alAgregarFoto(byteArrayOf(1))
        assertEquals(listOf(FotoEnPantalla.Subiendo(1)), viewModel.estado.value.fotos)
        assertTrue(viewModel.estado.value.subiendoFoto)
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(2))
        advanceUntilIdle()

        val fotos = viewModel.estado.value.fotos
        assertTrue(fotos.all { it is FotoEnPantalla.Guardada })
        assertEquals(listOf(1, 2), fotos.map { it.posicion })
        assertEquals(2, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun noDebeGuardar_mientrasSubeUnaFoto() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        repositorioServicios.retrasoMs = 1_000

        viewModel.alAgregarFoto(byteArrayOf(1))
        viewModel.alGuardar()

        assertFalse(viewModel.estado.value.guardando)
    }

    @Test
    fun debeQuitarLaMiniatura_alCancelarLaSubida() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        repositorioServicios.retrasoMs = 1_000
        viewModel.alCambiarTitulo("Sigue escrito")

        viewModel.alAgregarFoto(byteArrayOf(1))
        viewModel.alCancelarSubida(1)
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.fotos.isEmpty())
        assertEquals("Sigue escrito", viewModel.estado.value.titulo)
        assertEquals(0, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun debeAvisar_cuandoLaFotoNoSube() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()

        repositorioServicios.errorForzado = TipoError.RED
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.fotos.isEmpty())
        assertEquals(R.string.servicio_foto_no_subio, viewModel.estado.value.aviso)
    }

    @Test
    fun debeQuitarLaFoto_soloTrasConfirmar() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()
        val foto = viewModel.estado.value.fotos.single()

        viewModel.alPedirQuitarFoto(foto)
        assertEquals(foto, viewModel.estado.value.fotoPorQuitar)
        viewModel.alConfirmarQuitarFoto()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.fotoPorQuitar)
        assertTrue(viewModel.estado.value.fotos.isEmpty())
        assertEquals(0, fuente.fotosServicio.count { it.servicioId == "serv-1" })
    }

    @Test
    fun debeDejarLaFoto_cuandoQuitarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.fotosServicio.removeAll { it.servicioId == "serv-1" }
        val viewModel = crearViewModel("serv-1")
        advanceUntilIdle()
        viewModel.alAgregarFoto(byteArrayOf(1))
        advanceUntilIdle()
        val foto = viewModel.estado.value.fotos.single()

        repositorioServicios.errorForzado = TipoError.RED
        viewModel.alPedirQuitarFoto(foto)
        viewModel.alConfirmarQuitarFoto()
        advanceUntilIdle()

        assertEquals(listOf(foto), viewModel.estado.value.fotos)
        assertEquals(obtenerMensajeErrorRes(TipoError.RED), viewModel.estado.value.aviso)
    }

    private companion object {
        const val ID_TRABAJADOR = "usr-trab-1"
    }
}
