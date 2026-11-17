package mx.donchambitas.app.ui.tema

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TemaTest {

    @Test
    fun debeTenerTintaComoOnPrimary_cuandoSeConsultaEsquemaDeColor() {
        assertEquals(Tinta, EsquemaColoresCasco.onPrimary)
        assertNotEquals(Blanco, EsquemaColoresCasco.onPrimary)
    }

    @Test
    fun debeMapearColoresCascoCorrectamente_cuandoSeConsultaEsquemaDeColor() {
        assertEquals(Amarillo, EsquemaColoresCasco.primary)
        assertEquals(Naranja, EsquemaColoresCasco.secondary)
        assertEquals(Blanco, EsquemaColoresCasco.onSecondary)
        assertEquals(Marfil, EsquemaColoresCasco.background)
        assertEquals(Tinta, EsquemaColoresCasco.onBackground)
        assertEquals(Hueso, EsquemaColoresCasco.surface)
        assertEquals(Tinta, EsquemaColoresCasco.onSurface)
        assertEquals(Borde, EsquemaColoresCasco.outline)
        assertEquals(Error, EsquemaColoresCasco.error)
        assertEquals(Blanco, EsquemaColoresCasco.onError)
    }

    @Test
    fun debeTenerSeisEstilosTipograficosConTamanosYPesosCorrectos_cuandoSeConsultaTipografia() {
        // titulo: 24 sp, SemiBold
        assertEquals(24.sp, Titulo.fontSize)
        assertEquals(FontWeight.SemiBold, Titulo.fontWeight)
        assertEquals(Titulo, Tipografia.titulo)

        // subtitulo: 20 sp, Medium
        assertEquals(20.sp, Subtitulo.fontSize)
        assertEquals(FontWeight.Medium, Subtitulo.fontWeight)
        assertEquals(Subtitulo, Tipografia.subtitulo)

        // cuerpoFuerte: 16 sp, Medium
        assertEquals(16.sp, CuerpoFuerte.fontSize)
        assertEquals(FontWeight.Medium, CuerpoFuerte.fontWeight)
        assertEquals(CuerpoFuerte, Tipografia.cuerpoFuerte)

        // cuerpo: 16 sp, Normal
        assertEquals(16.sp, Cuerpo.fontSize)
        assertEquals(FontWeight.Normal, Cuerpo.fontWeight)
        assertEquals(Cuerpo, Tipografia.cuerpo)

        // secundario: 14 sp, Normal
        assertEquals(14.sp, Secundario.fontSize)
        assertEquals(FontWeight.Normal, Secundario.fontWeight)
        assertEquals(Secundario, Tipografia.secundario)

        // pie: 12 sp, Normal
        assertEquals(12.sp, Pie.fontSize)
        assertEquals(FontWeight.Normal, Pie.fontWeight)
        assertEquals(Pie, Tipografia.pie)
    }

    @Test
    fun debeExponerEscalaBase4_cuandoSeConsultaEspaciado() {
        assertEquals(4.dp, Espaciado.dp4)
        assertEquals(8.dp, Espaciado.dp8)
        assertEquals(12.dp, Espaciado.dp12)
        assertEquals(16.dp, Espaciado.dp16)
        assertEquals(24.dp, Espaciado.dp24)
        assertEquals(32.dp, Espaciado.dp32)
        assertEquals(48.dp, Espaciado.dp48)

        assertEquals(16.dp, Espaciado.margenPantalla)
        assertEquals(12.dp, Espaciado.separacionTarjetas)
        assertEquals(16.dp, Espaciado.rellenoTarjeta)
    }
}
