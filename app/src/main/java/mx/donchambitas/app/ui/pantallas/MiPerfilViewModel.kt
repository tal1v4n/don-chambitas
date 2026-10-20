package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.ui.componentes.obtenerMensajeErrorRes
import mx.donchambitas.app.util.Resultado

private const val MAXIMO_DIGITOS_EXPERIENCIA = 2

/**
 * ViewModel de Mi perfil (P-11), HU-06.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, secciones 2.2 a 2.5.
 *
 * Las validaciones de campo son de S3-T07: aqui solo se recorta lo escrito al
 * armar el perfil. Pedir a la IA es de S3-T13; aqui solo se recalcula el boton.
 */
@HiltViewModel
class MiPerfilViewModel @Inject constructor(
    private val repositorioTrabajador: RepositorioTrabajador,
    private val repositorioCatalogos: RepositorioCatalogos
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMiPerfil())
    val estado: StateFlow<EstadoMiPerfil> = _estado.asStateFlow()

    /** La fila tal como quedo en la base, para conservar sus fechas al volver a guardar. */
    private var perfilGuardado: PerfilTrabajador? = null

    /** Las habilidades tal como quedaron en la base: si no cambian, no se reemplazan (2.4). */
    private var habilidadesGuardadas: List<String> = emptyList()

    private var cargaMunicipios: Job? = null

    init {
        cargar()
    }

    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val perfil = repositorioTrabajador.obtenerMiPerfil()
            val habilidades = repositorioTrabajador.obtenerMisHabilidades()
            val estados = repositorioCatalogos.obtenerEstados()
            val error = listOf(perfil, habilidades, estados).filterIsInstance<Resultado.Error>().firstOrNull()
            if (error != null) {
                _estado.update { it.copy(cargando = false, errorCarga = error.tipo) }
                return@launch
            }
            val leido = (perfil as Resultado.Exito).dato
            val listaHabilidades = (habilidades as Resultado.Exito).dato
            perfilGuardado = leido
            habilidadesGuardadas = listaHabilidades

            val municipios = leido?.estadoId?.let { repositorioCatalogos.obtenerMunicipios(it) }
            if (municipios is Resultado.Error) {
                _estado.update { it.copy(cargando = false, errorCarga = municipios.tipo) }
                return@launch
            }

            _estado.update {
                it.copy(
                    cargando = false,
                    perfilGuardado = leido != null,
                    usuarioId = leido?.usuarioId.orEmpty(),
                    titulo = leido?.titulo.orEmpty(),
                    experienciaAnios = leido?.experienciaAnios?.toString().orEmpty(),
                    habilidades = listaHabilidades,
                    descripcion = leido?.descripcion.orEmpty(),
                    estados = (estados as Resultado.Exito).dato,
                    municipios = (municipios as? Resultado.Exito)?.dato.orEmpty(),
                    estadoId = leido?.estadoId,
                    municipioId = leido?.municipioId,
                    telefonoContacto = leido?.telefonoContacto.orEmpty(),
                    disponible = leido?.disponible ?: true,
                    faltantes = calcularFaltantes(leido, listaHabilidades),
                    ia = it.ia.recalcular(faltanDatos = leido?.titulo.isNullOrBlank())
                )
            }
        }
    }

    fun alCambiarTitulo(valor: String) {
        _estado.update {
            it.copy(titulo = valor, errorTitulo = null, ia = it.ia.recalcular(faltanDatos = valor.isBlank()))
        }
    }

    fun alCambiarExperiencia(valor: String) {
        _estado.update {
            it.copy(
                experienciaAnios = valor.filter(Char::isDigit).take(MAXIMO_DIGITOS_EXPERIENCIA),
                errorExperiencia = null
            )
        }
    }

    fun alCambiarHabilidadNueva(valor: String) {
        _estado.update { it.copy(habilidadNueva = valor, errorHabilidad = null) }
    }

    /** Sin validar todavia: vacia o repetida es de S3-T07. Lo que si se hace es no agregar nada. */
    fun alAgregarHabilidad() {
        val habilidad = _estado.value.habilidadNueva.trim()
        if (habilidad.isEmpty()) return
        _estado.update { it.copy(habilidades = it.habilidades + habilidad, habilidadNueva = "") }
    }

    /** No llama al repositorio: se guarda con todo lo demas (2.3). */
    fun alQuitarHabilidad(habilidad: String) {
        _estado.update { it.copy(habilidades = it.habilidades - habilidad) }
    }

    fun alCambiarDescripcion(valor: String) {
        _estado.update { it.copy(descripcion = valor) }
    }

    /** Otro estado borra el municipio y trae solo los suyos (HU-06). */
    fun alElegirEstado(id: Int) {
        if (id == _estado.value.estadoId) return
        _estado.update { it.copy(estadoId = id, municipioId = null, municipios = emptyList()) }
        cargaMunicipios?.cancel()
        cargaMunicipios = viewModelScope.launch {
            when (val resultado = repositorioCatalogos.obtenerMunicipios(id)) {
                is Resultado.Exito -> _estado.update { it.copy(municipios = resultado.dato) }
                is Resultado.Error -> _estado.update { it.copy(aviso = obtenerMensajeErrorRes(resultado.tipo)) }
            }
        }
    }

    fun alElegirMunicipio(id: Int) {
        _estado.update { it.copy(municipioId = id, errorMunicipio = null) }
    }

    fun alCambiarTelefono(valor: String) {
        _estado.update { it.copy(telefonoContacto = valor.filter(Char::isDigit), errorTelefono = null) }
    }

    fun alCambiarDisponible(valor: Boolean) {
        _estado.update { it.copy(disponible = valor) }
    }

    /** Primero el perfil y luego las habilidades, que apuntan a el (2.4). */
    fun alGuardar() {
        val actual = _estado.value
        if (actual.guardando || actual.cargando) return
        _estado.update { it.copy(guardando = true, errorPantalla = null, soloFaltanHabilidades = false) }
        viewModelScope.launch {
            when (val resultado = repositorioTrabajador.guardarMiPerfil(armarPerfil(actual))) {
                is Resultado.Exito -> {
                    perfilGuardado = resultado.dato
                    _estado.update { it.copy(perfilGuardado = true, usuarioId = resultado.dato.usuarioId) }
                    guardarHabilidades(actual.habilidades)
                }
                is Resultado.Error -> _estado.update { it.copy(guardando = false, errorPantalla = resultado.tipo) }
            }
        }
    }

    /** "Reintentar" del error en linea: si el perfil ya se guardo, repite solo las habilidades. */
    fun alReintentarGuardado() {
        val actual = _estado.value
        if (!actual.soloFaltanHabilidades) {
            alGuardar()
            return
        }
        if (actual.guardando) return
        _estado.update { it.copy(guardando = true, errorPantalla = null, soloFaltanHabilidades = false) }
        viewModelScope.launch { guardarHabilidades(actual.habilidades) }
    }

    private suspend fun guardarHabilidades(habilidades: List<String>) {
        if (habilidades != habilidadesGuardadas) {
            val resultado = repositorioTrabajador.reemplazarHabilidades(habilidades)
            if (resultado is Resultado.Error) {
                _estado.update {
                    it.copy(guardando = false, errorPantalla = resultado.tipo, soloFaltanHabilidades = true)
                }
                return
            }
            habilidadesGuardadas = habilidades
        }
        _estado.update {
            it.copy(
                guardando = false,
                faltantes = calcularFaltantes(perfilGuardado, habilidadesGuardadas),
                aviso = R.string.mi_perfil_guardado
            )
        }
    }

    private fun armarPerfil(estado: EstadoMiPerfil): PerfilTrabajador {
        val ahora = Instant.now()
        return PerfilTrabajador(
            usuarioId = perfilGuardado?.usuarioId ?: estado.usuarioId,
            titulo = estado.titulo.trim(),
            descripcion = estado.descripcion.trim().ifEmpty { null },
            experienciaAnios = estado.experienciaAnios.toIntOrNull() ?: 0,
            telefonoContacto = estado.telefonoContacto.ifEmpty { null },
            estadoId = estado.estadoId,
            municipioId = estado.municipioId,
            disponible = estado.disponible,
            creadoEn = perfilGuardado?.creadoEn ?: ahora,
            actualizadoEn = ahora
        )
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }
}
