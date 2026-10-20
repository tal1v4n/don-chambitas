package mx.donchambitas.app.dominio.repositorio

import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.util.Resultado

/**
 * Contrato de operaciones para catalogo de servicios de un trabajador.
 * Especificado en docs/tecnico/CONTRATOS-API.md.
 */
interface RepositorioServicios {
    /**
     * Obtiene los servicios publicados por el trabajador autenticado, activos y
     * pausados, cada uno con sus fotos ordenadas por posicion (H-18).
     */
    suspend fun obtenerMisServicios(): Resultado<List<Servicio>>

    /**
     * Publica un nuevo servicio para el perfil del trabajador autenticado.
     */
    suspend fun crear(servicio: Servicio): Resultado<Servicio>

    /**
     * Modifica los datos de un servicio existente.
     */
    suspend fun actualizar(servicio: Servicio): Resultado<Servicio>

    /**
     * Elimina un servicio existente.
     */
    suspend fun eliminar(id: String): Resultado<Unit>

    /**
     * Pausa o reactiva la publicacion de un servicio. Devuelve el servicio con sus fotos.
     */
    suspend fun pausar(id: String, activo: Boolean): Resultado<Servicio>

    /**
     * Sube una foto asociada a un servicio (maximo 3 por servicio). No informa
     * avance: la pantalla pinta un indicador indeterminado (H-20).
     */
    suspend fun subirFoto(servicioId: String, bytes: ByteArray, posicion: Int): Resultado<ServicioFoto>

    /**
     * Elimina una foto asociada a un servicio.
     */
    suspend fun eliminarFoto(fotoId: String): Resultado<Unit>
}
