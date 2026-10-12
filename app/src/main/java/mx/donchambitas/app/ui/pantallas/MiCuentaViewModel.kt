package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.dominio.validacion.LimitesRegistro
import mx.donchambitas.app.dominio.validacion.ValidacionesAuth
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel de la pantalla Mi cuenta (P-18).
 * Contrato de estado y eventos en docs/producto/DISENO-CUENTA.md, secciones
 * 5 a 7.
 */
@HiltViewModel
class MiCuentaViewModel @Inject constructor(
    private val repositorioUsuario: RepositorioUsuario,
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMiCuenta())
    val estado: StateFlow<EstadoMiCuenta> = _estado.asStateFlow()

    // Misma regla que en P-03: solo se valida al salir de un campo en el que
    // ya se escribio (1.5 de DISENO-AUTENTICACION.md).
    private var tocados = emptySet<CampoMiCuenta>()
    private var contrasenaTocada = false

    init {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val resultado = repositorioUsuario.obtenerMiUsuario()
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(cargando = false, usuario = resultado.dato)
                    is Resultado.Error -> it.copy(cargando = false, errorCarga = resultado.tipo)
                }
            }
        }
    }

    fun alReintentarCarga() {
        if (!_estado.value.cargando) cargar()
    }

    /** Quien llega por el enlace de recuperacion entra directo a la contrasena (DEC-27). */
    fun alLlegarPorRecuperacion() {
        alAbrirCambiarContrasena()
    }

    fun alAbrirEditarDatos() {
        val usuario = _estado.value.usuario ?: return
        tocados = emptySet()
        _estado.update {
            it.copy(
                vista = VistaMiCuenta.EDITAR_DATOS,
                aviso = null,
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                telefono = usuario.telefono.orEmpty(),
                errorNombre = null,
                errorApellidos = null,
                errorTelefono = null,
                errorFormulario = null,
                errorCierre = null
            )
        }
    }

    fun alAbrirCambiarContrasena() {
        contrasenaTocada = false
        _estado.update {
            it.copy(
                vista = VistaMiCuenta.CAMBIAR_CONTRASENA,
                aviso = null,
                contrasena = "",
                errorContrasena = null,
                errorFormulario = null,
                errorCierre = null
            )
        }
    }

    /** Regresa al resumen descartando lo escrito. Mientras se envia no se sale. */
    fun alVolverAlResumen() {
        if (_estado.value.enviando) return
        _estado.update {
            it.copy(vista = VistaMiCuenta.RESUMEN, contrasena = "", errorFormulario = null)
        }
    }

    fun alCambiarNombre(valor: String) {
        tocados = tocados + CampoMiCuenta.NOMBRE
        _estado.update {
            it.copy(nombre = valor.take(LimitesRegistro.LARGO_MAXIMO_NOMBRE), errorNombre = null)
        }
    }

    fun alCambiarApellidos(valor: String) {
        tocados = tocados + CampoMiCuenta.APELLIDOS
        _estado.update {
            it.copy(apellidos = valor.take(LimitesRegistro.LARGO_MAXIMO_APELLIDOS), errorApellidos = null)
        }
    }

    /** Se filtra al escribir, igual que en P-03. */
    fun alCambiarTelefono(valor: String) {
        tocados = tocados + CampoMiCuenta.TELEFONO
        _estado.update {
            it.copy(
                telefono = valor.filter(Char::isDigit).take(LimitesRegistro.LARGO_TELEFONO),
                errorTelefono = null
            )
        }
    }

    fun alPerderFoco(campo: CampoMiCuenta) {
        if (campo in tocados) _estado.update { it.validado(campo) }
    }

    fun alGuardarDatos() {
        if (_estado.value.enviando) return
        val validado = CampoMiCuenta.entries.fold(_estado.value.copy(errorFormulario = null)) { parcial, campo ->
            parcial.validado(campo)
        }
        if (!validado.sinErroresDeDatos()) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(enviando = true)

        viewModelScope.launch {
            // El repositorio no recorta (CONTRATOS-API.md, "Usuario"): la
            // normalizacion de 1.6 se hace aqui.
            val resultado = repositorioUsuario.actualizarMiUsuario(
                nombre = validado.nombre.trim(),
                apellidos = validado.apellidos.trim(),
                telefono = validado.telefono
            )
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(
                        enviando = false,
                        usuario = resultado.dato,
                        vista = VistaMiCuenta.RESUMEN,
                        aviso = AvisoMiCuenta.DATOS_GUARDADOS
                    )
                    is Resultado.Error -> it.copy(enviando = false, errorFormulario = resultado.tipo)
                }
            }
        }
    }

    fun alEscribirContrasena(valor: String) {
        contrasenaTocada = true
        _estado.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun alPerderFocoContrasena() {
        if (contrasenaTocada) _estado.update { it.contrasenaValidada() }
    }

    fun alGuardarContrasena() {
        if (_estado.value.enviando) return
        val validado = _estado.value.copy(errorFormulario = null).contrasenaValidada()
        if (validado.errorContrasena != null) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(enviando = true)

        viewModelScope.launch {
            val resultado = repositorioAuth.cambiarContrasena(validado.contrasena)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(
                        enviando = false,
                        contrasena = "",
                        vista = VistaMiCuenta.RESUMEN,
                        aviso = AvisoMiCuenta.CONTRASENA_CAMBIADA
                    )
                    is Resultado.Error -> if (resultado.tipo == TipoError.VALIDACION) {
                        // Con el minimo de 8 ya validado aqui, lo unico que
                        // Auth puede rechazar es la misma contrasena de antes.
                        it.copy(enviando = false, errorContrasena = R.string.cuenta_contrasena_rechazada)
                    } else {
                        it.copy(enviando = false, errorFormulario = resultado.tipo)
                    }
                }
            }
        }
    }

    /** Repite el envio de la vista en la que esta el usuario. */
    fun alReintentarFormulario() {
        when (_estado.value.vista) {
            VistaMiCuenta.EDITAR_DATOS -> alGuardarDatos()
            VistaMiCuenta.CAMBIAR_CONTRASENA -> alGuardarContrasena()
            VistaMiCuenta.RESUMEN -> Unit
        }
    }

    fun alPedirCerrarSesion() {
        if (_estado.value.cerrandoSesion) return
        _estado.update { it.copy(confirmandoCierre = true, errorCierre = null, aviso = null) }
    }

    fun alCancelarCierre() {
        _estado.update { it.copy(confirmandoCierre = false) }
    }

    /**
     * No navega: si el cierre sale bien, sesionActual() emite null y
     * GrafoNavegacion saca al usuario a P-02. Por eso el boton se queda
     * cargando. Si falla (sin red), la sesion sigue abierta y se dice.
     */
    fun alConfirmarCierre() {
        if (_estado.value.cerrandoSesion) return
        _estado.update { it.copy(confirmandoCierre = false, cerrandoSesion = true, errorCierre = null) }
        viewModelScope.launch {
            val resultado = repositorioAuth.cerrarSesion()
            if (resultado is Resultado.Error) {
                _estado.update { it.copy(cerrandoSesion = false, errorCierre = resultado.tipo) }
            }
        }
    }
}

private fun EstadoMiCuenta.validado(campo: CampoMiCuenta): EstadoMiCuenta = when (campo) {
    CampoMiCuenta.NOMBRE ->
        copy(errorNombre = ValidacionesAuth.validarNombre(nombre)?.mensaje())
    CampoMiCuenta.APELLIDOS ->
        copy(errorApellidos = ValidacionesAuth.validarApellidos(apellidos)?.mensaje())
    CampoMiCuenta.TELEFONO ->
        copy(errorTelefono = ValidacionesAuth.validarTelefono(telefono)?.mensaje())
}

private fun EstadoMiCuenta.contrasenaValidada(): EstadoMiCuenta =
    copy(errorContrasena = ValidacionesAuth.validarContrasenaRegistro(contrasena)?.mensaje())

private fun EstadoMiCuenta.sinErroresDeDatos(): Boolean =
    listOf(errorNombre, errorApellidos, errorTelefono).all { it == null }
