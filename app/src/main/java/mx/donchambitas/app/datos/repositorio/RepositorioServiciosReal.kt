package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import mx.donchambitas.app.datos.remoto.dto.FotoEscrituraDto
import mx.donchambitas.app.datos.remoto.dto.ServicioDto
import mx.donchambitas.app.datos.remoto.dto.ServicioEscrituraDto
import mx.donchambitas.app.datos.remoto.dto.ServicioFotoDto
import mx.donchambitas.app.datos.remoto.dto.UrlDto
import mx.donchambitas.app.datos.remoto.supabase.TipoImagen
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorStorage
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.dominio.repositorio.RepositorioServicios
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * RepositorioServicios contra servicios, servicio_fotos y la cubeta servicios
 * (S3-T09). Implementa la seccion "Servicios" de docs/tecnico/CONTRATOS-API.md;
 * cualquier diferencia con el contrato es un defecto de esta clase o un
 * hallazgo para el lider, no una decision local.
 *
 * Storage sigue el orden de H-06: al subir, el archivo y luego la fila; al
 * borrar, la fila y luego el archivo. Ninguna fila apunta a un archivo que no
 * existe; lo peor que queda es un archivo sin fila, que nadie ve.
 */
@Singleton
class RepositorioServiciosReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>
) : RepositorioServicios {

    private val supabase get() = cliente.get()
    private val cubeta: BucketApi get() = supabase.storage.from(CUBETA_SERVICIOS)

    override suspend fun obtenerMisServicios(): Resultado<List<Servicio>> = conMiId { uid ->
        val servicios = supabase.from(TABLA_SERVICIOS)
            .select(CON_FOTOS) {
                filter { eq("perfil_id", uid) }
                order("posicion", Order.ASCENDING, referencedTable = TABLA_FOTOS)
            }
            .decodeList<ServicioDto>()
        Resultado.Exito(servicios.map { it.aServicio() })
    }

    /** Siete columnas: id, activo y las fechas los pone la base. */
    override suspend fun crear(servicio: Servicio): Resultado<Servicio> = conMiId { uid ->
        val creado = supabase.from(TABLA_SERVICIOS)
            .insert(
                ServicioEscrituraDto(
                    perfilId = uid,
                    categoriaId = servicio.categoriaId,
                    titulo = servicio.titulo,
                    descripcion = servicio.descripcion,
                    precioDesde = servicio.precioDesde,
                    precioHasta = servicio.precioHasta,
                    unidadPrecio = servicio.unidadPrecio
                )
            ) { select() }
            .decodeSingle<ServicioDto>()
        Resultado.Exito(creado.aServicio())
    }

    /** Seis columnas: activo lo cambia pausar y perfil_id no se toca. */
    override suspend fun actualizar(servicio: Servicio): Resultado<Servicio> = conMiId {
        val filas = supabase.from(TABLA_SERVICIOS)
            .update({
                set("categoria_id", servicio.categoriaId)
                set("titulo", servicio.titulo)
                set("descripcion", servicio.descripcion)
                set("precio_desde", servicio.precioDesde?.toPlainString())
                set("precio_hasta", servicio.precioHasta?.toPlainString())
                set("unidad_precio", servicio.unidadPrecio)
            }) {
                filter { eq("id", servicio.id) }
                select(CON_FOTOS)
            }
            .decodeList<ServicioDto>()
        unaFila(filas)
    }

    override suspend fun pausar(id: String, activo: Boolean): Resultado<Servicio> = conMiId {
        val filas = supabase.from(TABLA_SERVICIOS)
            .update({ set("activo", activo) }) {
                filter { eq("id", id) }
                select(CON_FOTOS)
            }
            .decodeList<ServicioDto>()
        unaFila(filas)
    }

    /** Las URL se leen antes: la cascada se lleva las filas de servicio_fotos (H-06). */
    override suspend fun eliminar(id: String): Resultado<Unit> = conMiId {
        val urls = supabase.from(TABLA_FOTOS)
            .select(Columns.list("url")) { filter { eq("servicio_id", id) } }
            .decodeList<UrlDto>()
            .map { it.url }
        val borrados = supabase.from(TABLA_SERVICIOS)
            .delete {
                filter { eq("id", id) }
                select()
            }
            .decodeList<ServicioDto>()
        if (borrados.isEmpty()) return@conMiId Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado")
        borrarArchivosSinFallar(urls)
        Resultado.Exito(Unit)
    }

    /** Sin avance (H-20): upload, no uploadAsFlow. */
    override suspend fun subirFoto(servicioId: String, bytes: ByteArray, posicion: Int): Resultado<ServicioFoto> {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
            val tipo = TipoImagen.deBytes(bytes)
                ?: return Resultado.Error(TipoError.VALIDACION, "Tipo de imagen no admitido")
            val ruta = "$uid/$servicioId/${System.currentTimeMillis()}.${tipo.extension}"
            cubeta.upload(ruta, bytes) { contentType = tipo.contentType }
            val url = cubeta.publicUrl(ruta)

            val registrada = registrarFoto(servicioId, url, posicion)
            if (registrada is Resultado.Error) borrarSinFallar(listOf(ruta))
            return registrada
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return traducirErrorStorage(e)
        }
    }

    /** La fila primero (H-06); el delete con select() devuelve la URL del archivo. */
    override suspend fun eliminarFoto(fotoId: String): Resultado<Unit> = conMiId {
        val borradas = supabase.from(TABLA_FOTOS)
            .delete {
                filter { eq("id", fotoId) }
                select()
            }
            .decodeList<ServicioFotoDto>()
        if (borradas.isEmpty()) return@conMiId Resultado.Error(TipoError.VALIDACION, "Foto no encontrada")
        borrarArchivosSinFallar(borradas.map { it.url })
        Resultado.Exito(Unit)
    }

    private suspend fun registrarFoto(servicioId: String, url: String, posicion: Int): Resultado<ServicioFoto> =
        try {
            val foto = supabase.from(TABLA_FOTOS)
                .insert(FotoEscrituraDto(servicioId = servicioId, url = url, posicion = posicion)) { select() }
                .decodeSingle<ServicioFotoDto>()
            Resultado.Exito(foto.aServicioFoto())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    /** Cero filas: el servicio ya no esta, o no es propio. RLS filtra, no rechaza. */
    private fun unaFila(filas: List<ServicioDto>): Resultado<Servicio> =
        filas.firstOrNull()
            ?.let { Resultado.Exito(it.aServicio()) }
            ?: Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado")

    private suspend fun borrarArchivosSinFallar(urls: List<String>) {
        borrarSinFallar(urls.mapNotNull(::rutaEnCubeta))
    }

    private suspend fun borrarSinFallar(rutas: List<String>) {
        if (rutas.isEmpty()) return
        try {
            cubeta.delete(rutas)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La fila ya no esta: el archivo se queda sin que nadie lo vea (H-06).
        }
    }

    /** Lo que sigue a /object/public/servicios/ en la URL publica: <uid>/<servicioId>/<archivo>. */
    private fun rutaEnCubeta(url: String): String? =
        url.substringAfter(PREFIJO_RUTA, missingDelimiterValue = "").ifEmpty { null }

    /** El uid sale de la sesion que ya tiene supabase-kt, sin ir a la red. */
    private suspend fun <T> conMiId(operacion: suspend (String) -> Resultado<T>): Resultado<T> =
        try {
            val uid = supabase.auth.currentUserOrNull()?.id
                ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion abierta")
            operacion(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private companion object {
        const val TABLA_SERVICIOS = "servicios"
        const val TABLA_FOTOS = "servicio_fotos"

        /** basedatos/03_almacenamiento.sql. Las politicas exigen que la ruta empiece con el uid. */
        const val CUBETA_SERVICIOS = "servicios"
        const val PREFIJO_RUTA = "/object/public/$CUBETA_SERVICIOS/"

        val CON_FOTOS = Columns.raw("*, $TABLA_FOTOS(*)")
    }
}
