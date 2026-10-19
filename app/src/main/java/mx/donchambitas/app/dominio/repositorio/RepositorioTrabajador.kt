package mx.donchambitas.app.dominio.repositorio

import mx.donchambitas.app.dominio.modelo.PerfilHabilidad
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.util.Resultado

/**
 * Contrato de operaciones del perfil laboral del trabajador.
 * Especificado en docs/tecnico/CONTRATOS-API.md.
 */
interface RepositorioTrabajador {
    /**
     * Obtiene el perfil publico completo con habilidades, servicios y resenas para P-07.
     */
    suspend fun obtenerPerfilPublico(id: String): Resultado<PerfilPublicoTrabajador>

    /**
     * Obtiene el perfil de trabajador del usuario autenticado si existe.
     */
    suspend fun obtenerMiPerfil(): Resultado<PerfilTrabajador?>

    /**
     * Obtiene las habilidades del trabajador autenticado, en orden alfabetico.
     * Lista vacia si todavia no tiene perfil o no declaro ninguna (H-19).
     */
    suspend fun obtenerMisHabilidades(): Resultado<List<String>>

    /**
     * Guarda o actualiza el perfil del trabajador autenticado.
     */
    suspend fun guardarMiPerfil(perfil: PerfilTrabajador): Resultado<PerfilTrabajador>

    /**
     * Reemplaza por completo el catalogo de habilidades del trabajador.
     */
    suspend fun reemplazarHabilidades(lista: List<String>): Resultado<List<PerfilHabilidad>>
}
