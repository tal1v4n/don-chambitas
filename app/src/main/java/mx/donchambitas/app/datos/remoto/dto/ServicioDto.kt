package mx.donchambitas.app.datos.remoto.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.datos.remoto.supabase.SerializadorBigDecimal
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto

/**
 * Fila de public.servicios con sus servicio_fotos embebidas, si se pidieron
 * (CONTRATOS-API.md, "obtenerMisServicios", H-18). Sin el embebido, fotos
 * queda vacia, que es lo que devuelve crear.
 */
@Serializable
data class ServicioDto(
    val id: String,
    @SerialName("perfil_id") val perfilId: String,
    @SerialName("categoria_id") val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_desde") val precioDesde: BigDecimal? = null,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_hasta") val precioHasta: BigDecimal? = null,
    @SerialName("unidad_precio") val unidadPrecio: String? = null,
    val activo: Boolean,
    @SerialName("creado_en") val creadoEn: String,
    @SerialName("actualizado_en") val actualizadoEn: String,
    @SerialName("servicio_fotos") val fotos: List<ServicioFotoDto> = emptyList()
) {
    /** Las fotos se vuelven a ordenar aqui, para no depender del order sobre la tabla embebida. */
    fun aServicio(): Servicio = Servicio(
        id = id,
        perfilId = perfilId,
        categoriaId = categoriaId,
        titulo = titulo,
        descripcion = descripcion,
        precioDesde = precioDesde,
        precioHasta = precioHasta,
        unidadPrecio = unidadPrecio,
        activo = activo,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant(),
        actualizadoEn = OffsetDateTime.parse(actualizadoEn).toInstant(),
        fotos = fotos.map { it.aServicioFoto() }.sortedBy { it.posicion }
    )
}

/** Fila de public.servicio_fotos. */
@Serializable
data class ServicioFotoDto(
    val id: String,
    @SerialName("servicio_id") val servicioId: String,
    val url: String,
    val posicion: Int,
    @SerialName("creado_en") val creadoEn: String
) {
    fun aServicioFoto(): ServicioFoto = ServicioFoto(
        id = id,
        servicioId = servicioId,
        url = url,
        posicion = posicion,
        creadoEn = OffsetDateTime.parse(creadoEn).toInstant()
    )
}

/**
 * Las siete columnas que escribe crear. Sin valores por omision, como
 * PerfilEscrituraDto: un precio null tiene que viajar como null.
 */
@Serializable
data class ServicioEscrituraDto(
    @SerialName("perfil_id") val perfilId: String,
    @SerialName("categoria_id") val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_desde") val precioDesde: BigDecimal?,
    @Serializable(with = SerializadorBigDecimal::class)
    @SerialName("precio_hasta") val precioHasta: BigDecimal?,
    @SerialName("unidad_precio") val unidadPrecio: String?
)

/** Las tres columnas que escribe subirFoto despues de subir el archivo. */
@Serializable
data class FotoEscrituraDto(
    @SerialName("servicio_id") val servicioId: String,
    val url: String,
    val posicion: Int
)

/** Solo la URL, que es lo que eliminar lee antes de borrar (H-06). */
@Serializable
data class UrlDto(val url: String)
