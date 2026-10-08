package mx.donchambitas.app.datos.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.auth.user.UserSession
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * El almacen con un cifrador falso: el Keystore real solo existe en el
 * dispositivo y se prueba aparte en CifradorSesionTest.
 */
class AlmacenSesionCifradaTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    /** "Cifra" volteando el texto, para poder comprobar que nada llega en claro. */
    private class CifradorFalso : CifradorSesion {
        override fun cifrar(texto: String) = PREFIJO + texto.reversed()
        override fun descifrar(cifrado: String) =
            if (cifrado.startsWith(PREFIJO)) cifrado.removePrefix(PREFIJO).reversed() else null

        companion object {
            const val PREFIJO = "cifrado:"
        }
    }

    private class SesionHeredadaFalsa(var texto: String? = null) : SesionHeredada {
        override fun leer() = texto
        override fun borrar() {
            texto = null
        }
    }

    private val json = Json { encodeDefaults = true }
    private val llaveSesion = stringPreferencesKey("sesion")

    private lateinit var heredada: SesionHeredadaFalsa
    private val almacen by lazy {
        PreferenceDataStoreFactory.create {
            File(carpeta.root, "$ARCHIVO_SESION_CIFRADA.preferences_pb")
        }
    }
    private lateinit var sesiones: AlmacenSesionCifrada

    private fun sesion(token: String = "token-acceso-secreto") = UserSession(
        accessToken = token,
        refreshToken = "token-refresco-secreto",
        expiresIn = 3600,
        tokenType = "bearer"
    )

    private suspend fun guardadoEnDisco(): String? = almacen.data.first()[llaveSesion]

    @Before
    fun preparar() {
        heredada = SesionHeredadaFalsa()
        sesiones = AlmacenSesionCifrada(almacen, CifradorFalso(), heredada)
    }

    @Test
    fun debeDevolverLaMismaSesion_cuandoSeGuardaYSeCarga() = runTest {
        val original = sesion()

        sesiones.saveSession(original)
        val cargada = sesiones.loadSession()

        assertNotNull(cargada)
        assertEquals(original.accessToken, cargada!!.accessToken)
        assertEquals(original.refreshToken, cargada.refreshToken)
        assertEquals(original.expiresAt, cargada.expiresAt)
    }

    @Test
    fun debeGuardarSinElTokenEnClaro_cuandoSeGuardaLaSesion() = runTest {
        sesiones.saveSession(sesion())

        val enDisco = guardadoEnDisco()

        assertNotNull(enDisco)
        assertFalse(enDisco!!.contains("token-acceso-secreto"))
        assertFalse(enDisco.contains("token-refresco-secreto"))
    }

    @Test
    fun debeQuedarSinSesion_cuandoSeBorra() = runTest {
        sesiones.saveSession(sesion())

        sesiones.deleteSession()

        assertNull(sesiones.loadSession())
        assertNull(guardadoEnDisco())
    }

    @Test
    fun debeDarSinSesionYBorrarLoGuardado_cuandoNoSePuedeDescifrar() = runTest {
        almacen.edit { it[llaveSesion] = "basura de otro telefono" }

        assertNull(sesiones.loadSession())
        assertNull(guardadoEnDisco())
    }

    @Test
    fun debeDarSinSesion_cuandoLoDescifradoNoEsUnaSesion() = runTest {
        almacen.edit { it[llaveSesion] = CifradorFalso().cifrar("{no es json") }

        assertNull(sesiones.loadSession())
        assertNull(guardadoEnDisco())
    }

    @Test
    fun debeMigrarLaSesionEnClaroYBorrarla_cuandoNoHaySesionCifrada() = runTest {
        heredada.texto = json.encodeToString(sesion(token = "token-heredado"))

        val cargada = sesiones.loadSession()

        assertEquals("token-heredado", cargada?.accessToken)
        assertNull("La copia en claro se borra", heredada.texto)
        assertFalse(guardadoEnDisco()!!.contains("token-heredado"))
        assertEquals("token-heredado", sesiones.loadSession()?.accessToken)
    }

    @Test
    fun debeBorrarLaSesionEnClaro_cuandoNoSePuedeLeer() = runTest {
        heredada.texto = "{rota"

        assertNull(sesiones.loadSession())
        assertNull(heredada.texto)
    }

    @Test
    fun debeBorrarLaSesionEnClaro_cuandoYaHayUnaCifrada() = runTest {
        sesiones.saveSession(sesion())
        heredada.texto = json.encodeToString(sesion(token = "token-viejo"))

        val cargada = sesiones.loadSession()

        assertEquals("token-acceso-secreto", cargada?.accessToken)
        assertNull(heredada.texto)
    }

    @Test
    fun debeDarSinSesion_cuandoNuncaSeGuardoNada() = runTest {
        assertNull(sesiones.loadSession())
    }
}
