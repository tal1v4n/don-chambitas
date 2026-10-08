package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
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

class RecuperarContrasenaViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()
    private lateinit var viewModel: RecuperarContrasenaViewModel

    @Before
    fun preparar() {
        viewModel = RecuperarContrasenaViewModel(repositorio)
    }

    private fun responderCon(resultado: Resultado<Unit>) {
        coEvery { repositorio.recuperarContrasena(any()) } returns resultado
    }

    @Test
    fun debeEnviarElCorreoEnMinusculasYSinEspacios_cuandoSePideElEnlace() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Unit))
        viewModel.alCambiarCorreo("  Refugio@Ejemplo.MX ")

        viewModel.alEnviar()
        advanceUntilIdle()

        coVerify { repositorio.recuperarContrasena("refugio@ejemplo.mx") }
        assertEquals("  Refugio@Ejemplo.MX ", viewModel.estado.value.correo)
    }

    @Test
    fun debePasarALaConfirmacion_cuandoElRepositorioResponde() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Unit))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")

        viewModel.alEnviar()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.enviado)
        assertFalse(estado.cargando)
        assertNull(estado.errorPantalla)
    }

    @Test
    fun debeMarcarElCorreoYNoLlamarAlRepositorio_cuandoSeEnviaVacio() = runTest(reglaCorrutinas.testDispatcher) {
        viewModel.alEnviar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(R.string.validacion_correo_vacio, estado.errorCorreo)
        assertFalse(estado.cargando)
        assertFalse(estado.enviado)
        coVerify(exactly = 0) { repositorio.recuperarContrasena(any()) }
    }

    @Test
    fun debeMarcarElFormatoYNoLlamarAlRepositorio_cuandoSeEnviaMalEscrito() = runTest(reglaCorrutinas.testDispatcher) {
        viewModel.alCambiarCorreo("refugio@")

        viewModel.alEnviar()
        advanceUntilIdle()

        assertEquals(R.string.validacion_correo_formato, viewModel.estado.value.errorCorreo)
        coVerify(exactly = 0) { repositorio.recuperarContrasena(any()) }
    }

    @Test
    fun debeQuedarseEnElFormularioConElCorreo_cuandoNoHayRed() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.RED, "sin red"))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")

        viewModel.alEnviar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorPantalla)
        assertFalse(estado.enviado)
        assertFalse(estado.cargando)
        assertEquals("refugio@ejemplo.mx", estado.correo)
    }

    @Test
    fun debeQuedarseEnElFormulario_cuandoElServidorFalla() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.SERVIDOR, "caido"))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")

        viewModel.alEnviar()
        advanceUntilIdle()

        assertEquals(TipoError.SERVIDOR, viewModel.estado.value.errorPantalla)
        assertFalse(viewModel.estado.value.enviado)
    }

    @Test
    fun debeQuedarseEnElFormulario_cuandoElErrorEsDesconocido() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.DESCONOCIDO, "?"))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")

        viewModel.alEnviar()
        advanceUntilIdle()

        assertEquals(TipoError.DESCONOCIDO, viewModel.estado.value.errorPantalla)
        assertFalse(viewModel.estado.value.enviado)
    }

    @Test
    fun debeVolverALlamarAlRepositorio_cuandoSeReintentaTrasErrorDeRed() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.RED, "sin red"))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")
        viewModel.alEnviar()
        advanceUntilIdle()

        responderCon(Resultado.Exito(Unit))
        viewModel.alReintentar()
        advanceUntilIdle()

        coVerify(exactly = 2) { repositorio.recuperarContrasena("refugio@ejemplo.mx") }
        assertNull(viewModel.estado.value.errorPantalla)
        assertTrue(viewModel.estado.value.enviado)
    }

    @Test
    fun debeLimpiarElErrorDePantalla_cuandoSeVuelveAPulsarElBoton() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.SERVIDOR, "caido"))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")
        viewModel.alEnviar()
        advanceUntilIdle()

        viewModel.alEnviar()

        assertNull(viewModel.estado.value.errorPantalla)
        assertTrue(viewModel.estado.value.cargando)
    }

    @Test
    fun debeLlamarUnaSolaVez_cuandoSePulsaDosVecesMientrasCarga() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Unit))
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")

        viewModel.alEnviar()
        viewModel.alEnviar()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.recuperarContrasena(any()) }
    }

    @Test
    fun debeMarcarElCorreo_cuandoPierdeElFocoConFormatoQueNoSirve() {
        viewModel.alCambiarCorreo("hola")

        viewModel.alPerderFocoCorreo()

        assertEquals(R.string.validacion_correo_formato, viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeNoMarcarNada_cuandoPierdeElFocoSinHaberloTocado() {
        viewModel.alPerderFocoCorreo()

        assertNull(viewModel.estado.value.errorCorreo)
    }

    @Test
    fun debeLimpiarElErrorDelCorreo_cuandoSeVuelveAEscribir() {
        viewModel.alEnviar()
        assertEquals(R.string.validacion_correo_vacio, viewModel.estado.value.errorCorreo)

        viewModel.alCambiarCorreo("r")

        assertNull(viewModel.estado.value.errorCorreo)
    }
}
