package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.ReglaCorrutinas
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MiCuentaViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorioUsuario = mockk<RepositorioUsuario>()
    private val repositorioAuth = mockk<RepositorioAuth>()
    private val usuario = DatosPrueba.crearUsuario()

    private fun fichaResponde(resultado: Resultado<Usuario> = Resultado.Exito(usuario)) {
        coEvery { repositorioUsuario.obtenerMiUsuario() } returns resultado
    }

    private fun crear() = MiCuentaViewModel(repositorioUsuario, repositorioAuth)

    // ---- carga ------------------------------------------------------------

    @Test
    fun debeEmpezarCargandoEnElResumen_cuandoSeAbre() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde()

        val estado = crear().estado.value

        assertTrue(estado.cargando)
        assertEquals(VistaMiCuenta.RESUMEN, estado.vista)
        assertNull(estado.usuario)
    }

    @Test
    fun debeMostrarLaFicha_cuandoElRepositorioLaDevuelve() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde()
        val viewModel = crear()

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(usuario, estado.usuario)
        assertFalse(estado.cargando)
        assertNull(estado.errorCarga)
    }

    @Test
    fun debeMostrarElError_cuandoLaFichaNoSePuedeLeer() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde(Resultado.Error(TipoError.RED, "sin red"))
        val viewModel = crear()

        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorCarga)
        assertNull(estado.usuario)
        assertFalse(estado.cargando)
    }

    @Test
    fun debeVolverACargarYMostrarLaFicha_cuandoSeReintenta() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde(Resultado.Error(TipoError.RED, "sin red"))
        val viewModel = crear()
        advanceUntilIdle()
        fichaResponde()

        viewModel.alReintentarCarga()
        assertTrue(viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.errorCarga)
        advanceUntilIdle()

        assertEquals(usuario, viewModel.estado.value.usuario)
        coVerify(exactly = 2) { repositorioUsuario.obtenerMiUsuario() }
    }

    // ---- editar datos -----------------------------------------------------

    private fun abrirEdicion(): MiCuentaViewModel {
        fichaResponde()
        return crear().also {
            reglaCorrutinas.testDispatcher.scheduler.advanceUntilIdle()
            it.alAbrirEditarDatos()
        }
    }

    @Test
    fun debePrecargarLosDatos_cuandoSeAbreLaEdicion() = runTest(reglaCorrutinas.testDispatcher) {
        val estado = abrirEdicion().estado.value

        assertEquals(VistaMiCuenta.EDITAR_DATOS, estado.vista)
        assertEquals(usuario.nombre, estado.nombre)
        assertEquals(usuario.apellidos, estado.apellidos)
        assertEquals(usuario.telefono, estado.telefono)
    }

    @Test
    fun noDebeAbrirLaEdicion_cuandoLaFichaTodaviaNoLlega() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde()
        val viewModel = crear()

        viewModel.alAbrirEditarDatos()

        assertEquals(VistaMiCuenta.RESUMEN, viewModel.estado.value.vista)
    }

    @Test
    fun debeGuardarRecortadoYVolverAlResumen_cuandoLosDatosSonValidos() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirEdicion()
        val guardado = usuario.copy(nombre = "Refugio", apellidos = "Luna", telefono = "4771234567")
        coEvery { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) } returns Resultado.Exito(guardado)
        viewModel.alCambiarNombre("  Refugio ")
        viewModel.alCambiarApellidos(" Luna  ")
        viewModel.alCambiarTelefono("477-123-4567")

        viewModel.alGuardarDatos()
        assertTrue(viewModel.estado.value.enviando)
        advanceUntilIdle()

        coVerify { repositorioUsuario.actualizarMiUsuario("Refugio", "Luna", "4771234567") }
        val estado = viewModel.estado.value
        assertEquals(VistaMiCuenta.RESUMEN, estado.vista)
        assertEquals(guardado, estado.usuario)
        assertEquals(AvisoMiCuenta.DATOS_GUARDADOS, estado.aviso)
        assertFalse(estado.enviando)
    }

    @Test
    fun debeMarcarTodosLosCamposYNoLlamarAlRepositorio_cuandoLosDatosNoSonValidos() =
        runTest(reglaCorrutinas.testDispatcher) {
            val viewModel = abrirEdicion()
            viewModel.alCambiarNombre("   ")
            viewModel.alCambiarApellidos("")
            viewModel.alCambiarTelefono("47712")

            viewModel.alGuardarDatos()
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(R.string.validacion_nombre_vacio, estado.errorNombre)
            assertEquals(R.string.validacion_apellidos_vacio, estado.errorApellidos)
            assertEquals(R.string.validacion_telefono_digitos, estado.errorTelefono)
            assertEquals(VistaMiCuenta.EDITAR_DATOS, estado.vista)
            coVerify(exactly = 0) { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) }
        }

    @Test
    fun debeExigirElTelefono_cuandoLaCuentaNoTieneUno() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde(Resultado.Exito(usuario.copy(telefono = null)))
        val viewModel = crear()
        advanceUntilIdle()
        viewModel.alAbrirEditarDatos()

        viewModel.alGuardarDatos()

        assertEquals(R.string.validacion_telefono_vacio, viewModel.estado.value.errorTelefono)
    }

    @Test
    fun debeValidarAlSalirDelCampo_soloCuandoYaSeEscribioEnEl() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirEdicion()

        viewModel.alPerderFoco(CampoMiCuenta.NOMBRE)
        assertNull(viewModel.estado.value.errorNombre)

        viewModel.alCambiarNombre("")
        viewModel.alPerderFoco(CampoMiCuenta.NOMBRE)
        assertEquals(R.string.validacion_nombre_vacio, viewModel.estado.value.errorNombre)

        viewModel.alCambiarNombre("R")
        assertNull(viewModel.estado.value.errorNombre)
    }

    @Test
    fun debeConservarLoEscritoYMostrarElError_cuandoGuardarFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirEdicion()
        coEvery { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) } returns
            Resultado.Error(TipoError.RED, "sin red")
        viewModel.alCambiarNombre("Refugio")

        viewModel.alGuardarDatos()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorFormulario)
        assertEquals("Refugio", estado.nombre)
        assertEquals(VistaMiCuenta.EDITAR_DATOS, estado.vista)
        assertEquals(usuario, estado.usuario)
        assertFalse(estado.enviando)
    }

    @Test
    fun debeRepetirElGuardado_cuandoSeReintentaEnLaEdicion() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirEdicion()
        coEvery { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) } returns
            Resultado.Error(TipoError.RED, "sin red")
        viewModel.alGuardarDatos()
        advanceUntilIdle()

        viewModel.alReintentarFormulario()
        advanceUntilIdle()

        coVerify(exactly = 2) { repositorioUsuario.actualizarMiUsuario(any(), any(), any()) }
    }

    @Test
    fun debeDescartarLoEscrito_cuandoSeVuelveAlResumenSinGuardar() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirEdicion()
        viewModel.alCambiarNombre("Otro nombre")

        viewModel.alVolverAlResumen()
        viewModel.alAbrirEditarDatos()

        assertEquals(usuario.nombre, viewModel.estado.value.nombre)
    }

    // ---- cambiar contrasena -----------------------------------------------

    private fun abrirContrasena(): MiCuentaViewModel {
        fichaResponde()
        return crear().also {
            reglaCorrutinas.testDispatcher.scheduler.advanceUntilIdle()
            it.alAbrirCambiarContrasena()
        }
    }

    @Test
    fun debeAbrirEnLaContrasena_cuandoSeLlegaPorRecuperacion() = runTest(reglaCorrutinas.testDispatcher) {
        fichaResponde()
        val viewModel = crear()

        viewModel.alLlegarPorRecuperacion()

        assertEquals(VistaMiCuenta.CAMBIAR_CONTRASENA, viewModel.estado.value.vista)
    }

    @Test
    fun debeCambiarLaContrasenaYVolverAlResumen_cuandoEsValida() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirContrasena()
        coEvery { repositorioAuth.cambiarContrasena(any()) } returns Resultado.Exito(Unit)
        viewModel.alEscribirContrasena("nueva-secreta")

        viewModel.alGuardarContrasena()
        assertTrue(viewModel.estado.value.enviando)
        advanceUntilIdle()

        coVerify { repositorioAuth.cambiarContrasena("nueva-secreta") }
        val estado = viewModel.estado.value
        assertEquals(VistaMiCuenta.RESUMEN, estado.vista)
        assertEquals(AvisoMiCuenta.CONTRASENA_CAMBIADA, estado.aviso)
        assertEquals("", estado.contrasena)
        assertFalse(estado.enviando)
    }

    @Test
    fun noDebeEnviar_cuandoLaContrasenaTieneMenosDeOchoCaracteres() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirContrasena()
        viewModel.alEscribirContrasena("corta")

        viewModel.alGuardarContrasena()
        advanceUntilIdle()

        assertEquals(R.string.validacion_contrasena_corta, viewModel.estado.value.errorContrasena)
        coVerify(exactly = 0) { repositorioAuth.cambiarContrasena(any()) }
    }

    @Test
    fun debePedirOtraContrasena_cuandoAuthLaRechaza() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirContrasena()
        coEvery { repositorioAuth.cambiarContrasena(any()) } returns
            Resultado.Error(TipoError.VALIDACION, "same_password")
        viewModel.alEscribirContrasena("la-misma-de-antes")

        viewModel.alGuardarContrasena()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(R.string.cuenta_contrasena_rechazada, estado.errorContrasena)
        assertNull(estado.errorFormulario)
        assertEquals(VistaMiCuenta.CAMBIAR_CONTRASENA, estado.vista)
        assertEquals("la-misma-de-antes", estado.contrasena)
    }

    @Test
    fun debeMostrarElErrorSinBorrarLoEscrito_cuandoElCambioFalla() = runTest(reglaCorrutinas.testDispatcher) {
        listOf(TipoError.RED, TipoError.AUTENTICACION, TipoError.SERVIDOR, TipoError.DESCONOCIDO).forEach { tipo ->
            val viewModel = abrirContrasena()
            coEvery { repositorioAuth.cambiarContrasena(any()) } returns Resultado.Error(tipo, "fallo")
            viewModel.alEscribirContrasena("nueva-secreta")

            viewModel.alGuardarContrasena()
            advanceUntilIdle()

            val estado = viewModel.estado.value
            assertEquals(tipo.name, tipo, estado.errorFormulario)
            assertNull(tipo.name, estado.errorContrasena)
            assertEquals(tipo.name, "nueva-secreta", estado.contrasena)
            assertEquals(tipo.name, VistaMiCuenta.CAMBIAR_CONTRASENA, estado.vista)
        }
    }

    @Test
    fun debeBorrarLaContrasenaEscrita_cuandoSeVuelveAlResumen() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = abrirContrasena()
        viewModel.alEscribirContrasena("a-medias")

        viewModel.alVolverAlResumen()

        assertEquals(VistaMiCuenta.RESUMEN, viewModel.estado.value.vista)
        assertEquals("", viewModel.estado.value.contrasena)
    }

    // ---- cerrar sesion ----------------------------------------------------

    private fun enResumen(): MiCuentaViewModel {
        fichaResponde()
        return crear().also { reglaCorrutinas.testDispatcher.scheduler.advanceUntilIdle() }
    }

    @Test
    fun debePedirConfirmacionYNoCerrar_cuandoSePideCerrarSesion() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = enResumen()

        viewModel.alPedirCerrarSesion()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.confirmandoCierre)
        coVerify(exactly = 0) { repositorioAuth.cerrarSesion() }
    }

    @Test
    fun noDebeCerrar_cuandoSeCancelaLaConfirmacion() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = enResumen()
        viewModel.alPedirCerrarSesion()

        viewModel.alCancelarCierre()
        advanceUntilIdle()

        assertFalse(viewModel.estado.value.confirmandoCierre)
        coVerify(exactly = 0) { repositorioAuth.cerrarSesion() }
    }

    @Test
    fun debeCerrarYQuedarseEsperandoLaSalida_cuandoSeConfirma() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = enResumen()
        coEvery { repositorioAuth.cerrarSesion() } returns Resultado.Exito(Unit)
        viewModel.alPedirCerrarSesion()

        viewModel.alConfirmarCierre()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorioAuth.cerrarSesion() }
        val estado = viewModel.estado.value
        assertFalse(estado.confirmandoCierre)
        assertTrue(estado.cerrandoSesion)
        assertNull(estado.errorCierre)
    }

    @Test
    fun debeMostrarElErrorYSeguirEnElResumen_cuandoElCierreFalla() = runTest(reglaCorrutinas.testDispatcher) {
        val viewModel = enResumen()
        coEvery { repositorioAuth.cerrarSesion() } returns Resultado.Error(TipoError.RED, "sin red")
        viewModel.alPedirCerrarSesion()

        viewModel.alConfirmarCierre()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorCierre)
        assertFalse(estado.cerrandoSesion)
        assertEquals(VistaMiCuenta.RESUMEN, estado.vista)
        assertEquals(usuario, estado.usuario)
    }
}
