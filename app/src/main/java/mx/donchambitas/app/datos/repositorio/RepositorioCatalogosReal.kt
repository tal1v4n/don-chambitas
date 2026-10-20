package mx.donchambitas.app.datos.repositorio

import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.remoto.dto.CategoriaDto
import mx.donchambitas.app.datos.remoto.dto.EstadoDto
import mx.donchambitas.app.datos.remoto.dto.MunicipioDto
import mx.donchambitas.app.datos.remoto.supabase.traducirErrorDatos
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.FiltrosBusquedaTrabajadores
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.util.Resultado

/**
 * Los tres catalogos de lectura contra categorias, estados y municipios
 * (S3-T09, "Busqueda y catalogos" de CONTRATOS-API.md).
 *
 * Entraron aqui y no en S4-T09 por decision del lider: los ids de municipio
 * del catalogo falso no coinciden con los de la base, y con el perfil real
 * P-11 guardaria el municipio equivocado sin error. buscarTrabajadores sigue
 * en el falso hasta S4-T09, que es la suya.
 *
 * Los catalogos no cambian mientras la aplicacion esta abierta: se guardan en
 * memoria despues de la primera lectura buena.
 */
@Singleton
class RepositorioCatalogosReal @Inject constructor(
    private val cliente: Lazy<SupabaseClient>,
    private val busquedaFalsa: RepositorioCatalogosFalso
) : RepositorioCatalogos {

    private val supabase get() = cliente.get()

    private var categorias: List<Categoria>? = null
    private var estados: List<Estado>? = null
    private val municipiosPorEstado = mutableMapOf<Int, List<Municipio>>()

    override suspend fun buscarTrabajadores(
        filtros: FiltrosBusquedaTrabajadores,
        pagina: Int
    ): Resultado<List<ResumenTrabajadorBusqueda>> = busquedaFalsa.buscarTrabajadores(filtros, pagina)

    /** Solo las activas, en el orden de categorias.orden. */
    override suspend fun obtenerCategorias(): Resultado<List<Categoria>> =
        categorias?.let { Resultado.Exito(it) } ?: leer {
            supabase.from(TABLA_CATEGORIAS)
                .select {
                    filter { eq("activa", true) }
                    order("orden", Order.ASCENDING)
                }
                .decodeList<CategoriaDto>()
                .map { it.aCategoria() }
                .also { categorias = it }
        }

    override suspend fun obtenerEstados(): Resultado<List<Estado>> =
        estados?.let { Resultado.Exito(it) } ?: leer {
            supabase.from(TABLA_ESTADOS)
                .select { order("nombre", Order.ASCENDING) }
                .decodeList<EstadoDto>()
                .map { it.aEstado() }
                .also { estados = it }
        }

    override suspend fun obtenerMunicipios(estadoId: Int): Resultado<List<Municipio>> =
        municipiosPorEstado[estadoId]?.let { Resultado.Exito(it) } ?: leer {
            supabase.from(TABLA_MUNICIPIOS)
                .select {
                    filter { eq("estado_id", estadoId) }
                    order("nombre", Order.ASCENDING)
                }
                .decodeList<MunicipioDto>()
                .map { it.aMunicipio() }
                .also { municipiosPorEstado[estadoId] = it }
        }

    private suspend fun <T> leer(consulta: suspend () -> T): Resultado<T> =
        try {
            Resultado.Exito(consulta())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            traducirErrorDatos(e)
        }

    private companion object {
        const val TABLA_CATEGORIAS = "categorias"
        const val TABLA_ESTADOS = "estados"
        const val TABLA_MUNICIPIOS = "municipios"
    }
}
