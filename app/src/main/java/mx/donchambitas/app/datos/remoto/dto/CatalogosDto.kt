package mx.donchambitas.app.datos.remoto.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.Municipio

/** Fila de public.categorias. */
@Serializable
data class CategoriaDto(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val icono: String? = null,
    val activa: Boolean,
    val orden: Int
) {
    fun aCategoria(): Categoria = Categoria(id, nombre, descripcion, icono, activa, orden)
}

/** Fila de public.estados. */
@Serializable
data class EstadoDto(val id: Int, val nombre: String, val clave: String) {
    fun aEstado(): Estado = Estado(id, nombre, clave)
}

/** Fila de public.municipios. */
@Serializable
data class MunicipioDto(
    val id: Int,
    @SerialName("estado_id") val estadoId: Int,
    val nombre: String
) {
    fun aMunicipio(): Municipio = Municipio(id, estadoId, nombre)
}
