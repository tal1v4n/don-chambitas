package mx.donchambitas.app.datos.remoto.supabase

import io.ktor.http.ContentType

private const val LARGO_FIRMA_WEBP = 12

/**
 * Los tres tipos que admite la cubeta servicios (03_almacenamiento.sql).
 * Las fotos de un servicio se suben tal como se eligieron, sin recomprimir
 * (DISENO-PERFIL-SERVICIOS.md 4.2), asi que el tipo se lee de sus primeros
 * bytes (CONTRATOS-API.md, "subirFoto").
 */
enum class TipoImagen(val contentType: ContentType, val extension: String) {
    JPEG(ContentType.Image.JPEG, "jpg"),
    PNG(ContentType.Image.PNG, "png"),
    WEBP(ContentType("image", "webp"), "webp");

    companion object {
        /** Nulo si no es ninguno de los tres: esa imagen no se sube. */
        fun deBytes(bytes: ByteArray): TipoImagen? = when {
            bytes.empiezaCon(0xFF, 0xD8, 0xFF) -> JPEG
            bytes.empiezaCon(0x89, 0x50, 0x4E, 0x47) -> PNG
            bytes.size >= LARGO_FIRMA_WEBP &&
                bytes.copyOfRange(0, 4).decodeToString() == "RIFF" &&
                bytes.copyOfRange(8, 12).decodeToString() == "WEBP" -> WEBP
            else -> null
        }

        private fun ByteArray.empiezaCon(vararg firma: Int): Boolean =
            size >= firma.size && firma.indices.all { (this[it].toInt() and 0xFF) == firma[it] }
    }
}
