package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.util.TipoError

/**
 * Estado inmutable de la pantalla de recuperar contrasena (P-04).
 * Especificado en docs/producto/DISENO-AUTENTICACION.md, seccion 4.3.
 *
 * No hay `destino`: P-04 no navega sola. Se sale por la flecha o por "Volver
 * a iniciar sesion", y las dos regresan a P-02 con popBackStack.
 *
 * @property enviado Cambia la pantalla a la vista de confirmacion (4.2). No es
 *   otra pantalla, es la misma con otro contenido.
 */
data class EstadoRecuperarContrasena(
    val correo: String = "",
    @StringRes val errorCorreo: Int? = null,
    val errorPantalla: TipoError? = null,
    val cargando: Boolean = false,
    val enviado: Boolean = false
)
