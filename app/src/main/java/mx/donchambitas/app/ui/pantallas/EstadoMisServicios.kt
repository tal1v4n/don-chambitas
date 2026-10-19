package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.util.TipoError

/**
 * Estado inmutable de la pantalla Mis servicios (P-12).
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 3.2.
 *
 * @property servicios Ya ordenados con [ordenarMisServicios].
 * @property categorias Nombre de cada categoria por id, para la tarjeta.
 * @property servicioEnAcciones Servicio cuya hoja de acciones esta abierta.
 * @property servicioPorEliminar Servicio cuya confirmacion de eliminar esta abierta.
 * @property procesandoId Servicio que se esta pausando, reactivando o eliminando: su tarjeta no responde.
 * @property aviso Mensaje de un solo uso para el SnackbarHost.
 */
data class EstadoMisServicios(
    val cargando: Boolean = true,
    val error: TipoError? = null,
    val servicios: List<Servicio> = emptyList(),
    val categorias: Map<Int, String> = emptyMap(),
    val servicioEnAcciones: Servicio? = null,
    val servicioPorEliminar: Servicio? = null,
    val procesandoId: String? = null,
    @StringRes val aviso: Int? = null
) {
    val pausados: Int get() = servicios.count { !it.activo }
}

/** Activos primero y pausados despues; en cada grupo, el editado mas recientemente arriba (3.1). */
fun ordenarMisServicios(servicios: List<Servicio>): List<Servicio> =
    servicios.sortedWith(compareByDescending<Servicio> { it.activo }.thenByDescending { it.actualizadoEn })

/** Cuales de los dos precios trae un servicio, para elegir la frase de la tabla 3.4. */
sealed interface PrecioServicio {
    data class Rango(val desde: String, val hasta: String) : PrecioServicio
    data class Desde(val desde: String) : PrecioServicio
    data class Hasta(val hasta: String) : PrecioServicio
}

/** Nulo cuando no hay ningun precio: la tarjeta se muestra sin el (HU-09). */
fun precioDeServicio(desde: BigDecimal?, hasta: BigDecimal?): PrecioServicio? = when {
    desde != null && hasta != null -> PrecioServicio.Rango(formatearCantidad(desde), formatearCantidad(hasta))
    desde != null -> PrecioServicio.Desde(formatearCantidad(desde))
    hasta != null -> PrecioServicio.Hasta(formatearCantidad(hasta))
    else -> null
}

private val LOCALE_MEXICO: Locale = Locale.forLanguageTag("es-MX")

/** Sin decimales si la cantidad es entera, con dos si no, y con separador de miles de es-MX (3.4). */
fun formatearCantidad(cantidad: BigDecimal): String {
    val entera = cantidad.stripTrailingZeros().scale() <= 0
    val formato = NumberFormat.getNumberInstance(LOCALE_MEXICO).apply {
        minimumFractionDigits = if (entera) 0 else 2
        maximumFractionDigits = if (entera) 0 else 2
        roundingMode = RoundingMode.HALF_UP
    }
    return formato.format(cantidad)
}
