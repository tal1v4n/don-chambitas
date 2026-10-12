package mx.donchambitas.app.ui.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth

/** El mismo tope que P-01 le da a la lectura de la sesión (S2-T09). */
internal const val ESPERA_MAXIMA_SESION_MS = 8_000L

/**
 * Escucha la sesión mientras la aplicación está abierta, uno por actividad.
 * Avisa cuando Auth se queda sin sesión, sea porque el usuario la cerró o
 * porque el token de refresco dejó de servir (CONTRATOS-API.md,
 * "sesionActual()"), y cuando el usuario llega por el enlace de recuperación
 * (DEC-27). Entrar por el camino normal lo siguen decidiendo P-01, P-02 y
 * P-03.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _sinSesion = Channel<Unit>(Channel.CONFLATED)

    /**
     * Un aviso por cada vez que Auth reporta que no hay sesión, incluido el
     * del arranque. Quien lo recibe decide si había alguien adentro.
     */
    val sinSesion: Flow<Unit> = _sinSesion.receiveAsFlow()

    private val _recuperacion = Channel<RolUsuario>(Channel.CONFLATED)

    /**
     * El rol de quien acaba de llegar por el enlace de recuperación, ya con
     * la sesión abierta. Quien lo recibe lo lleva a P-18.
     */
    val recuperacion: Flow<RolUsuario> = _recuperacion.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                repositorioAuth.sesionActual().collect { sesion ->
                    if (sesion == null) _sinSesion.send(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin llaves en local.properties el flujo lanza: no hay sesión
                // que escuchar y la aplicación sigue abierta.
            }
        }
    }

    /**
     * MainActivity ya importó la sesión del enlace. Aquí se espera a que
     * sesionActual() la entregue con su ficha, porque de ahí sale el rol. Si
     * no llega en el tope (sin red para leer la ficha), no se avisa nada y la
     * aplicación se queda donde estaba.
     */
    fun alLlegarPorRecuperacion() {
        viewModelScope.launch {
            try {
                val sesion = withTimeoutOrNull(ESPERA_MAXIMA_SESION_MS) {
                    repositorioAuth.sesionActual().filterNotNull().first()
                }
                if (sesion != null) _recuperacion.send(sesion.usuario.rol)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Mismo caso que arriba: sin llaves no hay sesión que esperar.
            }
        }
    }
}
