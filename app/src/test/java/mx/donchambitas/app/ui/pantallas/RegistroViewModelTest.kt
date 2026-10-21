package mx.donchambitas.app.ui.pantallas

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.donchambitas.app.R
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import mx.donchambitas.app.datos.falso.RepositorioAuthFalso
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val repositorio = mockk<RepositorioAuth>()
    private val viewModel = RegistroViewModel(repositorio)

    private fun responderCon(resultado: Resultado<Sesion>) {
        coEvery { repositorio.registrar(any(), any(), any(), any(), any(), any()) } returns resultado
    }

    private fun RegistroViewModel.llenarValido(rol: RolUsuario = RolUsuario.CLIENTE) {
        alElegirRol(rol)
        alCambiarNombre("Refugio")
        alCambiarApellidos("Martinez Luna")
        alCambiarCorreo("refugio@ejemplo.mx")
        alCambiarContrasena("12345678")
        alCambiarTelefono("4771234567")
    }

    @Test
    fun debeEnviarLosValoresNormalizados_cuandoSeRegistra() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR))))
        viewModel.alElegirRol(RolUsuario.TRABAJADOR)
        viewModel.alCambiarNombre("  Refugio ")
        viewModel.alCambiarApellidos(" Martinez Luna  ")
        viewModel.alCambiarCorreo(" Refugio@Ejemplo.MX ")
        viewModel.alCambiarContrasena(" clave123 ")
        viewModel.alCambiarTelefono("477-123-4567")

        viewModel.alRegistrar()
        advanceUntilIdle()

        coVerify {
            repositorio.registrar(
                correo = "refugio@ejemplo.mx",
                contrasena = " clave123 ",
                nombre = "Refugio",
                apellidos = "Martinez Luna",
                telefono = "4771234567",
                rol = RolUsuario.TRABAJADOR
            )
        }
    }

    @Test
    fun debeReclamarElRolYNoLlamarAlRepositorio_cuandoNoSeEligio() = runTest(reglaCorrutinas.testDispatcher) {
        viewModel.alCambiarNombre("Refugio")
        viewModel.alCambiarApellidos("Martinez Luna")
        viewModel.alCambiarCorreo("refugio@ejemplo.mx")
        viewModel.alCambiarContrasena("12345678")
        viewModel.alCambiarTelefono("4771234567")

        viewModel.alRegistrar()
        advanceUntilIdle()

        assertEquals(R.string.validacion_rol_sin_elegir, viewModel.estado.value.errorRol)
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun debeMarcarLosCincoCamposYNoLlamarAlRepositorio_cuandoSeEnviaConRolYTodoVacio() = runTest(reglaCorrutinas.testDispatcher) {
        viewModel.alElegirRol(RolUsuario.CLIENTE)

        viewModel.alRegistrar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNull(estado.errorRol)
        assertEquals(R.string.validacion_nombre_vacio, estado.errorNombre)
        assertEquals(R.string.validacion_apellidos_vacio, estado.errorApellidos)
        assertEquals(R.string.validacion_correo_vacio, estado.errorCorreo)
        assertEquals(R.string.validacion_contrasena_corta, estado.errorContrasena)
        assertEquals(R.string.validacion_telefono_vacio, estado.errorTelefono)
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun debeIrAInicioDelRol_cuandoLaCuentaQuedaCreada() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.TRABAJADOR))))
        viewModel.llenarValido(RolUsuario.TRABAJADOR)

        viewModel.alRegistrar()
        assertTrue(viewModel.estado.value.cargando)
        advanceUntilIdle()

        assertFalse(viewModel.estado.value.cargando)
        assertEquals(Ruta.InicioTrabajador, viewModel.estado.value.destino)
    }

    @Test
    fun debeMarcarCorreoDuplicadoSinNavegar_cuandoElCorreoYaExiste() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.CORREO_DUPLICADO, "User already registered"))
        viewModel.llenarValido()

        viewModel.alRegistrar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.CORREO_DUPLICADO, estado.errorPantalla)
        assertNull(estado.destino)
        assertFalse(estado.cargando)
    }

    @Test
    fun debeConservarTodo_cuandoElErrorEsDeRed() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.RED, "sin red"))
        viewModel.llenarValido(RolUsuario.TRABAJADOR)

        viewModel.alRegistrar()
        advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals(TipoError.RED, estado.errorPantalla)
        assertEquals(RolUsuario.TRABAJADOR, estado.rol)
        assertEquals("12345678", estado.contrasena)
        assertEquals("4771234567", estado.telefono)
        assertFalse(estado.cargando)
    }

    @Test
    fun debeVolverALlamarAlRepositorio_cuandoSeReintenta() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Error(TipoError.SERVIDOR, "caido"))
        viewModel.llenarValido()
        viewModel.alRegistrar()
        advanceUntilIdle()

        responderCon(Resultado.Exito(Sesion(DatosPrueba.crearUsuario(rol = RolUsuario.CLIENTE))))
        viewModel.alReintentar()
        advanceUntilIdle()

        coVerify(exactly = 2) { repositorio.registrar(any(), any(), any(), any(), any(), any()) }
        assertNull(viewModel.estado.value.errorPantalla)
        assertEquals(Ruta.InicioCliente, viewModel.estado.value.destino)
    }

    @Test
    fun debeLlamarUnaSolaVez_cuandoSePulsaDosVecesMientrasCarga() = runTest(reglaCorrutinas.testDispatcher) {
        responderCon(Resultado.Exito(Sesion(DatosPrueba.crearUsuario())))
        viewModel.llenarValido()

        viewModel.alRegistrar()
        viewModel.alRegistrar()
        advanceUntilIdle()

        coVerify(exactly = 1) { repositorio.registrar(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun debeDescartarLoQueNoSeaDigitoYCortarEnDiez_cuandoSeEscribeElTelefono() {
        viewModel.alCambiarTelefono("477-12ab34x5678901")

        assertEquals("4771234567", viewModel.estado.value.telefono)
    }

    @Test
    fun debeCortarEnElTope_cuandoElNombreEsMasLargo() {
        viewModel.alCambiarNombre("a".repeat(100))

        assertEquals(80, viewModel.estado.value.nombre.length)
    }

    @Test
    fun debeMarcarElTelefono_cuandoPierdeElFocoConMenosDeDiezDigitos() {
        viewModel.alCambiarTelefono("55123")

        viewModel.alPerderFoco(CampoRegistro.TELEFONO)

        assertEquals(R.string.validacion_telefono_digitos, viewModel.estado.value.errorTelefono)
    }

    @Test
    fun debeNoMarcarNada_cuandoPierdeElFocoUnCampoQueNoSeToco() {
        viewModel.alPerderFoco(CampoRegistro.NOMBRE)

        assertNull(viewModel.estado.value.errorNombre)
    }

    @Test
    fun debePoderIniciarSesion_cuandoSeRegistroAntesContraElRepositorioFalso() = runTest(reglaCorrutinas.testDispatcher) {
        val repositorioFalso = RepositorioAuthFalso(FuenteDatosFalsa()).apply { retrasoMs = 0 }
        val registro = RegistroViewModel(repositorioFalso)
        registro.llenarValido(RolUsuario.TRABAJADOR)
        registro.alRegistrar()
        advanceUntilIdle()

        val inicio = IniciarSesionViewModel(repositorioFalso)
        inicio.alCambiarCorreo("Refugio@Ejemplo.mx")
        inicio.alCambiarContrasena("12345678")
        inicio.alIniciarSesion()
        advanceUntilIdle()

        assertEquals(Ruta.InicioTrabajador, inicio.estado.value.destino)
    }
}
