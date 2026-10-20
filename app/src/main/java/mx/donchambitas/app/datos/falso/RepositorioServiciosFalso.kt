package mx.donchambitas.app.datos.falso

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.dominio.repositorio.RepositorioServicios
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

@Singleton
class RepositorioServiciosFalso @Inject constructor(
    private val fuente: FuenteDatosFalsa
) : RepositorioServicios {

    var retrasoMs: Long = 300L
    var errorForzado: TipoError? = null

    private suspend fun verificarSimulacion(): Resultado.Error? {
        if (retrasoMs > 0) delay(retrasoMs)
        return errorForzado?.let { Resultado.Error(it, "Error simulado en RepositorioServicios: $it") }
    }

    /** Las fotos viven en fuente.fotosServicio, no dentro del servicio guardado: se pegan al leer (H-18). */
    private fun conFotos(servicio: Servicio): Servicio = servicio.copy(
        fotos = fuente.fotosServicio.filter { it.servicioId == servicio.id }.sortedBy { it.posicion }
    )

    override suspend fun obtenerMisServicios(): Resultado<List<Servicio>> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val lista = fuente.servicios.filter { it.perfilId == usuarioId }.map(::conFotos)
        return Resultado.Exito(lista)
    }

    override suspend fun crear(servicio: Servicio): Resultado<Servicio> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val ahora = Instant.now()
        val nuevoServicio = servicio.copy(
            id = "serv-${UUID.randomUUID().toString().take(8)}",
            perfilId = usuarioId,
            creadoEn = ahora,
            actualizadoEn = ahora,
            fotos = emptyList()
        )
        fuente.servicios.add(nuevoServicio)
        return Resultado.Exito(nuevoServicio)
    }

    override suspend fun actualizar(servicio: Servicio): Resultado<Servicio> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.servicios.indexOfFirst { it.id == servicio.id && it.perfilId == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado o sin permisos")
        }

        val actualizado = servicio.copy(actualizadoEn = Instant.now(), fotos = emptyList())
        fuente.servicios[index] = actualizado
        return Resultado.Exito(conFotos(actualizado))
    }

    override suspend fun eliminar(id: String): Resultado<Unit> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val eliminado = fuente.servicios.removeAll { it.id == id && it.perfilId == usuarioId }
        if (!eliminado) {
            return Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado o sin permisos")
        }
        fuente.fotosServicio.removeAll { it.servicioId == id }
        return Resultado.Exito(Unit)
    }

    override suspend fun pausar(id: String, activo: Boolean): Resultado<Servicio> {
        verificarSimulacion()?.let { return it }

        val usuarioId = fuente.obtenerUsuarioActivoId()
            ?: return Resultado.Error(TipoError.AUTENTICACION, "No hay sesion activa")

        val index = fuente.servicios.indexOfFirst { it.id == id && it.perfilId == usuarioId }
        if (index == -1) {
            return Resultado.Error(TipoError.VALIDACION, "Servicio no encontrado o sin permisos")
        }

        val actualizado = fuente.servicios[index].copy(activo = activo, actualizadoEn = Instant.now())
        fuente.servicios[index] = actualizado
        return Resultado.Exito(conFotos(actualizado))
    }

    override suspend fun subirFoto(
        servicioId: String,
        bytes: ByteArray,
        posicion: Int
    ): Resultado<ServicioFoto> {
        verificarSimulacion()?.let { return it }

        val fotosActuales = fuente.fotosServicio.filter { it.servicioId == servicioId }
        if (fotosActuales.size >= 3) {
            return Resultado.Error(TipoError.VALIDACION, "Maximo 3 fotos por servicio permitidas")
        }

        val nuevaFoto = ServicioFoto(
            id = "foto-${UUID.randomUUID().toString().take(8)}",
            servicioId = servicioId,
            url = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=400&mock=$servicioId-$posicion",
            posicion = posicion,
            creadoEn = Instant.now()
        )
        fuente.fotosServicio.add(nuevaFoto)
        return Resultado.Exito(nuevaFoto)
    }

    override suspend fun eliminarFoto(fotoId: String): Resultado<Unit> {
        verificarSimulacion()?.let { return it }

        val eliminado = fuente.fotosServicio.removeAll { it.id == fotoId }
        return if (eliminado) {
            Resultado.Exito(Unit)
        } else {
            Resultado.Error(TipoError.VALIDACION, "Foto no encontrada")
        }
    }
}
