package mx.donchambitas.app.ui.pantallas

import java.time.Instant
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadoServicioTest {

    @Test
    fun filtrarPrecio_dejaSoloDigitosYUnSeparadorConDosDecimales() {
        assertEquals("300", filtrarPrecio("300"))
        assertEquals("350.50", filtrarPrecio("350.505"))
        assertEquals("350,5", filtrarPrecio("350,5"))
        assertEquals("1200", filtrarPrecio("$1 2a00"))
        assertEquals("12.34", filtrarPrecio("12.3.4"))
        assertEquals(".5", filtrarPrecio(".5"))
        assertEquals("", filtrarPrecio("abc"))
    }

    @Test
    fun creando_dependeDeQueNoHayaServicioId() {
        assertTrue(EstadoServicio().creando)
        assertFalse(EstadoServicio(servicioId = "serv-1").creando)
    }

    @Test
    fun hayPrecio_conCualquieraDeLosDos() {
        assertFalse(EstadoServicio().hayPrecio)
        assertTrue(EstadoServicio(precioDesde = "300").hayPrecio)
        assertTrue(EstadoServicio(precioHasta = "800").hayPrecio)
        assertFalse(EstadoServicio(precioDesde = "  ").hayPrecio)
    }

    @Test
    fun subiendoFoto_soloConUnaMiniaturaSubiendo() {
        val guardada = FotoEnPantalla.Guardada(ServicioFoto("f1", "serv-1", "url", 1, Instant.EPOCH))
        assertFalse(EstadoServicio(fotos = listOf(guardada)).subiendoFoto)
        assertTrue(EstadoServicio(fotos = listOf(guardada, FotoEnPantalla.Subiendo(2))).subiendoFoto)
        assertEquals(1, guardada.posicion)
    }
}
