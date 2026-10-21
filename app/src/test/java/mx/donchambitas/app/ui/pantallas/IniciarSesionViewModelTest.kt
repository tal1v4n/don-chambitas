package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IniciarSesionViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()
    private lateinit var viewModel: IniciarSesionViewModel

    @Before
    fun preparar() {
        viewModel = IniciarSesionViewModel(repositorio)
    }

    private fun responderCon(resultado: Resultado<Sesion>) {
        coEvery { repositorio.iniciarSesion(any(), any()) } returns resultado
    }

    private fun sesionDe(rol: RolUsuario) = Sesion(DatosPrueba.crearUsuario(rol = rol))

    private fun llenarValido() {
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")
        viewModel.alCambiarContrasena("x")
    }

    @Test
    fun debeEnviarElCorreoEnMinusculasYSinEspacios_cuandoSeIniciaSesion() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(sesionDe(RolUsuario.CLIENTE)))
        viewModel.alCambiarCorreo("  Refugio@Ejemplo.MX ")
        viewModel.alCambiarContrasena(" clave ")

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        coVerify { repositorio.iniciarSesion("refugio@ejemplo.mx", " clave ") }
        assertEquals("  Refugio@Ejemplo.MX ", viewModel.estado.value.correo)
    }

    @Test
    fun debeMarcarLosDosCamposYNoLlamarAlRepositorio_cuandoSeEnviaVacio() = runTest(reglaCorrutinas.testDispatcher) {
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(R.string.validacion_correo_vacio, estado.errorCorreo)
        assertEquals(R.string.validacion_contrasena_vacia, estado.errorContrasena)
        assertFalse(estado.cargando)
        coVerify(exactly = 0) { repositorio.iniciarSesion(any(), any()) }
    }

    @Test
    fun debeIrAInicioCliente_cuandoLaSesionEsDeCliente() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(sesionDe(RolUsuario.CLIENTE)))
        llenarValido()

        viewModel.alIniciarSesion()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        assertFalse(viewModel.estado.value.cargando)
        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeIrAInicioTrabajador_cuandoLaSesionEsDeTrabajador() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(sesionDe(RolUsuario.TRABAJADOR)))
        llenarValido()

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debeConservarLoEscritoYNoNavegar_cuandoLasCredencialesSonRechazadas() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.AUTENTICACION, "Correo o contrasena incorrectos"))
        llenarValido()

        viewModel.alIniciarSesion()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.AUTENTICACION, estado.errorPantalla)
        assertEquals("refugio@ejemplo.mx", estado.correo)
        assertEquals("x", estado.contrasena)
        assertNull(estado.destino)
        assertFalse(estado.cargando)
    }

    @Test
    fun debeVolverALlamarAlRepositorio_cuandoSeReintentaTrasErrorDeRed() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.RED, "sin red"))
        llenarValido()
        viewModel.alIniciarSesion()
        advanceUntilIdle()
        assertEquals(TipoError.RED, viewModel.estado.value.errorPantalla)

        responderCon(Resultado.Exito(sesionDe(RolUsuario.CLIENTE)))
        viewModel.alReintentar()
        advanceUntilIdle()

        coVerify(exactly = 2) { repositorio.iniciarSesion(any(), any()) }
        assertNull(viewModel.estado.value.errorPantalla)
        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeLimpiarElErrorDePantalla_cuandoSeVuelveAPulsarElBoton() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.SERVIDOR, "caido"))
        llenarValido()
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        viewModel.alIniciarSesion()

        assertNull(viewModel.estado.value.errorPantalla)
        assertTrue(viewModel.estado.value.cargando)
    }

    @Test
    fun debeLlamarUnaSolaVez_cuandoSePulsaDosVecesMientrasCarga() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(sesionDe(RolUsuario.CLIENTE)))
        llenarValido()

        viewModel.alIniciarSesion()
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.iniciarSesion(any(), any()) }
    }

    @Test
    fun debeQuedarSinDestino_cuandoSeConsume() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(sesionDe(RolUsuario.CLIENTE)))
        llenarValido()
        viewModel.alIniciarSesion()
        advanceUntilIdle()

        viewModel.alConsumirDestino()

        assertNull(viewModel.estado.value.destino)
    }

    @Test
    fun debeMarcarElCorreo_cuandoPierdeElFocoConFormatoQueNoSirve() {
        viewModel.alCambiarCorreo("hola")

        viewModel.alPerderFoco(CampoIniciarSesion.CORREO)

        assertEquals(R.string.validacion_correo_formato, viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeNoMarcarNada_cuandoPierdeElFocoUnCampoQueNoSeToco() {
        viewModel.alPerderFoco(CampoIniciarSesion.CORREO)
        viewModel.alPerderFoco(CampoIniciarSesion.CONTRASENA)

        assertNull(viewModel.estado.value.errorCorreo)
        assertNull(viewModel.estado.value.errorContrasena)
    }

    @Test
    fun debeLimpiarElErrorDelCampo_cuandoSeVuelveAEscribir() {
        viewModel.alIniciarSesion()

        viewModel.alCambiarCorreo("r")

        assertNull(viewModel.estado.value.errorCorreo)
        assertEquals(R.string.validacion_contrasena_vacia, viewModel.estado.value.errorContrasena)
    }
}
