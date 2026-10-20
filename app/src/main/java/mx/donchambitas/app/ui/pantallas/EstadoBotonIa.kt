package mx.donchambitas.app.ui.pantallas

/**
 * Estado del boton "Redactar con IA" de P-11 y P-13.
 * Especificado en docs/producto/DISENO-PERFIL-SERVICIOS.md, seccion 1.9.
 *
 * En ninguno de los cuatro el campo de descripcion deja de ser editable: la IA
 * propone, el usuario escribe (HU-31).
 */
sealed interface EstadoBotonIa {
    /** Faltan los datos de los que sale la entrada; el boton dice cuales. */
    data object FaltanDatos : EstadoBotonIa

    /** @property restantesHoy Usos que quedan hoy, o nulo si todavia no se conocen. */
    data class Disponible(val restantesHoy: Int? = null) : EstadoBotonIa

    data object Pidiendo : EstadoBotonIa

    /** Se gastaron las 10 llamadas del dia, en total (DEC-18). */
    data object LimiteAlcanzado : EstadoBotonIa
}

/**
 * El estado del boton tras cambiar los datos de los que sale la entrada (1.9).
 * Mientras pide o con el limite alcanzado no cambia: escribir no lo destraba.
 */
fun EstadoBotonIa.recalcular(faltanDatos: Boolean): EstadoBotonIa = when {
    this == EstadoBotonIa.Pidiendo || this == EstadoBotonIa.LimiteAlcanzado -> this
    faltanDatos -> EstadoBotonIa.FaltanDatos
    else -> EstadoBotonIa.Disponible((this as? EstadoBotonIa.Disponible)?.restantesHoy)
}
