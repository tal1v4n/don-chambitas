package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.validacion.FalloTrabajador
import mx.donchambitas.app.dominio.validacion.FalloValidacion

/** Recurso de strings.xml de cada fallo, con las claves de la seccion 7 de DISENO-AUTENTICACION.md. */
@StringRes
fun FalloValidacion.mensaje(): Int = when (this) {
    FalloValidacion.CORREO_VACIO -> R.string.validacion_correo_vacio
    FalloValidacion.CORREO_FORMATO -> R.string.validacion_correo_formato
    FalloValidacion.CORREO_LARGO -> R.string.validacion_correo_largo
    FalloValidacion.CONTRASENA_VACIA -> R.string.validacion_contrasena_vacia
    FalloValidacion.CONTRASENA_CORTA -> R.string.validacion_contrasena_corta
    FalloValidacion.ROL_SIN_ELEGIR -> R.string.validacion_rol_sin_elegir
    FalloValidacion.NOMBRE_VACIO -> R.string.validacion_nombre_vacio
    FalloValidacion.NOMBRE_LARGO -> R.string.validacion_nombre_largo
    FalloValidacion.APELLIDOS_VACIO -> R.string.validacion_apellidos_vacio
    FalloValidacion.APELLIDOS_LARGO -> R.string.validacion_apellidos_largo
    FalloValidacion.TELEFONO_VACIO -> R.string.validacion_telefono_vacio
    FalloValidacion.TELEFONO_DIGITOS -> R.string.validacion_telefono_digitos
}

/** Recurso de cada fallo de P-11 y P-13, con las claves de la seccion 7 de DISENO-PERFIL-SERVICIOS.md. */
@StringRes
fun FalloTrabajador.mensaje(): Int = when (this) {
    FalloTrabajador.PERFIL_TITULO_VACIO -> R.string.validacion_perfil_titulo_vacio
    FalloTrabajador.PERFIL_TITULO_LARGO -> R.string.validacion_perfil_titulo_largo
    FalloTrabajador.EXPERIENCIA_RANGO -> R.string.validacion_experiencia_rango
    FalloTrabajador.HABILIDAD_VACIA -> R.string.validacion_habilidad_vacia
    FalloTrabajador.HABILIDAD_LARGA -> R.string.validacion_habilidad_larga
    FalloTrabajador.HABILIDAD_REPETIDA -> R.string.validacion_habilidad_repetida
    FalloTrabajador.MUNICIPIO_SIN_ELEGIR -> R.string.validacion_municipio_sin_elegir
    FalloTrabajador.TELEFONO_DIGITOS -> R.string.validacion_telefono_digitos
    FalloTrabajador.SERVICIO_TITULO_VACIO -> R.string.validacion_servicio_titulo_vacio
    FalloTrabajador.SERVICIO_TITULO_LARGO -> R.string.validacion_servicio_titulo_largo
    FalloTrabajador.CATEGORIA_SIN_ELEGIR -> R.string.validacion_categoria_sin_elegir
    FalloTrabajador.SERVICIO_DESCRIPCION_VACIA -> R.string.validacion_servicio_descripcion_vacia
    FalloTrabajador.PRECIO_FORMATO -> R.string.validacion_precio_formato
    FalloTrabajador.PRECIO_GRANDE -> R.string.validacion_precio_grande
    FalloTrabajador.PRECIO_RANGO -> R.string.validacion_precio_rango
    FalloTrabajador.UNIDAD_LARGA -> R.string.validacion_unidad_larga
}

/**
 * Avisa cuando el foco sale del campo, no cuando entra ni al componerse por
 * primera vez: es la regla 1 de 1.5, validar al perder el foco.
 */
internal fun Modifier.alPerderFoco(accion: () -> Unit): Modifier = composed {
    var teniaFoco by remember { mutableStateOf(false) }
    onFocusChanged { foco ->
        if (teniaFoco && !foco.hasFocus) accion()
        teniaFoco = foco.hasFocus
    }
}
