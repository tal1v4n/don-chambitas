package mx.donchambitas.app.datos.falso

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioAuthFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioAuth {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioAuth: $it") }
    }

    override suspend fun registrar(
        correo: String,
        contrasena: String,
        nombre: String,
        apellidos: String,
        telefono: String?,
        rol: RolUsuario
    ): Resultado<Sesion> {
        verificarSimulacion()?.let { return it }

        if (fuente.usuarios.any { it.correo.equals(correo, ignoreCase = true) }) {
            return Resultado.Error(TipoError.CORREO_DUPLICADO, "El correo ya está registrado, inicia sesión")
        }

        val nuevoId = "usr-${UUID.randomUUID().toString().take(8)}"
        val ahora = Instant.now()
        val nuevoUsuario = Usuario(
            id = nuevoId,
            correo = correo,
            nombre = nombre,
            apellidos = apellidos,
            telefono = telefono,
            rol = rol,
            fotoUrl = null,
            activo = true,
            creadoEn = ahora,
            actualizadoEn = ahora
        )
        fuente.usuarios.add(nuevoUsuario)
        val sesion = Sesion(nuevoUsuario)
        fuente.fijarSesionActiva(sesion)
        return Resultado.Exito(sesion)
    }

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Resultado<Sesion> {
        verificarSimulacion()?.let { return it }

        val usuario = fuente.usuarios.firstOrNull { it.correo.equals(correo, ignoreCase = true) }
        return if (usuario != null) {
            val sesion = Sesion(usuario)
            fuente.fijarSesionActiva(sesion)
            Resultado.Exito(sesion)
        } else {
            Resultado.Error(TipoError.AUTENTICACION, "Correo o contraseña incorrectos")
        }
    }

    override suspend fun recuperarContrasena(correo: String): Resultado<Unit> {
        verificarSimulacion()?.let { return it }
        // Segun CONTRATOS-API.md, siempre reporta exito para evitar fuga de informacion
        return Resultado.Exito(Unit)
    }

    override suspend fun cambiarContrasena(nueva: String): Resultado<Unit> {
        verificarSimulacion()?.let { return it }
        val sesion = fuente.sesionActiva.value
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesión activa")
        return Resultado.Exito(Unit)
    }

    override suspend fun cerrarSesion(): Resultado<Unit> {
        verificarSimulacion()?.let { return it }
        fuente.fijarSesionActiva(null)
        return Resultado.Exito(Unit)
    }

    override fun sesionActual(): Flow<Sesion?> = fuente.sesionActiva
}
