package mx.donchambitas.app.dominio.validacion

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Motivo por el que un campo de P-11 o P-13 no pasa. Como [FalloValidacion],
 * es Kotlin puro: la capa ui lo traduce a su recurso validacion_*.
 */
enum class FalloTrabajador {
    PERFIL_TITULO_VACIO,
    PERFIL_TITULO_LARGO,
    EXPERIENCIA_RANGO,
    HABILIDAD_VACIA,
    HABILIDAD_LARGA,
    HABILIDAD_REPETIDA,
    MUNICIPIO_SIN_ELEGIR,
    TELEFONO_DIGITOS,
    SERVICIO_TITULO_VACIO,
    SERVICIO_TITULO_LARGO,
    CATEGORIA_SIN_ELEGIR,
    SERVICIO_DESCRIPCION_VACIA,
    PRECIO_FORMATO,
    PRECIO_GRANDE,
    PRECIO_RANGO,
    UNIDAD_LARGA
}

/**
 * Topes de perfiles_trabajador, perfil_habilidades y servicios en
 * basedatos/01_esquema.sql (DISENO-PERFIL-SERVICIOS.md 5.1). Si alla cambian,
 * aqui cambian el mismo dia, o la base rechaza lo que la aplicacion dio por bueno.
 */
object LimitesTrabajador {
    const val LARGO_MAXIMO_TITULO_PERFIL = 100
    const val EXPERIENCIA_MAXIMA = 70
    const val LARGO_MAXIMO_HABILIDAD = 60
    const val LARGO_TELEFONO = 10
    const val LARGO_MAXIMO_TITULO_SERVICIO = 120
    const val LARGO_MAXIMO_UNIDAD = 30
    const val DECIMALES_PRECIO = 2

    /** numeric(10,2): ocho enteros y dos decimales. */
    val PRECIO_MAXIMO: BigDecimal = BigDecimal("99999999.99")
}

/**
 * Reglas de los formularios de P-11 y P-13.
 * Especificadas en docs/producto/DISENO-PERFIL-SERVICIOS.md, secciones 5.2 y 5.3.
 *
 * Cada funcion devuelve el fallo del campo, o nulo si esta bien. La
 * normalizacion de 1.6 tambien vive aqui, para que lo que se valida y lo que
 * se envia salgan de la misma lectura.
 */
object ValidacionesTrabajador {

    fun validarTituloPerfil(titulo: String): FalloTrabajador? {
        val limpio = titulo.trim()
        return when {
            limpio.isEmpty() -> FalloTrabajador.PERFIL_TITULO_VACIO
            limpio.length > LimitesTrabajador.LARGO_MAXIMO_TITULO_PERFIL -> FalloTrabajador.PERFIL_TITULO_LARGO
            else -> null
        }
    }

    /** Vacia se guarda como 0, el valor por omision de la columna (5.2). */
    fun validarExperiencia(anios: String): FalloTrabajador? {
        val limpio = anios.trim()
        if (limpio.isEmpty()) return null
        val numero = limpio.toIntOrNull()
        return if (numero == null || numero !in 0..LimitesTrabajador.EXPERIENCIA_MAXIMA) {
            FalloTrabajador.EXPERIENCIA_RANGO
        } else {
            null
        }
    }

    /** Se valida al agregarla, contra las que ya estan en la lista (5.2). */
    fun validarHabilidadNueva(habilidad: String, existentes: List<String>): FalloTrabajador? {
        val limpia = habilidad.trim()
        return when {
            limpia.isEmpty() -> FalloTrabajador.HABILIDAD_VACIA
            limpia.length > LimitesTrabajador.LARGO_MAXIMO_HABILIDAD -> FalloTrabajador.HABILIDAD_LARGA
            existentes.any { it.trim().equals(limpia, ignoreCase = true) } -> FalloTrabajador.HABILIDAD_REPETIDA
            else -> null
        }
    }

    /** Obligatorio solo si ya hay estado: sin estado, el perfil no dice donde trabaja y es valido. */
    fun validarMunicipio(estadoId: Int?, municipioId: Int?): FalloTrabajador? =
        if (estadoId != null && municipioId == null) FalloTrabajador.MUNICIPIO_SIN_ELEGIR else null

    /** Vacio o exactamente 10 digitos, mas estricto que varchar(20), como en P-03. */
    fun validarTelefonoContacto(telefono: String): FalloTrabajador? {
        val limpio = telefono.trim()
        return when {
            limpio.isEmpty() -> null
            limpio.length != LimitesTrabajador.LARGO_TELEFONO || !limpio.all(Char::isDigit) ->
                FalloTrabajador.TELEFONO_DIGITOS
            else -> null
        }
    }

    fun validarTituloServicio(titulo: String): FalloTrabajador? {
        val limpio = titulo.trim()
        return when {
            limpio.isEmpty() -> FalloTrabajador.SERVICIO_TITULO_VACIO
            limpio.length > LimitesTrabajador.LARGO_MAXIMO_TITULO_SERVICIO -> FalloTrabajador.SERVICIO_TITULO_LARGO
            else -> null
        }
    }

    fun validarCategoria(categoriaId: Int?): FalloTrabajador? =
        if (categoriaId == null) FalloTrabajador.CATEGORIA_SIN_ELEGIR else null

    fun validarDescripcionServicio(descripcion: String): FalloTrabajador? =
        if (descripcion.isBlank()) FalloTrabajador.SERVICIO_DESCRIPCION_VACIA else null

    /** Vacio, o un numero mayor o igual a 0 con hasta dos decimales y que quepa en numeric(10,2). */
    fun validarPrecio(precio: String): FalloTrabajador? {
        if (precio.isBlank()) return null
        val cantidad = leerCantidad(precio)
        return when {
            cantidad == null || cantidad.signum() < 0 ||
                cantidad.stripTrailingZeros().scale() > LimitesTrabajador.DECIMALES_PRECIO ->
                FalloTrabajador.PRECIO_FORMATO
            cantidad > LimitesTrabajador.PRECIO_MAXIMO -> FalloTrabajador.PRECIO_GRANDE
            else -> null
        }
    }

    /**
     * Hasta no puede ser menor que desde, si los dos estan llenos y se leen.
     * Si alguno no se lee, ya lo marco [validarPrecio]. El error va en "Hasta" (5.3).
     */
    fun validarRangoPrecio(desde: String, hasta: String): FalloTrabajador? {
        val minimo = desde.takeIf(String::isNotBlank)?.let(::leerCantidad) ?: return null
        val maximo = hasta.takeIf(String::isNotBlank)?.let(::leerCantidad) ?: return null
        return if (maximo < minimo) FalloTrabajador.PRECIO_RANGO else null
    }

    fun validarUnidad(unidad: String): FalloTrabajador? =
        if (unidad.trim().length > LimitesTrabajador.LARGO_MAXIMO_UNIDAD) FalloTrabajador.UNIDAD_LARGA else null

    /** 1.6: coma o punto, enviado con punto y dos decimales. Vacio o ilegible es nulo. */
    fun normalizarPrecio(precio: String): BigDecimal? =
        leerCantidad(precio)?.setScale(LimitesTrabajador.DECIMALES_PRECIO, RoundingMode.HALF_UP)

    /** 1.6: recortadas y sin repetidas; entre dos que solo difieren en mayusculas se queda la primera. */
    fun normalizarHabilidades(habilidades: List<String>): List<String> =
        habilidades.map(String::trim).filter(String::isNotEmpty).distinctBy { it.lowercase() }

    private fun leerCantidad(texto: String): BigDecimal? =
        texto.trim().replace(',', '.').toBigDecimalOrNull()
}
