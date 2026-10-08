package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mx.donchambitas.app.datos.remoto.dto.UsuarioDto
import mx.donchambitas.app.datos.remoto.supabase.ConfiguracionSupabase
import mx.donchambitas.app.datos.remoto.supabase.esRechazoQueDelataLaCuenta
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorAuth
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorFicha
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioAuth contra Supabase Auth.
 * Implementa la seccion "Autenticacion" de docs/tecnico/CONTRATOS-API.md,
 * escrita contra supabase-kt 3.0.3; cualquier diferencia con el contrato es
 * un defecto de esta clase o un hallazgo para el lider, no una decision local.
 */
@Singleton
class RepositorioAuthReal @Inject constructor(
    // Perezoso: sin llaves en local.properties el cliente no se puede crear, y
    // asi la falla llega como Resultado.Error al pulsar el boton en vez de
    // cerrar la aplicacion al abrir P-02.
    private val cliente: Lazy<SupabaseClient>
) : RepositorioAuth {

    private val supabase get() = cliente.get()
    private val auth get() = supabase.auth

    override suspend fun registrar(
        correo: String,
        contrasena: String,
        nombre: String,
        apellidos: String,
        telefono: String?,
        rol: RolUsuario
    ): Resultado<Sesion> = ejecutar {
        // Sin confirmacion por correo (DEC-25) el alta no manda enlace, asi
        // que no hay redireccion que indicar. Las llaves de data son las que
        // lee fn_crear_usuario_desde_auth: una mal escrita no falla, el
        // trigger pone el valor por omision sin avisar.
        auth.signUpWith(Email, redirectUrl = null) {
            email = correo
            password = contrasena
            data = buildJsonObject {
                put("nombre", nombre)
                put("apellidos", apellidos)
                put("telefono", telefono)
                put("rol", rol.valor)
            }
        }
        sesionConFicha()
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Resultado<Sesion> = ejecutar {
        auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
        sesionConFicha()
    }

    override suspend fun recuperarContrasena(correo: String): Resultado<Unit> =
        try {
            auth.resetPasswordForEmail(correo, redirectUrl = ConfiguracionSupabase.ENLACE_AUTH)
            Resultado.Exito(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (esRechazoQueDelataLaCuenta(e)) Resultado.Exito(Unit) else traducirErrorAuth(e)
        }

    override suspend fun cambiarContrasena(nueva: String): Resultado<Unit> = ejecutar {
        auth.updateUser { password = nueva }
        Resultado.Exito(Unit)
    }

    override suspend fun cerrarSesion(): Resultado<Unit> = ejecutar {
        auth.signOut()
        Resultado.Exito(Unit)
    }

    /**
     * Initializing y RefreshFailure no emiten: el primero todavia no sabe si
     * hay sesion, y el segundo en 3.0.3 solo marca fallas pasajeras que la
     * biblioteca reintenta sola. Si la ficha no se puede leer tampoco se
     * emite: un tropiezo de red no es un cierre de sesion.
     */
    override fun sesionActual(): Flow<Sesion?> = flow {
        var ultima: Sesion? = null
        auth.sessionStatus.collect { estado ->
            when (estado) {
                SessionStatus.Initializing, is SessionStatus.RefreshFailure -> Unit
                is SessionStatus.NotAuthenticated -> {
                    ultima = null
                    emit(null)
                }
                is SessionStatus.Authenticated -> {
                    val id = estado.session.user?.id ?: return@collect
                    val anterior = ultima
                    // La ficha se lee una vez por usuario, no en cada refresco del token.
                    val usuario = if (anterior?.usuario?.id == id) {
                        anterior.usuario
                    } else {
                        try {
                            leerFicha(id)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (usuario != null) {
                        val sesion = Sesion(usuario)
                        ultima = sesion
                        emit(sesion)
                    }
                }
            }
        }
    }.distinctUntilChanged()

    private suspend fun sesionConFicha(): Resultado<Sesion> {
        val sesionAuth = auth.currentSessionOrNull()
            ?: return Resultado.Error(
                TipoError.SERVIDOR,
                "Auth no dejo sesion abierta: revisa que la confirmacion por correo este desactivada (DEC-25)"
            )
        val id = sesionAuth.user?.id ?: auth.currentUserOrNull()?.id
            ?: return Resultado.Error(TipoError.SERVIDOR, "La sesion de Auth no trae usuario")
        return try {
            Resultado.Exito(Sesion(leerFicha(id)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorFicha(e)
        }
    }

    // La deja pasar la politica RLS "usuario ve su propia ficha" (id = auth.uid()).
    private suspend fun leerFicha(id: String) =
        supabase.from("usuarios")
            .select { filter { eq("id", id) } }
            .decodeSingle<UsuarioDto>()
            .aUsuario()

    private suspend fun <T> ejecutar(operacion: suspend () -> Resultado<T>): Resultado<T> =
        try {
            operacion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorAuth(e)
        }
}
