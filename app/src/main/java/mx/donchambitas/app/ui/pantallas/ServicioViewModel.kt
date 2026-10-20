package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
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
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel de Crear o editar servicio (P-13), HU-09 a HU-11.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, secciones 4.3 a 4.5.
 *
 * El servicioId sale del argumento de la ruta. Al publicar se guarda ahi el id
 * nuevo, asi que si Android mata el proceso la pantalla vuelve en modo editar.
 *
 * Las validaciones son de S3-T07: hasta entonces [alGuardar] solo se niega a lo
 * que no cabe en el modelo. Al editar, las fotos que ya tenia el servicio no se
 * pintan: nada las devuelve con su id (H-18).
 */
@HiltViewModel
class ServicioViewModel @Inject constructor(
    private val repositorioServicios: RepositorioServicios,
    private val repositorioCatalogos: RepositorioCatalogos,
    private val estadoGuardado: SavedStateHandle
) : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoServicio(servicioId = estadoGuardado.get<String>(Ruta.CrearEditarServicio.ARG_SERVICIO_ID))
    )
    val estado: StateFlow<EstadoServicio> = _estado.asStateFlow()

    /** El servicio tal como esta en la base: conserva dueño, estado y fechas al actualizar. */
    private var servicioGuardado: Servicio? = null

    /** Una corrutina por posicion, para cancelar cada subida sin tocar las otras (HU-11). */
    private val subidas = mutableMapOf<Int, Job>()

    init {
        cargar()
    }

    fun alReintentarCarga() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null, noExiste = false) }
        viewModelScope.launch {
            val categorias = repositorioCatalogos.obtenerCategorias()
            if (categorias is Resultado.Error) {
                _estado.update { it.copy(cargando = false, errorCarga = categorias.tipo) }
                return@launch
            }
            val lista = (categorias as Resultado.Exito).dato
            val id = _estado.value.servicioId
            if (id == null) {
                _estado.update { it.copy(cargando = false, categorias = lista) }
                return@launch
            }

            when (val servicios = repositorioServicios.obtenerMisServicios()) {
                is Resultado.Error -> _estado.update { it.copy(cargando = false, errorCarga = servicios.tipo) }
                is Resultado.Exito -> {
                    val servicio = servicios.dato.firstOrNull { it.id == id }
                    servicioGuardado = servicio
                    _estado.update {
                        if (servicio == null) {
                            it.copy(cargando = false, noExiste = true, categorias = lista)
                        } else {
                            it.copy(
                                cargando = false,
                                categorias = lista,
                                titulo = servicio.titulo,
                                categoriaId = servicio.categoriaId,
                                descripcion = servicio.descripcion,
                                precioDesde = servicio.precioDesde?.let(::textoDePrecio).orEmpty(),
                                precioHasta = servicio.precioHasta?.let(::textoDePrecio).orEmpty(),
                                unidadPrecio = servicio.unidadPrecio.orEmpty()
                            ).conIaRecalculada()
                        }
                    }
                }
            }
        }
    }

    fun alCambiarTitulo(valor: String) {
        _estado.update { it.copy(titulo = valor, errorTitulo = null).conIaRecalculada() }
    }

    fun alElegirCategoria(id: Int) {
        _estado.update { it.copy(categoriaId = id, errorCategoria = null).conIaRecalculada() }
    }

    fun alCambiarDescripcion(valor: String) {
        _estado.update { it.copy(descripcion = valor, errorDescripcion = null) }
    }

    fun alCambiarPrecioDesde(valor: String) {
        _estado.update { it.copy(precioDesde = filtrarPrecio(valor), errorPrecioDesde = null, errorPrecioHasta = null) }
    }

    fun alCambiarPrecioHasta(valor: String) {
        _estado.update { it.copy(precioHasta = filtrarPrecio(valor), errorPrecioDesde = null, errorPrecioHasta = null) }
    }

    fun alCambiarUnidad(valor: String) {
        _estado.update { it.copy(unidadPrecio = valor, errorUnidad = null) }
    }

    /** Con una foto subiendo, espera (4.5). */
    fun alGuardar() {
        val actual = _estado.value
        if (actual.guardando || actual.cargando || actual.subiendoFoto) return
        val servicio = armarServicio(actual) ?: return
        _estado.update { it.copy(guardando = true, errorPantalla = null) }
        viewModelScope.launch {
            val resultado = if (actual.creando) {
                repositorioServicios.crear(servicio)
            } else {
                repositorioServicios.actualizar(servicio)
            }
            when (resultado) {
                is Resultado.Error -> _estado.update { it.copy(guardando = false, errorPantalla = resultado.tipo) }
                is Resultado.Exito -> {
                    servicioGuardado = resultado.dato
                    if (actual.creando) {
                        // No navega: la misma entrada de la pila pasa a editar (4.1).
                        estadoGuardado[Ruta.CrearEditarServicio.ARG_SERVICIO_ID] = resultado.dato.id
                        _estado.update {
                            it.copy(guardando = false, servicioId = resultado.dato.id, aviso = R.string.servicio_publicado)
                        }
                    } else {
                        _estado.update { it.copy(guardando = false, cambiosGuardados = true) }
                    }
                }
            }
        }
    }

    /** La pantalla ya la valido por tipo y peso; se sube en la primera posicion libre (4.4). */
    fun alAgregarFoto(bytes: ByteArray) {
        val actual = _estado.value
        val servicioId = actual.servicioId ?: return
        val posicion = (1..MAXIMO_FOTOS_SERVICIO).firstOrNull { p -> actual.fotos.none { it.posicion == p } } ?: return
        _estado.update { it.copy(fotos = it.fotos + FotoEnPantalla.Subiendo(posicion)) }
        subidas[posicion] = viewModelScope.launch {
            val resultado = repositorioServicios.subirFoto(servicioId, bytes, posicion)
            subidas.remove(posicion)
            _estado.update {
                val sinSubida = it.fotos.filterNot { f -> f == FotoEnPantalla.Subiendo(posicion) }
                when (resultado) {
                    is Resultado.Exito -> it.copy(fotos = sinSubida + FotoEnPantalla.Guardada(resultado.dato))
                    is Resultado.Error -> it.copy(
                        fotos = sinSubida,
                        aviso = if (resultado.tipo == TipoError.VALIDACION) {
                            obtenerMensajeErrorRes(TipoError.VALIDACION)
                        } else {
                            R.string.servicio_foto_no_subio
                        }
                    )
                }
            }
        }
    }

    /** Quita la miniatura y deja el formulario como estaba (HU-11). */
    fun alCancelarSubida(posicion: Int) {
        subidas.remove(posicion)?.cancel()
        _estado.update { it.copy(fotos = it.fotos.filterNot { f -> f == FotoEnPantalla.Subiendo(posicion) }) }
    }

    /** La imagen no se pudo usar (pesada, de otro tipo, ilegible o sin camara): solo se avisa. */
    fun alRechazarFoto(@StringRes motivo: Int) {
        _estado.update { it.copy(aviso = motivo) }
    }

    fun alPedirQuitarFoto(foto: FotoEnPantalla) {
        _estado.update { it.copy(fotoPorQuitar = foto) }
    }

    fun alCancelarQuitarFoto() {
        _estado.update { it.copy(fotoPorQuitar = null) }
    }

    fun alConfirmarQuitarFoto() {
        val foto = _estado.value.fotoPorQuitar as? FotoEnPantalla.Guardada ?: return
        _estado.update { it.copy(fotoPorQuitar = null) }
        viewModelScope.launch {
            val resultado = repositorioServicios.eliminarFoto(foto.foto.id)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(fotos = it.fotos - foto)
                    is Resultado.Error -> it.copy(aviso = obtenerMensajeErrorRes(resultado.tipo))
                }
            }
        }
    }

    fun alConsumirAviso() {
        _estado.update { it.copy(aviso = null) }
    }

    /**
     * Nulo si lo escrito no cabe en [Servicio]: sin categoria, o con un precio
     * que no se lee como numero. Marcar esos errores es de S3-T07.
     */
    private fun armarServicio(estado: EstadoServicio): Servicio? {
        val categoriaId = estado.categoriaId ?: return null
        val desde = leerPrecio(estado.precioDesde) ?: return null
        val hasta = leerPrecio(estado.precioHasta) ?: return null
        val ahora = Instant.now()
        val guardado = servicioGuardado
        return Servicio(
            id = guardado?.id.orEmpty(),
            perfilId = guardado?.perfilId.orEmpty(),
            categoriaId = categoriaId,
            titulo = estado.titulo.trim(),
            descripcion = estado.descripcion.trim(),
            precioDesde = desde.cantidad,
            precioHasta = hasta.cantidad,
            unidadPrecio = estado.unidadPrecio.trim().takeIf { it.isNotEmpty() && estado.hayPrecio },
            activo = guardado?.activo ?: true,
            creadoEn = guardado?.creadoEn ?: ahora,
            actualizadoEn = ahora
        )
    }
}

/** Un precio leido: [cantidad] nula si el campo estaba vacio. */
private class PrecioLeido(val cantidad: BigDecimal?)

/** Nulo si el texto no es un numero; vacio es un precio leido sin cantidad. Acepta coma o punto (1.6). */
private fun leerPrecio(texto: String): PrecioLeido? {
    if (texto.isBlank()) return PrecioLeido(null)
    return texto.replace(',', '.').toBigDecimalOrNull()?.let(::PrecioLeido)
}

/** Como lo escribiria el trabajador: "350" si es entero, "350.50" si no. */
private fun textoDePrecio(cantidad: BigDecimal): String {
    val limpia = cantidad.stripTrailingZeros()
    return if (limpia.scale() <= 0) limpia.toBigInteger().toString() else cantidad.setScale(2, RoundingMode.HALF_UP).toPlainString()
}

/** El boton de IA de P-13 necesita titulo y categoria (1.9). */
private fun EstadoServicio.conIaRecalculada(): EstadoServicio =
    copy(ia = ia.recalcular(faltanDatos = titulo.isBlank() || categoriaId == null))
