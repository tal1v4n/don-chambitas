package mx.donchambitas.app.datos.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * El cifrado real contra el Android Keystore del dispositivo. En la JVM no
 * hay Keystore, por eso esta prueba es instrumentada.
 */
@RunWith(AndroidJUnit4::class)
class CifradorSesionTest {

    private val cifrador = CifradorKeystore()
    private val texto = """{"access_token":"token-acceso-secreto","refresh_token":"r"}"""

    @Test
    fun debeDevolverElMismoTexto_cuandoSeCifraYSeDescifra() {
        assertEquals(texto, cifrador.descifrar(cifrador.cifrar(texto)))
    }

    @Test
    fun debeNoDejarElTextoEnClaro_cuandoSeCifra() {
        val cifrado = cifrador.cifrar(texto)

        assertFalse(cifrado.contains("token-acceso-secreto"))
        assertFalse(String(Base64.getDecoder().decode(cifrado)).contains("token-acceso-secreto"))
    }

    /** GCM con el mismo IV dos veces rompe el cifrado; cada cifrado lleva uno nuevo. */
    @Test
    fun debeDarResultadosDistintos_cuandoSeCifraDosVecesLoMismo() {
        assertNotEquals(cifrador.cifrar(texto), cifrador.cifrar(texto))
    }

    @Test
    fun debeDarNull_cuandoElDatoFueAlterado() {
        val bytes = Base64.getDecoder().decode(cifrador.cifrar(texto))
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 1).toByte()

        assertNull(cifrador.descifrar(Base64.getEncoder().encodeToString(bytes)))
    }

    @Test
    fun debeDarNull_cuandoNoEsBase64NiTieneLargoSuficiente() {
        assertNull(cifrador.descifrar("esto no es base64 !!"))
        assertNull(cifrador.descifrar(Base64.getEncoder().encodeToString(ByteArray(4))))
    }
}
