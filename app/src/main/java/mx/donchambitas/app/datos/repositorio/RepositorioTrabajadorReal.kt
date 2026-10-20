package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mx.donchambitas.app.datos.remoto.dto.HabilidadDto
import mx.donchambitas.app.datos.remoto.dto.NombreHabilidadDto
import mx.donchambitas.app.datos.remoto.dto.PerfilEscrituraDto
import mx.donchambitas.app.datos.remoto.dto.PerfilPublicoDto
import mx.donchambitas.app.datos.remoto.dto.PerfilTrabajadorDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.dominio.modelo.PerfilHabilidad
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioTrabajador contra perfiles_trabajador y perfil_habilidades (S3-T09).
 * Implementa la seccion "Perfil del trabajador" de docs/tecnico/CONTRATOS-API.md;
 * cualquier diferencia con el contrato es un defecto de esta clase o un
 * hallazgo para el lider, no una decision local.
 *
 * No valida ni normaliza: eso es de ValidacionesTrabajador y del ViewModel.
 * Toda lectura propia lleva el filtro del uid: las politicas dejan leer los
 * perfiles y las habilidades de todos, porque P-07 los necesita.
 */
@Singleton
class RepositorioTrabajadorReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>
) : RepositorioTrabajador {

    private val supabase get() = cliente.get()

    /** Sin perfil, o con la cuenta inactiva, la funcion devuelve null con 200. */
    override suspend fun obtenerPerfilPublico(id: String): Resultado<PerfilPublicoTrabajador> = intentar {
        supabase.postgrest
            .rpc(FUNCION_PERFIL_PUBLICO, buildJsonObject { put("p_trabajador_id", id) })
            .decodeAsOrNull<PerfilPublicoDto>()
            ?.let { Resultado.Exito(it.aPerfilPublico()) }
            ?: Resultado.Error(TipoError.VALIDACION, "Trabajador no encontrado")
    }

    /** Cero filas es Exito(null): sin perfil es el estado de quien acaba de registrarse (HU-06). */
    override suspend fun obtenerMiPerfil(): Resultado<PerfilTrabajador?> = conMiId { uid ->
        val perfil = supabase.from(TABLA_PERFILES)
            .select { filter { eq("usuario_id", uid) } }
            .decodeList<PerfilTrabajadorDto>()
            .firstOrNull()
        Resultado.Exito(perfil?.aPerfilTrabajador())
    }

    override suspend fun obtenerMisHabilidades(): Resultado<List<String>> = conMiId { uid ->
        val habilidades = supabase.from(TABLA_HABILIDADES)
            .select(Columns.list("habilidad")) {
                filter { eq("perfil_id", uid) }
                order("habilidad", Order.ASCENDING)
            }
            .decodeList<NombreHabilidadDto>()
        Resultado.Exito(habilidades.map { it.habilidad })
    }

    /**
     * Upsert de ocho columnas: el rol lo pone la base y no admite otro, y las
     * fechas tambien. El uid sale de la sesion, no de perfil.usuarioId.
     */
    override suspend fun guardarMiPerfil(perfil: PerfilTrabajador): Resultado<PerfilTrabajador> = conMiId { uid ->
        val guardado = supabase.from(TABLA_PERFILES)
            .upsert(
                PerfilEscrituraDto(
                    usuarioId = uid,
                    titulo = perfil.titulo,
                    descripcion = perfil.descripcion,
                    experienciaAnios = perfil.experienciaAnios,
                    telefonoContacto = perfil.telefonoContacto,
                    estadoId = perfil.estadoId,
                    municipioId = perfil.municipioId,
                    disponible = perfil.disponible
                )
            ) {
                onConflict = "usuario_id"
                select()
            }
            .decodeSingle<PerfilTrabajadorDto>()
        Resultado.Exito(guardado.aPerfilTrabajador())
    }

    /**
     * Borrar todas y luego insertar. No es atomico (H-26): si el insert falla,
     * el perfil queda sin habilidades y P-11 ofrece reintentar.
     */
    override suspend fun reemplazarHabilidades(lista: List<String>): Resultado<List<PerfilHabilidad>> =
        conMiId { uid ->
            supabase.from(TABLA_HABILIDADES).delete { filter { eq("perfil_id", uid) } }
            if (lista.isEmpty()) return@conMiId Resultado.Exito(emptyList())
            val insertadas = supabase.from(TABLA_HABILIDADES)
                .insert(lista.map { HabilidadDto(perfilId = uid, habilidad = it) }) { select() }
                .decodeList<HabilidadDto>()
            Resultado.Exito(insertadas.map { PerfilHabilidad(it.perfilId, it.habilidad) })
        }

    /** El uid sale de la sesion que ya tiene supabase-kt, sin ir a la red. */
    private suspend fun <T> conMiId(operacion: suspend (String) -> Resultado<T>): Resultado<T> = intentar {
        val uid = supabase.auth.currentUserOrNull()?.id
            ?: return@intentar Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
        operacion(uid)
    }

    private suspend fun <T> intentar(operacion: suspend () -> Resultado<T>): Resultado<T> =
        try {
            operacion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private companion object {
        const val TABLA_PERFILES = "perfiles_trabajador"
        const val TABLA_HABILIDADES = "perfil_habilidades"
        const val FUNCION_PERFIL_PUBLICO = "fn_perfil_publico_trabajador"
    }
}
