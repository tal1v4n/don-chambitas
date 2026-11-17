package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mx.donchambitas.app.datos.remoto.dto.ConversacionDto
import mx.donchambitas.app.datos.remoto.dto.MensajeDto
import mx.donchambitas.app.datos.remoto.dto.MensajeEscrituraDto
import mx.donchambitas.app.datos.remoto.dto.ResumenConversacionDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.ResumenConversacion
import mx.donchambitas.app.dominio.repositorio.RepositorioChat
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioChat contra la vista vw_mis_conversaciones, mensajes, la RPC fn_abrir_conversacion y
 * Supabase Realtime (S5-T07). Implementa la seccion "Chat" de docs/tecnico/CONTRATOS-API.md;
 * cualquier diferencia con el contrato es un defecto de esta clase o un hallazgo para el lider.
 *
 * DEC-20: la conversacion la abre siempre el cliente. Ver H-58: la base no impide que un
 * trabajador abra un hilo con otro trabajador, asi que la pantalla no lo ofrece.
 */
@Singleton
class RepositorioChatReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>
) : RepositorioChat {

    private val supabase get() = cliente.get()

    /** De la vista: la otra persona, el ultimo mensaje y los sin leer en una sola lectura (H-50). */
    override suspend fun obtenerConversaciones(): Resultado<List<ResumenConversacion>> = intentar {
        val filas = supabase.from(VISTA_MIS_CONVERSACIONES)
            .select { order("ultimo_mensaje_en", Order.DESCENDING, nullsFirst = false) }
            .decodeList<ResumenConversacionDto>()
        Resultado.Exito(filas.map { it.aResumen() })
    }

    /** La RPC devuelve el id (nuevo o el que ya existia); despues se lee la fila. */
    override suspend fun abrirConversacion(trabajadorId: String, solicitudId: String?): Resultado<Conversacion> =
        intentar {
            val id = supabase.postgrest.rpc(
                FUNCION_ABRIR,
                buildJsonObject {
                    put("p_trabajador_id", trabajadorId)
                    solicitudId?.let { put("p_solicitud_id", it) }
                }
            ).decodeAs<String>()
            val filas = supabase.from(TABLA_CONVERSACIONES)
                .select { filter { eq("id", id) } }
                .decodeList<ConversacionDto>()
            filas.firstOrNull()?.let { Resultado.Exito(it.aConversacion()) }
                ?: Resultado.Error(TipoError.VALIDACION, "Conversacion no encontrada")
        }

    /** Pagina desde 1, de 30 en 30, del mas reciente al mas antiguo (CONTRATOS-API.md). */
    override suspend fun obtenerMensajes(conversacionId: String, pagina: Int): Resultado<List<Mensaje>> = intentar {
        val desde = (pagina - 1) * TAMANO_PAGINA
        val filas = supabase.from(TABLA_MENSAJES)
            .select {
                filter { eq("conversacion_id", conversacionId) }
                order("creado_en", Order.DESCENDING)
                range(desde.toLong()..(desde + TAMANO_PAGINA - 1).toLong())
            }
            .decodeList<MensajeDto>()
        Resultado.Exito(filas.map { it.aMensaje() })
    }

    /** Manda tres columnas; leido_en y creado_en los pone la base, que ademas sube el hilo. */
    override suspend fun enviar(conversacionId: String, contenido: String): Resultado<Mensaje> = conMiId { uid ->
        val fila = supabase.from(TABLA_MENSAJES)
            .insert(MensajeEscrituraDto(conversacionId, uid, contenido.trim())) { select() }
            .decodeSingle<MensajeDto>()
        Resultado.Exito(fila.aMensaje())
    }

    /** Solo los recibidos y todavia sin leer; cero filas afectadas es exito. */
    override suspend fun marcarLeidos(conversacionId: String): Resultado<Unit> = conMiId { uid ->
        supabase.from(TABLA_MENSAJES).update({ set("leido_en", Instant.now().toString()) }) {
            filter {
                eq("conversacion_id", conversacionId)
                neq("emisor_id", uid)
                filter("leido_en", FilterOperator.IS, "null")
            }
        }
        Resultado.Exito(Unit)
    }

    /**
     * INSERT sobre public.mensajes de esa conversacion. Emite tambien los propios: la pantalla los
     * reconcilia por id. Realtime aplica RLS por suscriptor.
     *
     * Al reconectar, la libreria vuelve a suscribir el canal pero no repite lo que llego mientras
     * estuvo caido: se pide la pagina 1 y se entrega de la mas antigua a la mas reciente
     * (CONTRATOS-API.md). Los repetidos los descarta la pantalla por id (S5-T14).
     */
    override fun mensajesNuevos(conversacionId: String): Flow<Mensaje> = channelFlow {
        val canal = supabase.realtime.channel(nombreUnico("mensajes-$conversacionId"))
        val inserciones = canal.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = TABLA_MENSAJES
            filter("conversacion_id", FilterOperator.EQ, conversacionId)
        }
        launch { inserciones.collect { send(it.decodeRecord<MensajeDto>().aMensaje()) } }
        try {
            canal.subscribe(blockUntilSubscribed = true)
            launch {
                alReconectar(canal) {
                    val recientes = obtenerMensajes(conversacionId, pagina = 1)
                    if (recientes is Resultado.Exito) recientes.dato.asReversed().forEach { send(it) }
                }
            }
            awaitClose()
        } finally {
            cerrar(canal)
        }
    }

    /**
     * Cada cambio de conversaciones (la base sube ultimo_mensaje_en al llegar un mensaje). Tambien
     * emite al reconectar, para que P-15 vuelva a pedir la bandeja con lo que paso mientras tanto.
     */
    override fun cambiosEnConversaciones(): Flow<Unit> = channelFlow {
        val canal = supabase.realtime.channel(nombreUnico("conversaciones-propias"))
        val cambios = canal.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLA_CONVERSACIONES
        }
        launch { cambios.collect { send(Unit) } }
        try {
            canal.subscribe(blockUntilSubscribed = true)
            launch { alReconectar(canal) { send(Unit) } }
            awaitClose()
        } finally {
            cerrar(canal)
        }
    }

    /**
     * Llama a [recuperar] cada vez que la conexion se cae y el canal vuelve a quedar suscrito.
     *
     * La caida se ve en el estado de la conexion: dura al menos el retraso de reconexion, asi que no
     * se pierde. El canal no sirve para eso: al caer la conexion la libreria lo deja en SUBSCRIBED.
     * Por eso, ya reconectado, se espera a que pase por SUBSCRIBING (la libreria lo vuelve a unir) y
     * a que quede SUBSCRIBED; si el paso por SUBSCRIBING ya ocurrio y no se alcanzo a ver, se sigue
     * tras el tope de espera. Recuperar antes de que el canal se una perderia lo que llegue entre la
     * lectura y la union.
     */
    private suspend fun alReconectar(canal: RealtimeChannel, recuperar: suspend () -> Unit) {
        var estuvoCaida = false
        supabase.realtime.status.collect { estado ->
            if (estado != Realtime.Status.CONNECTED) {
                estuvoCaida = true
                return@collect
            }
            if (!estuvoCaida) return@collect
            estuvoCaida = false
            withTimeoutOrNull(ESPERA_REUNION_MS) {
                canal.status.first { it == RealtimeChannel.Status.SUBSCRIBING }
            }
            canal.status.first { it == RealtimeChannel.Status.SUBSCRIBED }
            recuperar()
        }
    }

    /**
     * Cierra el mismo canal que se abrio. Cada suscripcion tiene su propio nombre ([nombreUnico]):
     * con un nombre fijo, cerrar una suscripcion tumbaba otra viva con el mismo tema, porque la
     * libreria y el servidor identifican el canal por su tema.
     */
    private suspend fun cerrar(canal: RealtimeChannel) = withContext(NonCancellable) {
        try {
            if (canal.status.value != RealtimeChannel.Status.UNSUBSCRIBED) canal.unsubscribe()
            supabase.realtime.removeChannel(canal)
        } catch (e: Exception) {
            // Cerrar un canal con la conexion caida no es un error de pantalla.
        }
    }

    private fun nombreUnico(base: String) = "$base-${UUID.randomUUID()}"

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
        const val VISTA_MIS_CONVERSACIONES = "vw_mis_conversaciones"
        const val TABLA_CONVERSACIONES = "conversaciones"
        const val TABLA_MENSAJES = "mensajes"
        const val FUNCION_ABRIR = "fn_abrir_conversacion"
        const val TAMANO_PAGINA = 30
        const val ESPERA_REUNION_MS = 5_000L
    }
}
