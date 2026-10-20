package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MiPerfilViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val fuente = FuenteDatosFalsa().apply {
        fijarSesionActiva(Sesion(usuarios.first { it.id == ID_TRABAJADOR }))
    }
    private val repositorioTrabajador = RepositorioTrabajadorFalso(fuente).apply { retrasoMs = 0 }
    private val repositorioCatalogos = RepositorioCatalogosFalso(fuente).apply { retrasoMs = 0 }

    private fun crearViewModel(trabajador: RepositorioTrabajador = repositorioTrabajador) =
        MiPerfilViewModel(trabajador, repositorioCatalogos)

    @Test
    fun debeLlenarElFormulario_cuandoYaHayPerfil() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        assertTrue(viewModel.estado.value.cargando)

        advanceUntilIdle()

        val estado = viewModel.estado.value
        val perfil = fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR }
        assertFalse(estado.cargando)
        assertTrue(estado.perfilGuardado)
        assertEquals(ID_TRABAJADOR, estado.usuarioId)
        assertEquals(perfil.titulo, estado.titulo)
        assertEquals("10", estado.experienciaAnios)
        assertEquals(listOf("Boilers", "Cobre", "Drenajes", "Termofusion"), estado.habilidades)
        assertEquals(perfil.municipioId, estado.municipioId)
        assertTrue(estado.municipios.isNotEmpty())
        assertTrue(estado.municipios.all { it.estadoId == perfil.estadoId })
        assertTrue(estado.faltantes.isEmpty())
        assertEquals(EstadoBotonIa.Disponible(null), estado.ia)
    }

    @Test
    fun debeArrancarEnBlanco_cuandoNoHayPerfil() = runTest(reglaCorrutinas.testDispatcher) {
        fuente.perfilesTrabajador.removeAll { it.usuarioId == ID_TRABAJADOR }
        fuente.habilidades.removeAll { it.perfilId == ID_TRABAJADOR }

        val viewModel = crearViewModel()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.perfilGuardado)
        assertEquals("", estado.titulo)
        assertTrue(estado.estados.isNotEmpty())
        assertTrue(estado.municipios.isEmpty())
        assertEquals(FaltaPerfil.entries, estado.faltantes)
        assertEquals(EstadoBotonIa.FaltanDatos, estado.ia)
    }

    @Test
    fun debeOfrecerReintentar_cuandoLaCargaFalla() = runTest(reglaCorrutinas.testDispatcher) {
        repositorioTrabajador.errorForzado = TipoError.RED
        val viewModel = crearViewModel()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorCarga)

        repositorioTrabajador.errorForzado = null
        viewModel.alReintentarCarga()
        advanceUntilIdle()

        assertNull(viewModel.estado.value.errorCarga)
        assertTrue(viewModel.estado.value.perfilGuardado)
    }

    @Test
    fun debeBorrarElMunicipioYTraerLosDelEstadoNuevo_cuandoCambiaElEstado() =
        runTest(reglaCorrutinas.testDispatcher) {
            val viewModel = crearViewModel()
            advanceUntilIdle()
            val otroEstado = fuente.estados.first { it.clave == "JAL" }.id

            viewModel.alElegirEstado(otroEstado)
            assertNull(viewModel.estado.value.municipioId)
            advanceUntilIdle()

            val municipios = viewModel.estado.value.municipios
            assertTrue(municipios.isNotEmpty())
            assertTrue(municipios.all { it.estadoId == otroEstado })
        }

    @Test
    fun debeHabilitarLaIa_soloConTitulo() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarTitulo("  ")
        assertEquals(EstadoBotonIa.FaltanDatos, viewModel.estado.value.ia)

        viewModel.alCambiarTitulo("Plomero")
        assertEquals(EstadoBotonIa.Disponible(null), viewModel.estado.value.ia)
    }

    @Test
    fun debeGuardarYRecalcularElAviso_cuandoGuardaBien() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.alCambiarDescripcion("   ")
        // El aviso es sobre lo guardado: escribir no lo mueve (2.5).
        assertTrue(viewModel.estado.value.faltantes.isEmpty())
        viewModel.alQuitarHabilidad("Cobre")
        viewModel.alGuardar()
        assertTrue(viewModel.estado.value.guardando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.guardando)
        assertEquals(R.string.mi_perfil_guardado, estado.aviso)
        assertEquals(listOf(FaltaPerfil.DESCRIPCION), estado.faltantes)
        assertNull(fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR }.descripcion)
        assertFalse(fuente.habilidades.any { it.perfilId == ID_TRABAJADOR && it.habilidad == "Cobre" })
    }

    @Test
    fun debeMostrarElErrorEnLinea_cuandoElPerfilNoSeGuarda() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = crearViewModel()
        advanceUntilIdle()

        repositorioTrabajador.errorForzado = TipoError.SERVIDOR
        viewModel.alGuardar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertFalse(estado.guardando)
        assertEquals(TipoError.SERVIDOR, estado.errorPantalla)
        assertFalse(estado.soloFaltanHabilidades)
        assertNull(estado.aviso)
    }

    @Test
    fun noDebeReemplazarHabilidades_cuandoNoCambiaron() = runTest(reglaCorrutinas.testDispatcher) {
        val trabajador = trabajadorSimulado()
        val viewModel = crearViewModel(trabajador)
        advanceUntilIdle()

        viewModel.alCambiarTitulo("Plomero certificado")
        viewModel.alGuardar()
        advanceUntilIdle()

        coVerify(exactly = 1) { trabajador.guardarMiPerfil(any()) }
        coVerify(exactly = 0) { trabajador.reemplazarHabilidades(any()) }
        assertEquals(R.string.mi_perfil_guardado, viewModel.estado.value.aviso)
    }

    @Test
    fun debeReintentarSoloLasHabilidades_cuandoElPerfilSeGuardoYEllasNo() =
        runTest(reglaCorrutinas.testDispatcher) {
            val trabajador = trabajadorSimulado()
            coEvery { trabajador.reemplazarHabilidades(any()) } returns Resultado.Error(TipoError.RED, "sin red")
            val viewModel = crearViewModel(trabajador)
            advanceUntilIdle()

            viewModel.alCambiarHabilidadNueva("Tinacos")
            viewModel.alAgregarHabilidad()
            viewModel.alGuardar()
            advanceUntilIdle()

            assertTrue(viewModel.estado.value.soloFaltanHabilidades)
            assertEquals(TipoError.RED, viewModel.estado.value.errorPantalla)
            assertTrue(viewModel.estado.value.perfilGuardado)

            coEvery { trabajador.reemplazarHabilidades(any()) } returns Resultado.Exito(emptyList())
            viewModel.alReintentarGuardado()
            advanceUntilIdle()

            coVerify(exactly = 1) { trabajador.guardarMiPerfil(any()) }
            coVerify(exactly = 2) { trabajador.reemplazarHabilidades(listOf("Cobre", "Tinacos")) }
            assertFalse(viewModel.estado.value.soloFaltanHabilidades)
            assertNull(viewModel.estado.value.errorPantalla)
            assertEquals(R.string.mi_perfil_guardado, viewModel.estado.value.aviso)
        }

    @Test
    fun calcularFaltantes_listaEnElOrdenDelAviso() {
        val completo = PerfilTrabajador(
            usuarioId = ID_TRABAJADOR,
            titulo = "Plomero",
            descripcion = "Reparo fugas",
            telefonoContacto = "2225550101",
            estadoId = 1,
            municipioId = 2,
            creadoEn = Instant.EPOCH,
            actualizadoEn = Instant.EPOCH
        )

        assertEquals(FaltaPerfil.entries, calcularFaltantes(null, emptyList()))
        assertTrue(calcularFaltantes(completo, listOf("Cobre")).isEmpty())
        assertEquals(
            listOf(FaltaPerfil.HABILIDAD, FaltaPerfil.UBICACION, FaltaPerfil.TELEFONO),
            calcularFaltantes(completo.copy(municipioId = null, telefonoContacto = ""), emptyList())
        )
    }

    /** Un perfil con una habilidad, para contar llamadas sin depender de la fuente falsa. */
    private fun trabajadorSimulado(): RepositorioTrabajador {
        val perfil = fuente.perfilesTrabajador.first { it.usuarioId == ID_TRABAJADOR }
        return mockk {
            coEvery { obtenerMiPerfil() } returns Resultado.Exito(perfil)
            coEvery { obtenerMisHabilidades() } returns Resultado.Exito(listOf("Cobre"))
            coEvery { guardarMiPerfil(any()) } answers { Resultado.Exito(firstArg()) }
            coEvery { reemplazarHabilidades(any()) } returns Resultado.Exito(emptyList())
        }
    }

    private companion object {
        const val ID_TRABAJADOR = "usr-trab-1"
    }
}
