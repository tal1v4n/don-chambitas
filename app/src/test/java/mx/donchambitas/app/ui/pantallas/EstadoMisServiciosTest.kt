package mx.donchambitas.app.ui.pantallas

import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.dominio.modelo.Servicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EstadoMisServiciosTest {

    private fun servicio(id: String, activo: Boolean, editadoEn: Long) = Servicio(
        id = id,
        perfilId = "usr-trab-1",
        categoriaId = 1,
        titulo = id,
        descripcion = "",
        activo = activo,
        creadoEn = Instant.EPOCH,
        actualizadoEn = Instant.ofEpochSecond(editadoEn)
    )

    @Test
    fun ordenarMisServicios_poneLosActivosPrimeroYElMasRecienteArriba() {
        val ordenados = ordenarMisServicios(
            listOf(
                servicio("pausado-viejo", activo = false, editadoEn = 10),
                servicio("activo-viejo", activo = true, editadoEn = 20),
                servicio("pausado-nuevo", activo = false, editadoEn = 40),
                servicio("activo-nuevo", activo = true, editadoEn = 30)
            )
        )

        assertEquals(
            listOf("activo-nuevo", "activo-viejo", "pausado-nuevo", "pausado-viejo"),
            ordenados.map { it.id }
        )
    }

    @Test
    fun precioDeServicio_eligeLaFraseDeLaTabla34() {
        assertEquals(PrecioServicio.Rango("300", "800"), precioDeServicio(BigDecimal(300), BigDecimal(800)))
        assertEquals(PrecioServicio.Desde("300"), precioDeServicio(BigDecimal(300), null))
        assertEquals(PrecioServicio.Hasta("800"), precioDeServicio(null, BigDecimal(800)))
        assertNull(precioDeServicio(null, null))
    }

    @Test
    fun formatearCantidad_sinDecimalesSiEsEnteraYConMilesDeMexico() {
        assertEquals("300", formatearCantidad(BigDecimal("300.00")))
        assertEquals("1,200", formatearCantidad(BigDecimal("1200")))
        assertEquals("350.50", formatearCantidad(BigDecimal("350.5")))
        assertEquals("99,999,999.99", formatearCantidad(BigDecimal("99999999.99")))
    }

    @Test
    fun pausados_cuentaLosInactivos() {
        val estado = EstadoMisServicios(
            servicios = listOf(
                servicio("a", activo = true, editadoEn = 1),
                servicio("b", activo = false, editadoEn = 2)
            )
        )
        assertEquals(1, estado.pausados)
    }
}
