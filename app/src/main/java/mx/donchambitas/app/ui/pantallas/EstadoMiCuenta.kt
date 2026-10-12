package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.util.TipoError

/** Las tres vistas de P-18. Es una sola ruta que cambia de contenido (DISENO-CUENTA.md, 1). */
enum class VistaMiCuenta { RESUMEN, EDITAR_DATOS, CAMBIAR_CONTRASENA }

/** Campos de la vista de editar datos, en orden visual: el foco salta al primero que falle. */
enum class CampoMiCuenta { NOMBRE, APELLIDOS, TELEFONO }

/** Aviso que el resumen muestra despues de guardar algo. */
enum class AvisoMiCuenta { DATOS_GUARDADOS, CONTRASENA_CAMBIADA }

/**
 * Estado inmutable de la pantalla Mi cuenta (P-18).
 * Especificado en docs/producto/DISENO-CUENTA.md, seccion 5.
 *
 * @property usuario La ficha de public.usuarios. Nula mientras carga o si la
 *   carga fallo. No sale de la Sesion: esa no se entera de las ediciones.
 * @property cargando Carga de la ficha, a pantalla completa en el resumen.
 * @property enviando Envio del formulario de la vista actual, dentro del boton.
 * @property errorFormulario Error del envio que no pertenece a un campo.
 */
data class EstadoMiCuenta(
    val vista: VistaMiCuenta = VistaMiCuenta.RESUMEN,
    val usuario: Usuario? = null,
    val cargando: Boolean = true,
    val errorCarga: TipoError? = null,
    val aviso: AvisoMiCuenta? = null,
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    @StringRes val errorNombre: Int? = null,
    @StringRes val errorApellidos: Int? = null,
    @StringRes val errorTelefono: Int? = null,
    val contrasena: String = "",
    @StringRes val errorContrasena: Int? = null,
    val enviando: Boolean = false,
    val errorFormulario: TipoError? = null,
    val confirmandoCierre: Boolean = false,
    val cerrandoSesion: Boolean = false,
    val errorCierre: TipoError? = null
)
