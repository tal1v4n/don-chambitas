package mx.donchambitas.app.datos.falso

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioUsuarioFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioUsuario {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioUsuario: $it") }
    }

    override suspend fun obtenerMiUsuario(): Resultado<Usuario> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val usuario = fuente.usuarios.firstOrNull { it.id == usuarioId }
            ?: return Resultado.Error(TipoError.AUTENTICACION, "Usuario no encontrado")

        return Resultado.Exito(usuario)
    }

    override suspend fun actualizarMiUsuario(
        nombre: String,
        apellidos: String,
        telefono: String?
    ): Resultado<Usuario> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.usuarios.indexOfFirst { it.id == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.AUTENTICACION, "Usuario no encontrado")
        }

        val actual = fuente.usuarios[index]
        val actualizado = actual.copy(
            nombre = nombre,
            apellidos = apellidos,
            telefono = telefono,
            actualizadoEn = Instant.now()
        )
        fuente.usuarios[index] = actualizado
        fuente.fijarSesionActiva(Sesion(actualizado))
        return Resultado.Exito(actualizado)
    }

    override suspend fun subirFotoPerfil(bytes: ByteArray): Resultado<String> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.usuarios.indexOfFirst { it.id == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.AUTENTICACION, "Usuario no encontrado")
        }

        val urlFalsa = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&mock=$usuarioId"
        val actual = fuente.usuarios[index]
        val actualizado = actual.copy(fotoUrl = urlFalsa, actualizadoEn = Instant.now())
        fuente.usuarios[index] = actualizado
        fuente.fijarSesionActiva(Sesion(actualizado))
        return Resultado.Exito(urlFalsa)
    }
}
