# Sistema de diseño

Paleta **Casco**. La identidad nace del casco de seguridad amarillo: un amarillo
claro y limpio como relleno, tinta casi negra para texto y contornos, y
neutros cálidos casi blancos de fondo para que el amarillo no canse la vista.
Sustituye a la paleta Taller (mostaza y terracota) desde el 2026-11-16.

## Colores

```kotlin
val Amarillo      = Color(0xFFFCD34D)  // primario: rellenos, barra superior, indicador
val AmarilloSuave = Color(0xFFFEF3C7)  // contenedores y resaltes suaves
val Naranja       = Color(0xFFC8410F)  // acento: acción destacada, estrellas, precio
val Tinta         = Color(0xFF1F1C17)  // texto principal
val Grafito       = Color(0xFF3A362E)  // contornos, foco, botones de texto
val Gris          = Color(0xFF67625A)  // texto secundario
val Marfil        = Color(0xFFFFFEFA)  // fondo
val Hueso         = Color(0xFFF6F4EE)  // superficie, tarjetas, campos
val HuesoAlto     = Color(0xFFEFECE4)  // superficie elevada (menús, diálogos)
val Borde         = Color(0xFFE8E4DA)  // separadores

val Exito       = Color(0xFF1E7F45)
val Advertencia = Color(0xFFF29A1F)
val Error       = Color(0xFFC8322B)
```

### Reglas de contraste, obligatorias

**Sobre amarillo va texto `Tinta`, nunca blanco.** Blanco sobre Amarillo da
1.44:1 y no se lee. Tinta sobre Amarillo alcanza **11.78:1** (AAA).

**El amarillo es relleno, nunca texto.** Para texto, iconos sueltos o contornos
interactivos se usa `Grafito` o `Tinta`. Un texto amarillo sobre fondo claro es
invisible al sol.

**Sobre naranja va texto blanco** (4.98:1, AA).

**Nunca naranja y amarillo pegados** en dos elementos grandes. El naranja es
acento: un botón, una insignia, un precio; no bloques.

### Tabla de contrastes (WCAG 2.1)

| Combinación (Frente / Fondo) | Códigos Hex | Contraste | Nivel WCAG | Uso |
|---|---|---|---|---|
| **Tinta sobre Amarillo** | `#1F1C17` / `#FCD34D` | **11.78:1** | AAA | Botón principal, barra superior, chips activos. Obligatorio. |
| **Blanco sobre Amarillo** | `#FFFFFF` / `#FCD34D` | **1.44:1** | Falla | **Prohibido** en texto o icono. |
| **Tinta sobre Amarillo Suave** | `#1F1C17` / `#FEF3C7` | **15.25:1** | AAA | Contenedores resaltados. |
| **Gris sobre Amarillo Suave** | `#67625A` / `#FEF3C7` | **5.43:1** | AA | Texto secundario en contenedores resaltados. |
| **Blanco sobre Naranja** | `#FFFFFF` / `#C8410F` | **4.98:1** | AA | Botón destacado (postularse, publicar). |
| **Naranja sobre Marfil** | `#C8410F` / `#FFFEFA` | **4.93:1** | AA | Precio, avisos de acento como texto. |
| **Tinta sobre Marfil** | `#1F1C17` / `#FFFEFA` | **16.83:1** | AAA | Texto de lectura. |
| **Tinta sobre Hueso** | `#1F1C17` / `#F6F4EE` | **15.44:1** | AAA | Texto sobre tarjetas. |
| **Gris sobre Marfil** | `#67625A` / `#FFFEFA` | **5.99:1** | AA | Texto secundario, fechas. |
| **Gris sobre Hueso** | `#67625A` / `#F6F4EE` | **5.50:1** | AA | Texto secundario en tarjetas. |
| **Grafito sobre Marfil** | `#3A362E` / `#FFFEFA` | **11.91:1** | AAA | Contornos, foco, botón de texto. |
| **Grafito sobre Hueso** | `#3A362E` / `#F6F4EE` | **10.93:1** | AAA | Borde del campo enfocado. |
| **Blanco sobre Éxito** | `#FFFFFF` / `#1E7F45` | **5.03:1** | AA | Insignia 'abierta'. |
| **Tinta sobre Advertencia** | `#1F1C17` / `#F29A1F` | **7.61:1** | AAA | Insignia 'asignada'. |
| **Blanco sobre Advertencia** | `#FFFFFF` / `#F29A1F` | **2.23:1** | Falla | Prohibido; usar Tinta. |
| **Blanco sobre Error** | `#FFFFFF` / `#C8322B` | **5.32:1** | AA | Insignia 'cancelada' y errores. |

### A qué se aplica cada uno

| Color | Dónde |
|---|---|
| Amarillo | Botón principal, barra superior, chip activo, indicador de la barra inferior |
| Amarillo suave | Contenedores resaltados, fondos de aviso suave |
| Naranja | Botón destacado (postularse, publicar), estrellas, precio |
| Tinta | Títulos y texto de lectura |
| Grafito | Contorno del botón secundario, borde del campo enfocado, botón de texto |
| Gris | Etiquetas, ayudas, fechas, texto deshabilitado |
| Marfil | Fondo de pantalla |
| Hueso | Tarjetas, campos, hojas inferiores |
| Borde | Separadores y contorno de campos en reposo |

Las barras del sistema llevan iconos oscuros (`windowLightStatusBar`): sobre el
amarillo, los iconos blancos no se leen.

**Sin modo oscuro en el MVP.** Es DEC-15.
Muestra gráfica de la paleta: ver `docs/tecnico/recursos/muestra-paleta.png`.
Identidad y logotipo: ver `docs/tecnico/recursos/logo.svg`.

## Tipografía

Fuente del sistema, sin descargar nada.

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| `portada` | 32 sp | Bold | Primera frase de una pantalla con portada (P-02, P-03, P-04) |
| `titulo` | 24 sp | SemiBold | Encabezado de pantalla |
| `subtitulo` | 20 sp | Medium | Sección |
| `cuerpoFuerte` | 16 sp | Medium | Título de tarjeta |
| `cuerpo` | 16 sp | Normal | Texto de lectura |
| `secundario` | 14 sp | Normal | Etiquetas y apoyos |
| `pie` | 12 sp | Normal | Fechas, contadores |

Nada por debajo de 12 sp. Un usuario en obra ve la pantalla con el sol encima.

## Espaciado

Escala de 4: `4, 8, 12, 16, 24, 32, 48`.

Margen lateral de pantalla: **16 dp**. Separación entre tarjetas: **12 dp**.
Relleno interno de tarjeta: **16 dp**.

## Formas

| Elemento | Radio |
|---|---|
| Botones | 16 dp |
| Campos | 14 dp |
| Tarjetas | 20 dp |
| Chips y píldoras | completamente redondeado |
| Hoja inferior | 28 dp arriba |
| Foto de perfil | círculo |

## Componentes base

Se construyen en el Sprint 1 y **nadie escribe un botón a mano después**. Si
falta un componente, se agrega aquí primero.

| Componente | Notas |
|---|---|
| `BotonPrincipal` | Amarillo, texto Tinta, ancho completo, 54 dp de alto. Icono opcional a la derecha (`IconoAvanzar` en lo que avanza) |
| `BotonSecundario` | Fondo Blanco con contorno Borde y texto Grafito; con `color = Error`, texto y contorno en Error |
| `BotonDestacado` | Naranja, texto blanco. Para postularse y publicar |
| `BotonFlotante` | Píldora Naranja con icono y texto Blanco y sombra: la acción "Publicar" de P-09 y P-12 |
| `BotonTexto` | Sin fondo, texto Grafito |
| `CampoTexto` | Fondo Blanco, borde Borde, borde Tinta al enfocar; Hueso deshabilitado |
| `CampoContrasena` | Igual, con candado al inicio y ojo para mostrar y ocultar |
| `TarjetaTrabajador` | Foto con punto de disponibilidad, nombre, título y `CalificacionCompacta`; abajo, municipio, "No disponible" y precio desde |
| `TarjetaServicio` | Formato portafolio: foto 16:10 arriba con la categoría encima, y abajo título y rango de precio. Sin precio, el renglón no se pinta. Con `pausado = true` va al 60 % de opacidad y la categoría dice "Plomería · Pausado" (`H-21`, decidido por el líder el 2026-10-18 para P-12) |
| `TarjetaSolicitud` | Categoría y `EtiquetaEstado` arriba, título, presupuesto en píldora AmarilloSuave y fecha; postulaciones sin revisar en píldora Naranja |
| `ChipCategoria` | Amarillo cuando está activo, Blanco con contorno Borde cuando no |
| `Estrellas` | Naranja, media estrella permitida en lectura |
| `CalificacionCompacta` | Píldora AmarilloSuave con una estrella Naranja, el promedio y las reseñas. Para tarjetas |
| `TarjetaConversacion` | Iniciales, nombre, título de la solicitud, último mensaje, hora y contador de «sin leer»; con mensajes sin leer, contorno y avatar en Amarillo (P-15). Registrada por `S5-T05` (`H-49`) |
| `BurbujaMensaje` | Propios a la derecha en Amarillo, de la otra persona a la izquierda en Blanco con contorno, con la esquina de abajo del lado de quien habla casi recta; hora y estado de envío debajo (P-16). Registrada por `S5-T05` (`H-49`) |
| `CampoMensaje` | Franja Blanca con campo de hasta 4 líneas y botón circular Tinta con la flecha Amarilla, apagado en Hueso con el texto en blanco (P-16). Registrada por `S5-T05` (`H-49`) |
| `EtiquetaEstado` | Píldora con el color del estado al 14 % de fondo, un punto del color pleno y texto Tinta |
| `Cargando` | Indicador centrado, amarillo |
| `EstadoVacio` | Icono en círculo AmarilloSuave rodeado por `ArcosCasco`, texto y botón opcional |
| `EstadoError` | Pantalla completa: icono en círculo con tinte Error, mensaje y botón de reintentar |
| `AvisoError` | El error de un formulario, en línea y compacto: franja con tinte Error, título, mensaje y "Reintentar" opcional |
| `BarraSuperior` | Sin relleno: toma el fondo de la pantalla (o `colorFondo` si la pantalla tiene cabecera de color). Título Tinta, más grande en pantallas raíz; flecha dentro de un círculo Hueso |
| `BarraInferior` | `BarraNavegacionFlotante`: cápsula Tinta flotante con 4 destinos; el actual se abre en una píldora Amarilla con su etiqueta y los demás muestran solo el icono en Borde |
| `GrupoOpciones`, `FilaOpcion`, `SeparadorOpciones` | Menú agrupado en tarjeta Blanca: icono en cuadro AmarilloSuave, texto, detalle y flecha. P-18 y la hoja de acciones de P-12 |
| `SeccionFormulario` | Bloque de formulario largo: tarjeta Blanca con icono y título. P-11 y P-13 |
| `ArcosCasco` | Firma gráfica: arcos concéntricos, como el ala del casco. Decoración de portadas y estados |
| `MedallaMarca` | El casco dentro de un círculo Blanco con sombra. Portada de P-02 |

### Colores de los estados de la solicitud

| Estado | Color |
|---|---|
| abierta | Éxito |
| asignada | Advertencia |
| cerrada | Gris |
| cancelada | Error |

## Iconos

Material Icons, variante `Outlined`. Sin librerías externas y sin emoji en la
interfaz.

### Iconos de categorías de oficios

Las 16 categorías del sistema (`categorias` en base de datos) tienen asignado
su icono correspondiente de Material Icons Outlined:

| Orden | Categoría | Icono Material Outlined | Nombre Compose (`Icons.Outlined.*`) | Uso / Representación |
|---|---|---|---|---|
| 10 | Plomería | `plumbing` | `Plumbing` | Llave y tubería hidráulica |
| 20 | Electricidad | `bolt` | `Bolt` | Rayo de energía e instalaciones eléctricas |
| 30 | Albañilería | `construction` | `Construction` | Herramientas de obra y edificación |
| 40 | Carpintería | `carpenter` | `Carpenter` | Sierra y trabajos en madera |
| 50 | Pintura | `format_paint` | `FormatPaint` | Rodillo de pintura y acabados |
| 60 | Herrería | `hardware` | `Hardware` | Yunque y herramientas de metalistería |
| 70 | Limpieza | `cleaning_services` | `CleaningServices` | Botella rociadora y limpieza |
| 80 | Jardinería | `yard` | `Yard` | Jardín, poda y exteriores |
| 90 | Mudanzas y carga | `local_shipping` | `LocalShipping` | Camión de fletes y transporte de carga |
| 100 | Aire y refrigeración | `ac_unit` | `AcUnit` | Copo de nieve y climatización |
| 110 | Mecánica | `car_repair` | `CarRepair` | Automóvil y mantenimiento mecánico |
| 120 | Cómputo | `computer` | `Computer` | Computadora y soporte técnico |
| 130 | Cerrajería | `key` | `Key` | Llave y seguridad de acceso |
| 140 | Costura | `content_cut` | `ContentCut` | Tijeras y confección textil |
| 150 | Cocina y eventos | `restaurant` | `Restaurant` | Cubiertos, gastronomía y banquetes |
| 999 | Otros | `more_horiz` | `MoreHoriz` | Puntos suspensivos / oficios diversos |

Estos valores están sincronizados con la columna `categorias.icono` en
`basedatos/04_datos_semilla.sql`.

La primera columna es `categorias.orden`, el que decide cómo se listan, **no el
`id`**: los `id` son un `smallserial` del 1 al 16 y no se usan en la interfaz.

La traducción de `categorias.icono` al icono vive en una sola función,
`iconoCategoria(nombre)` de `ui/componentes/IconosCategoria.kt` (`S3-T02`). Un
nombre nulo o desconocido cae en `MoreHoriz`, el de "Otros". Si se agrega una
categoría, se agrega aquí, en la semilla y en esa función.

## Textos

Todo en español. Tuteo, sin usted. Frases cortas.

**Los mensajes de error dicen qué hacer**, no solo qué falló. "El correo ya
está registrado, inicia sesión" y no "Error 409".

Todas las cadenas van en `strings.xml`. Ninguna cadena de interfaz vive dentro
de un `@Composable`.

## Estados que toda pantalla con datos debe tener

Cargando, vacío, error y contenido. **Las cuatro.** Una pantalla que solo
maneja el caso feliz no pasa revisión de pull request.
