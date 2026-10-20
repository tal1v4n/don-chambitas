package mx.donchambitas.app.dominio.validacion

import java.math.BigDecimal
import mx.donchambitas.app.dominio.validacion.ValidacionesTrabajador as V
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tablas 5.2 y 5.3 de DISENO-PERFIL-SERVICIOS.md, con sus bordes, y la normalizacion de 1.6. */
class ValidacionesTrabajadorTest {

    @Test
    fun tituloPerfil_obligatorioYHasta100() {
        assertEquals(FalloTrabajador.PERFIL_TITULO_VACIO, V.validarTituloPerfil("   "))
        assertNull(V.validarTituloPerfil("a".repeat(100)))
        assertNull(V.validarTituloPerfil("  ${"a".repeat(100)}  "))
        assertEquals(FalloTrabajador.PERFIL_TITULO_LARGO, V.validarTituloPerfil("a".repeat(101)))
    }

    @Test
    fun experiencia_vaciaOEntre0y70() {
        assertNull(V.validarExperiencia(""))
        assertNull(V.validarExperiencia("0"))
        assertNull(V.validarExperiencia("70"))
        assertEquals(FalloTrabajador.EXPERIENCIA_RANGO, V.validarExperiencia("71"))
        assertEquals(FalloTrabajador.EXPERIENCIA_RANGO, V.validarExperiencia("-1"))
        assertEquals(FalloTrabajador.EXPERIENCIA_RANGO, V.validarExperiencia("diez"))
    }

    @Test
    fun habilidadNueva_noVaciaHasta60YSinRepetirMayusculas() {
        val existentes = listOf("Cobre", "Boilers")
        assertEquals(FalloTrabajador.HABILIDAD_VACIA, V.validarHabilidadNueva("  ", existentes))
        assertNull(V.validarHabilidadNueva("a".repeat(60), existentes))
        assertEquals(FalloTrabajador.HABILIDAD_LARGA, V.validarHabilidadNueva("a".repeat(61), existentes))
        assertEquals(FalloTrabajador.HABILIDAD_REPETIDA, V.validarHabilidadNueva(" cobre ", existentes))
        assertNull(V.validarHabilidadNueva("Tinacos", existentes))
    }

    @Test
    fun municipio_obligatorioSoloConEstado() {
        assertNull(V.validarMunicipio(null, null))
        assertNull(V.validarMunicipio(21, 5))
        assertEquals(FalloTrabajador.MUNICIPIO_SIN_ELEGIR, V.validarMunicipio(21, null))
    }

    @Test
    fun telefonoContacto_vacioODiezDigitos() {
        assertNull(V.validarTelefonoContacto(""))
        assertNull(V.validarTelefonoContacto("2225550101"))
        assertEquals(FalloTrabajador.TELEFONO_DIGITOS, V.validarTelefonoContacto("33123"))
        assertEquals(FalloTrabajador.TELEFONO_DIGITOS, V.validarTelefonoContacto("22255501011"))
        assertEquals(FalloTrabajador.TELEFONO_DIGITOS, V.validarTelefonoContacto("222-555-01"))
    }

    @Test
    fun tituloServicio_obligatorioYHasta120() {
        assertEquals(FalloTrabajador.SERVICIO_TITULO_VACIO, V.validarTituloServicio(""))
        assertNull(V.validarTituloServicio("a".repeat(120)))
        assertEquals(FalloTrabajador.SERVICIO_TITULO_LARGO, V.validarTituloServicio("a".repeat(121)))
    }

    @Test
    fun categoriaYDescripcion_obligatorias() {
        assertEquals(FalloTrabajador.CATEGORIA_SIN_ELEGIR, V.validarCategoria(null))
        assertNull(V.validarCategoria(1))
        assertEquals(FalloTrabajador.SERVICIO_DESCRIPCION_VACIA, V.validarDescripcionServicio(" \n "))
        assertNull(V.validarDescripcionServicio("Reparo fugas"))
    }

    @Test
    fun precio_vacioONumeroConHastaDosDecimales() {
        assertNull(V.validarPrecio(""))
        assertNull(V.validarPrecio("0"))
        assertNull(V.validarPrecio("350,5"))
        assertNull(V.validarPrecio("350.50"))
        assertNull(V.validarPrecio("99999999.99"))
        assertEquals(FalloTrabajador.PRECIO_GRANDE, V.validarPrecio("100000000"))
        assertEquals(FalloTrabajador.PRECIO_FORMATO, V.validarPrecio("1.234"))
        assertEquals(FalloTrabajador.PRECIO_FORMATO, V.validarPrecio("-5"))
        assertEquals(FalloTrabajador.PRECIO_FORMATO, V.validarPrecio("."))
    }

    @Test
    fun rangoPrecio_hastaNoMenorQueDesde() {
        assertNull(V.validarRangoPrecio("300", "800"))
        assertNull(V.validarRangoPrecio("300", "300"))
        assertNull(V.validarRangoPrecio("", "200"))
        assertNull(V.validarRangoPrecio("300", ""))
        assertEquals(FalloTrabajador.PRECIO_RANGO, V.validarRangoPrecio("800", "300"))
        assertEquals(FalloTrabajador.PRECIO_RANGO, V.validarRangoPrecio("350,5", "350"))
    }

    @Test
    fun unidad_hasta30() {
        assertNull(V.validarUnidad(""))
        assertNull(V.validarUnidad("a".repeat(30)))
        assertEquals(FalloTrabajador.UNIDAD_LARGA, V.validarUnidad("a".repeat(31)))
    }

    @Test
    fun normalizarPrecio_conPuntoYDosDecimales() {
        assertEquals(BigDecimal("350.50"), V.normalizarPrecio("350,5"))
        assertEquals(BigDecimal("800.00"), V.normalizarPrecio(" 800 "))
        assertNull(V.normalizarPrecio(""))
    }

    @Test
    fun normalizarHabilidades_recortaYQuitaRepetidasConservandoLaPrimera() {
        assertEquals(
            listOf("Cobre", "Boilers"),
            V.normalizarHabilidades(listOf(" Cobre ", "Boilers", "COBRE", "  "))
        )
    }
}
