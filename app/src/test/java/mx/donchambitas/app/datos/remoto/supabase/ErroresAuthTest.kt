package mx.donchambitas.app.datos.remoto.supabase

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import mx.donchambitas.app.util.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Una prueba por fila de la tabla "Errores de autenticacion" de
 * docs/tecnico/CONTRATOS-API.md.
 */
class ErroresAuthTest {

    private fun auth(codigo: String, estado: Int = 400) = AuthRestException(codigo, "mensaje de Auth", estado)

    private fun tipoDe(error: Throwable) = traducirErrorAuth(error).tipo

    @Test
    fun debeSerRed_cuandoNoHayConexion() {
        assertEquals(TipoError.RED, tipoDe(HttpRequestException("sin red", HttpRequestBuilder())))
    }

    @Test
    fun debeSerRed_cuandoSeAgotaElTiempo() {
        assertEquals(TipoError.RED, tipoDe(HttpRequestTimeoutException("https://proyecto.supabase.co", 10_000L)))
    }

    @Test
    fun debeSerAutenticacion_cuandoLasCredencialesSonRechazadas() {
        assertEquals(TipoError.AUTENTICACION, tipoDe(auth("invalid_credentials")))
    }

    @Test
    fun debeSerAutenticacion_cuandoElCorreoNoEstaConfirmado() {
        assertEquals(TipoError.AUTENTICACION, tipoDe(auth("email_not_confirmed")))
    }

    @Test
    fun debeSerAutenticacion_cuandoLaCuentaEstaBloqueada() {
        assertEquals(TipoError.AUTENTICACION, tipoDe(auth("user_banned")))
    }

    @Test
    fun debeSerAutenticacion_cuandoLaSesionYaNoVale() {
        listOf("session_not_found", "session_expired", "refresh_token_not_found", "bad_jwt").forEach {
            assertEquals(it, TipoError.AUTENTICACION, tipoDe(auth(it, 401)))
        }
    }

    @Test
    fun debeSerCorreoDuplicado_cuandoElCorreoYaExiste() {
        assertEquals(TipoError.CORREO_DUPLICADO, tipoDe(auth("user_already_exists", 422)))
        assertEquals(TipoError.CORREO_DUPLICADO, tipoDe(auth("email_exists", 422)))
    }

    @Test
    fun debeSerValidacion_cuandoLaContrasenaEsDebil() {
        val debil = AuthWeakPasswordException("Password should be at least 8 characters", 422, listOf("length"))
        assertEquals(TipoError.VALIDACION, tipoDe(debil))
    }

    @Test
    fun debeSerValidacion_cuandoLaContrasenaEsLaMisma() {
        assertEquals(TipoError.VALIDACION, tipoDe(auth("same_password", 422)))
    }

    /** email_address_invalid no esta en AuthErrorCode de 3.0.3: se reconoce por el texto crudo. */
    @Test
    fun debeSerValidacion_cuandoElCorreoNoPasaLaValidacionDelServidor() {
        assertEquals(TipoError.VALIDACION, tipoDe(auth("validation_failed")))
        assertEquals(TipoError.VALIDACION, tipoDe(auth("email_address_invalid")))
    }

    @Test
    fun debeSerServidor_cuandoSeRebasaElLimiteDePeticiones() {
        assertEquals(TipoError.SERVIDOR, tipoDe(auth("over_request_rate_limit", 429)))
    }

    @Test
    fun debeSerServidor_cuandoElAltaEstaDesactivadaEnElProyecto() {
        assertEquals(TipoError.SERVIDOR, tipoDe(auth("signup_disabled", 422)))
        assertEquals(TipoError.SERVIDOR, tipoDe(auth("email_provider_disabled", 422)))
    }

    @Test
    fun debeSerServidor_cuandoAuthFallaPorDentro() {
        assertEquals(TipoError.SERVIDOR, tipoDe(auth("unexpected_failure", 500)))
        assertEquals(TipoError.SERVIDOR, tipoDe(RestException("otro", null, 503, "caido")))
    }

    @Test
    fun debeSerDesconocido_cuandoElErrorNoEstaEnLaTabla() {
        assertEquals(TipoError.DESCONOCIDO, tipoDe(auth("codigo_que_no_existe", 400)))
        assertEquals(TipoError.DESCONOCIDO, tipoDe(IllegalStateException("otra cosa")))
    }

    @Test
    fun debeSerServidor_cuandoLaFichaNoLlega() {
        assertEquals(TipoError.SERVIDOR, traducirErrorFicha(NoSuchElementException("sin filas")).tipo)
        assertEquals(TipoError.SERVIDOR, traducirErrorFicha(RestException("PGRST116", null, 406, "sin filas")).tipo)
    }

    @Test
    fun debeSerRed_cuandoLaFichaNoLlegaPorFaltaDeRed() {
        assertEquals(TipoError.RED, traducirErrorFicha(HttpRequestException("sin red", HttpRequestBuilder())).tipo)
    }

    @Test
    fun debeContarComoRechazoQueDelataLaCuenta_soloLosCuatroCodigosDelContrato() {
        assertTrue(esRechazoQueDelataLaCuenta(auth("over_email_send_rate_limit", 429)))
        assertTrue(esRechazoQueDelataLaCuenta(auth("over_request_rate_limit", 429)))
        assertTrue(esRechazoQueDelataLaCuenta(auth("email_address_invalid", 400)))
        assertTrue(esRechazoQueDelataLaCuenta(auth("email_address_not_authorized", 400)))
        assertFalse(esRechazoQueDelataLaCuenta(auth("unexpected_failure", 500)))
        assertFalse(esRechazoQueDelataLaCuenta(auth("validation_failed", 400)))
        assertFalse(esRechazoQueDelataLaCuenta(HttpRequestException("sin red", HttpRequestBuilder())))
    }

    /** Fuera de recuperarContrasena, email_address_invalid sigue siendo VALIDACION. */
    @Test
    fun debeSeguirTraduciendoAValidacion_cuandoElCorreoInvalidoLlegaEnOtraOperacion() {
        assertEquals(TipoError.VALIDACION, traducirErrorAuth(auth("email_address_invalid", 400)).tipo)
    }
}
