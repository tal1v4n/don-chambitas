package mx.donchambitas.app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import java.io.File
import mx.donchambitas.app.datos.falso.FuenteDatosFalsa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IconosCategoriaTest {

    private val otros = Icons.Outlined.MoreHoriz

    /** Renglones (nombre, descripcion, icono, orden) del INSERT de categorias en la semilla. */
    private fun categoriasDeLaSemilla(): List<List<String>> {
        // Las pruebas unitarias corren con app/ como directorio de trabajo.
        val semilla = File("../basedatos/04_datos_semilla.sql").readText()
        val bloque = semilla
            .substringAfter("INSERT INTO categorias")
            .substringBefore("on conflict")
        val renglon = Regex("""\('([^']+)',\s*'([^']+)',\s*'([^']+)',\s*(\d+)\)""")
        return renglon.findAll(bloque).map { it.groupValues.drop(1) }.toList()
    }

    @Test
    fun cadaIconoDeLaSemilla_debeTenerSuPropioIcono() {
        val iconos = categoriasDeLaSemilla().map { it[2] }
        assertEquals(16, iconos.size)

        iconos.filter { it != "more_horiz" }.forEach { nombre ->
            assertNotEquals("$nombre cae en Otros", otros, iconoCategoria(nombre))
        }
        assertEquals(otros, iconoCategoria("more_horiz"))

        // Ningun icono se repite entre categorias
        assertEquals(16, iconos.map { iconoCategoria(it) }.toSet().size)
    }

    @Test
    fun nombreNuloVacioODesconocido_debeCaerEnOtros() {
        assertEquals(otros, iconoCategoria(null))
        assertEquals(otros, iconoCategoria(""))
        assertEquals(otros, iconoCategoria("cohete"))
    }

    @Test
    fun categoriasFalsas_debenCoincidirConLaSemilla() {
        val falsas = FuenteDatosFalsa().categorias
            .map { listOf(it.nombre, it.descripcion.orEmpty(), it.icono.orEmpty(), it.orden.toString()) }

        // Si coinciden con la semilla, sus iconos ya los cubre la primera prueba.
        assertEquals(categoriasDeLaSemilla(), falsas)
    }

    @Test
    fun nombresDeLaSemilla_debenLlevarAcentos() {
        val nombres = categoriasDeLaSemilla().map { it[0] }
        listOf("Plomería", "Albañilería", "Cómputo", "Aire y refrigeración").forEach {
            assertTrue("Falta $it", it in nombres)
        }
    }
}
