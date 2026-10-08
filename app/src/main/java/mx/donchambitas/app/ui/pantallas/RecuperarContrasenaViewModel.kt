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
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.validacion.ValidacionesAuth
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de la pantalla de recuperar contrasena (P-04).
 * Contrato de estado y eventos en docs/producto/DISENO-AUTENTICACION.md,
 * secciones 4.3 y 4.4.
 */
@HiltViewModel
class RecuperarContrasenaViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRecuperarContrasena())
    val estado: StateFlow<EstadoRecuperarContrasena> = _estado.asStateFlow()

    // Igual que en P-02: salir de un campo vacio que nunca se toco no es un
    // error todavia (1.5).
    private var correoTocado = false

    fun alCambiarCorreo(valor: String) {
        correoTocado = true
        _estado.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun alPerderFocoCorreo() {
        if (correoTocado) _estado.update { it.correoValidado() }
    }

    fun alEnviar() {
        if (_estado.value.cargando) return
        val validado = _estado.value.copy(errorPantalla = null).correoValidado()
        if (validado.errorCorreo != null) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(cargando = true)

        viewModelScope.launch {
            // Se normaliza como en P-02 (1.6); en pantalla se sigue viendo lo
            // que se tecleo.
            val resultado = repositorioAuth.recuperarContrasena(validado.correo.trim().lowercase())
            _estado.update {
                when (resultado) {
                    // El contrato da Exito exista o no la cuenta. La pantalla no
                    // distingue nada mas, para no delatar que correos existen.
                    is Resultado.Exito -> it.copy(cargando = false, enviado = true)
                    // Un fallo de red no es exito: si la peticion no salio, el
                    // usuario tiene que saberlo o espera un correo que nunca
                    // se pidio (4.4).
                    is Resultado.Error -> it.copy(cargando = false, errorPantalla = resultado.tipo)
                }
            }
        }
    }

    fun alReintentar() {
        _estado.update { it.copy(errorPantalla = null) }
        alEnviar()
    }
}

private fun EstadoRecuperarContrasena.correoValidado(): EstadoRecuperarContrasena =
    copy(errorCorreo = ValidacionesAuth.validarCorreo(correo)?.mensaje())
