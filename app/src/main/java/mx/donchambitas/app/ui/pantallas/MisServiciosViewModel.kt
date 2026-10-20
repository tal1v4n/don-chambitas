package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.dominio.repositorio.RepositorioServicios
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.Resultado

/**
 * ViewModel de Mis servicios (P-12), HU-09 y HU-10.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, secciones 3.2 y 3.3.
 *
 * No carga en el init: la pantalla llama a [alCargar] en cada ON_RESUME, que
 * incluye el primero, para que al volver de P-13 aparezca lo guardado (8).
 */
@HiltViewModel
class MisServiciosViewModel @Inject constructor(
    private val repositorioServicios: RepositorioServicios,
    private val repositorioCatalogos: RepositorioCatalogos
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMisServicios())
    val estado: StateFlow<EstadoMisServicios> = _estado.asStateFlow()

    private var cargadoUnaVez = false
    private var carga: Job? = null

    /**
     * La primera vez tapa la pantalla con el cargando; despues recarga sin
     * quitar la lista, y si falla solo avisa.
     *
     * @param cambiosGuardados Se vuelve de P-13 tras "Guardar cambios": el aviso sale aqui (4.5).
     */
    fun alCargar(cambiosGuardados: Boolean = false) {
        _estado.update {
            it.copy(
                cargando = !cargadoUnaVez,
                error = null,
                aviso = if (cambiosGuardados) R.string.servicio_cambios_guardados else it.aviso
            )
        }
        carga?.cancel()
        carga = viewModelScope.launch {
            val servicios = repositorioServicios.obtenerMisServicios()
            val categorias = repositorioCatalogos.obtenerCategorias()
            val error = listOf(servicios, categorias).filterIsInstance<Resultado.Error>().firstOrNull()
            _estado.update {
                when {
                    error != null && cargadoUnaVez -> it.copy(aviso = obtenerMensajeErrorRes(error.tipo))
                    error != null -> it.copy(cargando = false, error = error.tipo)
                    else -> it.copy(
                        cargando = false,
                        servicios = ordenarMisServicios((servicios as Resultado.Exito).dato),
                        categorias = (categorias as Resultado.Exito).dato.associate { c -> c.id to c.nombre }
                    )
                }
            }
            if (error == null) cargadoUnaVez = true
        }
    }

    fun alAbrirAcciones(servicio: Servicio) {
        if (servicio.id == _estado.value.procesandoId) return
        _estado.update { it.copy(servicioEnAcciones = servicio) }
    }

    fun alCerrarAcciones() {
        _estado.update { it.copy(servicioEnAcciones = null) }
    }

    /** Solo cierra la hoja: a P-13 navega la pantalla. */
    fun alEditar() {
        alCerrarAcciones()
    }

    fun alCambiarActivo(servicio: Servicio) {
        _estado.update { it.copy(servicioEnAcciones = null, procesandoId = servicio.id) }
        viewModelScope.launch {
            val resultado = repositorioServicios.pausar(servicio.id, !servicio.activo)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.terminado(servicio.id).copy(
                        servicios = ordenarMisServicios(
                            it.servicios.map { s -> if (s.id == servicio.id) resultado.dato else s }
                        ),
                        aviso = if (resultado.dato.activo) {
                            R.string.mis_servicios_aviso_reactivado
                        } else {
                            R.string.mis_servicios_aviso_pausado
                        }
                    )
                    is Resultado.Error -> it.terminado(servicio.id).copy(aviso = obtenerMensajeErrorRes(resultado.tipo))
                }
            }
        }
    }

    fun alPedirEliminar(servicio: Servicio) {
        _estado.update { it.copy(servicioEnAcciones = null, servicioPorEliminar = servicio) }
    }

    fun alCancelarEliminar() {
        _estado.update { it.copy(servicioPorEliminar = null) }
    }

    fun alConfirmarEliminar() {
        val servicio = _estado.value.servicioPorEliminar ?: return
        _estado.update { it.copy(servicioPorEliminar = null, procesandoId = servicio.id) }
        viewModelScope.launch {
            val resultado = repositorioServicios.eliminar(servicio.id)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.terminado(servicio.id).copy(
                        servicios = it.servicios.filterNot { s -> s.id == servicio.id },
                        aviso = R.string.mis_servicios_aviso_eliminado
                    )
                    is Resultado.Error -> it.terminado(servicio.id).copy(aviso = obtenerMensajeErrorRes(resultado.tipo))
                }
            }
        }
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }
}

/** Suelta la tarjeta que se procesaba, si sigue siendo esa. */
private fun EstadoMisServicios.terminado(id: String): EstadoMisServicios =
    if (procesandoId == id) copy(procesandoId = null) else this
