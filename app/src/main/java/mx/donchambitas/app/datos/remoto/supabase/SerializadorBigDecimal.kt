package mx.donchambitas.app.datos.remoto.supabase

import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 * Precios de servicios, numeric(10,2) en la base (CONTRATOS-API.md, "crear").
 *
 * Se escriben como texto con punto, "350.50", y PostgREST lo convierte a
 * numeric. Se leen desde el texto del numero del JSON, nunca pasando por
 * Double, que pierde exactitud.
 */
object SerializadorBigDecimal : KSerializer<BigDecimal> {

    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val texto = (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonPrimitive?.content
            ?: decoder.decodeString()
        return texto.toBigDecimal()
    }
}
