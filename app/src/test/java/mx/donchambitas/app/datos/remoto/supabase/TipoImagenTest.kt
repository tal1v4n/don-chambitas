package mx.donchambitas.app.datos.remoto.supabase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Las firmas de CONTRATOS-API.md, "subirFoto". */
class TipoImagenTest {

    @Test
    fun reconoceLosTresTiposPorSusPrimerosBytes() {
        assertEquals(TipoImagen.JPEG, TipoImagen.deBytes(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0x00)))
        assertEquals(TipoImagen.PNG, TipoImagen.deBytes(bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A)))
        assertEquals(TipoImagen.WEBP, TipoImagen.deBytes("RIFF".toByteArray() + bytes(0, 0, 0, 0) + "WEBPVP8 ".toByteArray()))
    }

    @Test
    fun rechazaLoDemasYLoQueNoAlcanzaLaFirma() {
        assertNull(TipoImagen.deBytes(byteArrayOf()))
        assertNull(TipoImagen.deBytes(bytes(0xFF, 0xD8)))
        assertNull(TipoImagen.deBytes("GIF89a".toByteArray()))
        assertNull(TipoImagen.deBytes("RIFF".toByteArray() + bytes(0, 0, 0, 0) + "WAVEfmt ".toByteArray()))
    }

    private fun bytes(vararg valores: Int) = ByteArray(valores.size) { valores[it].toByte() }
}
