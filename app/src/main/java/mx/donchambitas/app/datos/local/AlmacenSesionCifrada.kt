package mx.donchambitas.app.datos.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import java.security.GeneralSecurityException
import java.security.ProviderException
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Archivo de DataStore donde vive la sesion cifrada; las reglas de respaldo lo excluyen. */
const val ARCHIVO_SESION_CIFRADA = "sesion_cifrada"

private val LLAVE_SESION = stringPreferencesKey("sesion")

private const val ETIQUETA_LOG = "AlmacenSesionCifrada"

// encodeDefaults es requisito de supabase-kt para UserSession: sin el,
// expiresAt no se guarda y la sesion se recalcula como recien emitida.
private val jsonSesion = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * La sesion de Supabase Auth guardada cifrada (DEC-30, S2-T08). Sustituye al
 * SettingsSessionManager de supabase-kt, que la dejaba en texto plano en las
 * SharedPreferences de la aplicacion.
 *
 * No hay segunda copia: supabase-kt sigue siendo quien decide cuando guardar,
 * cargar y borrar; aqui solo cambia donde y como.
 */
class AlmacenSesionCifrada(
    private val almacen: DataStore<Preferences>,
    private val cifrador: CifradorSesion,
    private val sesionHeredada: SesionHeredada
) : SessionManager {

    override suspend fun saveSession(session: UserSession) {
        val cifrada = try {
            cifrador.cifrar(jsonSesion.encodeToString(session))
        } catch (e: GeneralSecurityException) {
            // Sin Keystore no se guarda nada en claro: la sesion sigue viva en
            // memoria y al reabrir la aplicacion se pide entrar otra vez.
            Log.e(ETIQUETA_LOG, "No se pudo cifrar la sesion", e)
            return
        } catch (e: ProviderException) {
            // Algunos Keystore de fabricante fallan con esta, que no es
            // GeneralSecurityException. Mismo trato.
            Log.e(ETIQUETA_LOG, "No se pudo cifrar la sesion", e)
            return
        }
        almacen.edit { it[LLAVE_SESION] = cifrada }
        sesionHeredada.borrar()
    }

    override suspend fun loadSession(): UserSession? {
        val cifrada = almacen.data.first()[LLAVE_SESION]
        if (cifrada == null) return migrarSesionHeredada()

        // Lo que no se puede descifrar o leer se borra: guardarlo no sirve de
        // nada y para el usuario es simplemente "no hay sesion".
        val sesion = cifrador.descifrar(cifrada)?.let(::decodificar)
        if (sesion == null) almacen.edit { it.remove(LLAVE_SESION) }
        sesionHeredada.borrar()
        return sesion
    }

    override suspend fun deleteSession() {
        almacen.edit { it.remove(LLAVE_SESION) }
        sesionHeredada.borrar()
    }

    /**
     * Quien ya tenia sesion antes de S2-T08 la tiene en texto plano. Se pasa
     * cifrada una sola vez, para no obligarlo a entrar de nuevo, y la copia en
     * claro se borra.
     */
    private suspend fun migrarSesionHeredada(): UserSession? {
        val sesion = sesionHeredada.leer()?.let(::decodificar)
        if (sesion != null) saveSession(sesion) else sesionHeredada.borrar()
        return sesion
    }

    private fun decodificar(texto: String): UserSession? = try {
        jsonSesion.decodeFromString<UserSession>(texto)
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}

/** La copia en texto plano que dejaba SettingsSessionManager antes de S2-T08. */
interface SesionHeredada {
    fun leer(): String?
    fun borrar()
}

/**
 * SettingsSessionManager guardaba la sesion en las SharedPreferences por
 * omision, con la llave `sb-<proyecto>-session`. Se busca por forma y no por
 * nombre exacto, porque el nombre sale de la URL del proyecto.
 */
class SesionHeredadaDePreferencias(private val preferencias: SharedPreferences) : SesionHeredada {

    private fun llave(): String? =
        preferencias.all.keys.firstOrNull { it.startsWith("sb-") && it.endsWith("-session") }

    override fun leer(): String? = llave()?.let { preferencias.getString(it, null) }

    override fun borrar() {
        val llave = llave() ?: return
        preferencias.edit().remove(llave).apply()
    }
}

/**
 * Arma el almacen con sus piezas reales. Se llama una sola vez, desde el
 * SupabaseClient singleton: dos DataStore sobre el mismo archivo fallan.
 */
fun crearAlmacenSesionCifrada(contexto: Context): AlmacenSesionCifrada = AlmacenSesionCifrada(
    almacen = PreferenceDataStoreFactory.create(
        // Un archivo danado es "no hay sesion", igual que un dato que no se
        // puede descifrar: sin esto DataStore lanza al leer y Auth no arranca.
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { contexto.preferencesDataStoreFile(ARCHIVO_SESION_CIFRADA) }
    ),
    cifrador = CifradorKeystore(),
    sesionHeredada = SesionHeredadaDePreferencias(
        contexto.getSharedPreferences("${contexto.packageName}_preferences", Context.MODE_PRIVATE)
    )
)
