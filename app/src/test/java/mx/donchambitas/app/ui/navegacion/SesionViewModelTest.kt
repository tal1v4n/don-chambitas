package mx.donchambitas.app.ui.navegacion

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()

    private fun responderCon(sesiones: Flow<Sesion?>) {
        every { repositorio.sesionActual() } returns sesiones
    }

    private fun TestScope.avisosDe(viewModel: SesionViewModel): List<Unit> {
        val avisos = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.sinSesion.toList(avisos) }
        runCurrent()
        return avisos
    }

    @Test
    fun debeAvisar_cuandoLaSesionAbiertaTermina() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(DatosPrueba.crearSesion())
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio))
        assertEquals(0, avisos.size)

        sesiones.value = null
        runCurrent()

        assertEquals(1, avisos.size)
    }

    @Test
    fun debeAvisar_cuandoNoHaySesionAlArrancar() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(MutableStateFlow(null))

        val avisos = avisosDe(SesionViewModel(repositorio))

        assertEquals(1, avisos.size)
    }

    @Test
    fun noDebeAvisar_cuandoSeAbreUnaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val avisos = avisosDe(SesionViewModel(repositorio))

        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(1, avisos.size)
    }

    private fun TestScope.recuperacionesDe(viewModel: SesionViewModel): List<RolUsuario> {
        val roles = mutableListOf<RolUsuario>()
        backgroundScope.launch { viewModel.recuperacion.toList(roles) }
        runCurrent()
        return roles
    }

    @Test
    fun debeAvisarConElRol_cuandoLlegaPorRecuperacionYLaSesionYaEsta() = runTest(reglaCorrutinas.testDispatcher) {
        val sesion = DatosPrueba.crearSesion()
        responderCon(MutableStateFlow(sesion))
        val viewModel = SesionViewModel(repositorio)
        val roles = recuperacionesDe(viewModel)

        viewModel.alLlegarPorRecuperacion()
        runCurrent()

        assertEquals(listOf(sesion.usuario.rol), roles)
    }

    @Test
    fun debeEsperarALaSesion_cuandoLlegaPorRecuperacionAntesDeQueSeImporte() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio)
        val roles = recuperacionesDe(viewModel)

        viewModel.alLlegarPorRecuperacion()
        runCurrent()
        assertEquals(0, roles.size)

        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(1, roles.size)
    }

    @Test
    fun noDebeAvisarDeRecuperacion_cuandoLaSesionNoLlegaEnElTope() = runTest(reglaCorrutinas.testDispatcher) {
        val sesiones = MutableStateFlow<Sesion?>(null)
        responderCon(sesiones)
        val viewModel = SesionViewModel(repositorio)
        val roles = recuperacionesDe(viewModel)

        viewModel.alLlegarPorRecuperacion()
        advanceTimeBy(ESPERA_MAXIMA_SESION_MS + 1)
        sesiones.value = DatosPrueba.crearSesion()
        runCurrent()

        assertEquals(0, roles.size)
    }

    @Test
    fun noDebeAvisarDeRecuperacionNiLanzar_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })
        val viewModel = SesionViewModel(repositorio)
        val roles = recuperacionesDe(viewModel)

        viewModel.alLlegarPorRecuperacion()
        runCurrent()

        assertEquals(0, roles.size)
    }

    @Test
    fun noDebeAvisarNiLanzar_cuandoElFlujoDeSesionLanza() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(flow { throw IllegalStateException("faltan las llaves de Supabase") })

        val avisos = avisosDe(SesionViewModel(repositorio))

        assertEquals(0, avisos.size)
    }
}
