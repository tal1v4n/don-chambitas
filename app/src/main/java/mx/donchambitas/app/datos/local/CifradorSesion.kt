package mx.donchambitas.app.datos.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifra el texto de la sesion antes de que toque el disco (DEC-30).
 *
 * Es interfaz para que [AlmacenSesionCifrada] se pruebe en la JVM con un
 * cifrador falso: el Keystore solo existe en el dispositivo.
 */
interface CifradorSesion {

    fun cifrar(texto: String): String

    /**
     * Devuelve null si no se puede descifrar: la llave ya no existe, el dato
     * vino de otro telefono o alguien lo altero. Para quien llama es "no hay
     * sesion", nunca una excepcion.
     */
    fun descifrar(cifrado: String): String?
}

private const val PROVEEDOR_KEYSTORE = "AndroidKeyStore"
private const val ALIAS_LLAVE = "sesion_supabase"
private const val TRANSFORMACION = "AES/GCM/NoPadding"
private const val BITS_LLAVE = 256
private const val BITS_ETIQUETA_GCM = 128
private const val BYTES_IV_GCM = 12

/**
 * AES-256-GCM con una llave que se genera dentro del Android Keystore y nunca
 * sale de ahi. Es la opcion A del ticket de S2-T08: sin dependencia nueva.
 *
 * El resultado es Base64 de IV + texto cifrado. El IV lo genera el Cipher en
 * cada cifrado; reutilizarlo con la misma llave rompe GCM.
 */
class CifradorKeystore : CifradorSesion {

    override fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, llave())
        val datos = cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(cifrador.iv + datos)
    }

    override fun descifrar(cifrado: String): String? = try {
        val bytes = Base64.getDecoder().decode(cifrado)
        if (bytes.size <= BYTES_IV_GCM) {
            null
        } else {
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(
                Cipher.DECRYPT_MODE,
                llave(),
                GCMParameterSpec(BITS_ETIQUETA_GCM, bytes, 0, BYTES_IV_GCM)
            )
            String(cifrador.doFinal(bytes, BYTES_IV_GCM, bytes.size - BYTES_IV_GCM), Charsets.UTF_8)
        }
    } catch (e: GeneralSecurityException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    } catch (e: ProviderException) {
        null
    }

    private fun llave(): SecretKey {
        val almacen = KeyStore.getInstance(PROVEEDOR_KEYSTORE).apply { load(null) }
        (almacen.getKey(ALIAS_LLAVE, null) as? SecretKey)?.let { return it }

        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEEDOR_KEYSTORE)
        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS_LLAVE,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(BITS_LLAVE)
                .build()
        )
        return generador.generateKey()
    }
}
