# Diseño de Mi cuenta (P-18)

> Especificación de detalle de P-18, entregable de `S2-T11`. Convierte el
> wireframe de `S1-T14` (`WIREFRAMES.md`, sección 7) en algo implementable.
> Las reglas comunes de la sección 1 de `DISENO-AUTENTICACION.md` aplican
> aquí tal cual: estructura, catálogo de componentes, cuándo se valida,
> normalización, teclado y accesibilidad. Este documento solo dice lo que P-18
> agrega.

## 0. Alcance

Cubre HU-05 (ver y corregir mis datos, cambiar mi contraseña, cerrar sesión)
y el final de HU-04 (aterrizar aquí al abrir el enlace de recuperación,
`DEC-27`).

No cubre la foto de perfil (HU-07, `S2-T13` y `S2-T14`): P-18 muestra las
iniciales.

**Lo que decidió el líder el 2026-10-11**, todas las opciones A del ticket:

| Qué | Decisión |
|---|---|
| Dónde se edita | En la misma P-18, que cambia de vista. No hay hojas modales |
| Qué pide "Cambiar contraseña" | Solo la contraseña nueva |
| Accesos del trabajador | "Mi perfil" (P-11) y "Mis postulaciones" (P-14) |
| Teléfono al editar | Obligatorio, 10 dígitos, como en P-03 |

**Dónde este documento se aparta del wireframe**, que es anterior a esas
decisiones y a `DEC-22`, `DEC-27` y `DEC-31`:

| El wireframe dice | Aquí |
|---|---|
| La tarjeta muestra el municipio | No: `public.usuarios` no tiene municipio |
| Se edita "nombre, teléfono y ubicación" | Nombre, apellidos y teléfono |
| El avatar abre el selector de imagen | Iniciales, sin tocar |
| "Cambiar contraseña" pide la actual y la nueva | Solo la nueva |
| "Ver mi perfil público" y "Mis servicios publicados" | "Mi perfil" y "Mis postulaciones" |
| "Cerrar sesión" en color `Error` | `BotonSecundario` tal cual. El catálogo no tiene un botón rojo y no se agrega uno para un solo uso |

## 1. Una ruta, tres vistas

P-18 es una sola ruta, `mi_cuenta`. Su contenido cambia entre tres vistas,
igual que P-04 cambia entre el formulario y la confirmación.

| Vista | Título de la barra | Flecha de regreso |
|---|---|---|
| Resumen | Mi cuenta | No: es destino de la barra inferior |
| Editar datos | Editar datos personales | Sí, al resumen |
| Cambiar contraseña | Cambiar contraseña | Sí, al resumen |

- La **barra inferior** del rol se queda en las tres.
- **Atrás** del sistema, desde una vista de edición, regresa al resumen
  **sin guardar**. Desde el resumen hace lo que haga la pila.
- Mientras un envío está en curso, ni la flecha ni atrás sacan de la vista.
- **Girar** no pierde la vista ni lo escrito: todo vive en el ViewModel.

## 2. Resumen

De arriba hacia abajo:

| # | Elemento | Componente | Notas |
|---|---|---|---|
| 1 | Tarjeta de la ficha | Superficie `Arena`, borde `Borde`, `FormaTarjeta` | |
| 1a | Iniciales | Círculo `Mostaza` de 80 dp, texto `titulo` en `Carbon` | Primera letra del nombre y del primer apellido. Se anuncia como "Iniciales de …" |
| 1b | Nombre completo | `cuerpoFuerte`, `Carbon` | Nombre y apellidos |
| 1c | Rol | Etiqueta `pie` en `Carbon` | Fondo `Mostaza` para el cliente y `Advertencia` para el trabajador |
| 1d | Correo | `secundario`, `Cafe` | Solo se lee |
| 1e | Teléfono | `secundario`, `Cafe` | "Sin teléfono" si la cuenta no tiene |
| 2 | Aviso | Texto `Exito` sobre `Arena`, borde `Exito` | Solo después de guardar. Región viva |
| 3 | "Editar datos personales" | `BotonSecundario` | |
| 4 | "Cambiar contraseña" | `BotonSecundario` | |
| 5 | "Mi perfil" | `BotonSecundario` | **Solo trabajador.** Va a P-11 |
| 6 | "Mis postulaciones" | `BotonSecundario` | **Solo trabajador.** Va a P-14 |
| 7 | Error del cierre | `EstadoError` en línea | Solo si cerrar sesión falló |
| 8 | "Cerrar sesión" | `BotonSecundario` | Abre el diálogo. Carga dentro del botón |

**El diálogo de cierre:** "¿Deseas cerrar tu sesión?", con "Cancelar" y
"Cerrar sesión". Tocar fuera cancela.

**Los cuatro estados:**

| Estado | Qué se ve |
|---|---|
| Cargando | `Cargando` a pantalla completa, bajo la barra |
| Error | `EstadoError` a pantalla completa con "Reintentar", sea cual sea el tipo: sin ficha no hay nada que mostrar |
| Vacío | **No aplica.** Quien tiene sesión tiene ficha |
| Normal | La tabla de arriba |

## 3. Editar datos

| # | Elemento | Componente | Notas |
|---|---|---|---|
| 1 | Nombre(s) | `CampoTexto` | Precargado. Tope 80. Mayúscula inicial, acción Siguiente |
| 2 | Apellidos | `CampoTexto` | Precargado. Tope 120. Mayúscula inicial, acción Siguiente |
| 3 | Teléfono celular | `CampoTexto` | Precargado. Solo dígitos, tope 10. Acción Hecho, que guarda |
| 4 | Error del envío | `EstadoError` en línea | "Reintentar" solo en `RED`, `SERVIDOR` y `DESCONOCIDO` |
| 5 | "Guardar cambios" | `BotonPrincipal` | Carga dentro del botón |

- **El correo y el rol no aparecen**, ni como campos deshabilitados
  (`DEC-22`, HU-05).
- **Validación:** las reglas, los mensajes y los momentos de P-03
  (`ValidacionesAuth`, 1.5 y 5 de `DISENO-AUTENTICACION.md`). Al guardar se
  marcan todos los que fallan y el foco va al primero.
- **Normalización:** nombre y apellidos con `trim()` al enviar.
- **Estados:** vacío no aplica; cargando va dentro del botón y bloquea los
  campos; el error no borra lo escrito.
- **Al guardar bien** se regresa al resumen, que muestra los datos que
  devolvió el repositorio y el aviso "Guardamos tus datos".

## 4. Cambiar contraseña

| # | Elemento | Componente | Notas |
|---|---|---|---|
| 1 | Explicación | Texto `cuerpo`, `Cafe` | "Escribe tu contraseña nueva. No te pedimos la anterior." |
| 2 | Contraseña nueva | `CampoContrasena` | Con mostrar y ocultar. Acción Hecho, que guarda |
| 3 | Ayuda | Texto `pie`, `Cafe` | "Mínimo 8 caracteres", siempre visible |
| 4 | Error del envío | `EstadoError` en línea | Igual que en 3 |
| 5 | "Guardar contraseña" | `BotonPrincipal` | Carga dentro del botón |

- **Un solo campo**, sin confirmación y sin contraseña anterior. Es lo que
  recibe `cambiarContrasena` y lo que deja usar la misma vista para quien
  llega por el enlace.
- **Validación:** la de la contraseña de P-03, mínimo 8.
- **Al guardar bien** se regresa al resumen con el aviso "Tu contraseña quedó
  actualizada". La sesión sigue abierta.
- **Al salir de la vista, lo escrito se borra.**

## 5. Contrato de estado

`EstadoMiCuenta`, en `ui/pantallas/`:

| Campo | Tipo | Para qué |
|---|---|---|
| `vista` | `VistaMiCuenta` | `RESUMEN`, `EDITAR_DATOS` o `CAMBIAR_CONTRASENA` |
| `usuario` | `Usuario?` | La ficha. Sale de `obtenerMiUsuario()`, **no de la `Sesion`** |
| `cargando` | `Boolean` | Carga de la ficha |
| `errorCarga` | `TipoError?` | La ficha no se pudo leer |
| `aviso` | `AvisoMiCuenta?` | `DATOS_GUARDADOS` o `CONTRASENA_CAMBIADA` |
| `nombre`, `apellidos`, `telefono` | `String` | Lo escrito en la edición |
| `errorNombre`, `errorApellidos`, `errorTelefono` | `@StringRes Int?` | Error de cada campo |
| `contrasena` | `String` | La contraseña nueva |
| `errorContrasena` | `@StringRes Int?` | Error del campo |
| `enviando` | `Boolean` | Envío del formulario de la vista actual |
| `errorFormulario` | `TipoError?` | Error del envío que no es de un campo |
| `confirmandoCierre` | `Boolean` | El diálogo está abierto |
| `cerrandoSesion` | `Boolean` | El cierre está en curso, o ya salió bien y la navegación está por sacar al usuario |
| `errorCierre` | `TipoError?` | El cierre falló |

## 6. Eventos

| Evento | Qué hace |
|---|---|
| `alReintentarCarga()` | Vuelve a leer la ficha |
| `alAbrirEditarDatos()` | Pasa a la edición con los datos de la ficha. Sin ficha no hace nada |
| `alAbrirCambiarContrasena()` | Pasa a la vista de contraseña, vacía |
| `alLlegarPorRecuperacion()` | Lo mismo: quien llega por el enlace entra directo ahí |
| `alVolverAlResumen()` | Regresa descartando lo escrito. Durante un envío no hace nada |
| `alCambiarNombre`, `alCambiarApellidos`, `alCambiarTelefono` | Guardan lo escrito, aplican el tope y limpian el error del campo |
| `alPerderFoco(campo)` | Valida ese campo, si ya se escribió en él |
| `alGuardarDatos()` | Valida todo y llama a `actualizarMiUsuario` |
| `alEscribirContrasena(valor)`, `alPerderFocoContrasena()` | Lo mismo para la contraseña |
| `alGuardarContrasena()` | Valida y llama a `cambiarContrasena` |
| `alReintentarFormulario()` | Repite el envío de la vista actual |
| `alPedirCerrarSesion()`, `alCancelarCierre()` | Abren y cierran el diálogo |
| `alConfirmarCierre()` | Llama a `cerrarSesion`. **No navega** |

## 7. De cada resultado a lo que ve el usuario

**`obtenerMiUsuario()`**

| Resultado | Qué se ve |
|---|---|
| `Exito(Usuario)` | El resumen |
| `Error`, cualquier tipo | `EstadoError` a pantalla completa con "Reintentar" |

**`actualizarMiUsuario(...)`**

| Resultado | Qué se ve |
|---|---|
| `Exito(Usuario)` | El resumen con los datos nuevos y el aviso |
| `Error(RED)`, `SERVIDOR`, `DESCONOCIDO` | El error en línea con "Reintentar". Lo escrito se queda |
| `Error(AUTENTICACION)`, `VALIDACION` | El error en línea, sin "Reintentar" |

**`cambiarContrasena(nueva)`**

| Resultado | Qué se ve |
|---|---|
| `Exito` | El resumen con el aviso |
| `Error(VALIDACION)` | **En el campo:** "Elige una contraseña distinta a la que ya tenías". Con el mínimo de 8 ya validado, lo único que Auth rechaza es `same_password`. No se pinta el `mensaje` del error, que viene en inglés (`H-11`) |
| `Error(RED)`, `SERVIDOR`, `DESCONOCIDO` | El error en línea con "Reintentar" |
| `Error(AUTENTICACION)` | El error en línea, sin "Reintentar" |

**`cerrarSesion()`**

| Resultado | Qué se ve |
|---|---|
| `Exito` | El botón sigue cargando hasta que la aplicación sale a P-02. P-18 no navega: `sesionActual()` emite `null` y lo hace `GrafoNavegacion` |
| `Error`, cualquier tipo | El error en línea sobre el botón, con "Reintentar" donde aplica. El usuario sigue en P-18 con la sesión abierta, que es lo que dice el contrato para el cierre sin red |

## 8. Cadenas nuevas de `strings.xml`

Las etiquetas de los tres campos de la edición y la ayuda de la contraseña
reutilizan `registro_nombre`, `registro_apellidos`, `registro_telefono` y
`registro_ayuda_contrasena`.

| Clave | Texto |
|---|---|
| `cuenta_titulo` | Mi cuenta |
| `cuenta_rol_cliente` | Cliente |
| `cuenta_rol_trabajador` | Trabajador |
| `cuenta_sin_telefono` | Sin teléfono |
| `cuenta_iniciales_descripcion` | Iniciales de %1$s |
| `cuenta_editar_datos` | Editar datos personales |
| `cuenta_cambiar_contrasena` | Cambiar contraseña |
| `cuenta_mi_perfil` | Mi perfil |
| `cuenta_mis_postulaciones` | Mis postulaciones |
| `cuenta_cerrar_sesion` | Cerrar sesión |
| `cuenta_cerrar_pregunta` | ¿Deseas cerrar tu sesión? |
| `cuenta_cancelar` | Cancelar |
| `cuenta_guardar` | Guardar cambios |
| `cuenta_aviso_datos` | Guardamos tus datos |
| `cuenta_contrasena_explicacion` | Escribe tu contraseña nueva. No te pedimos la anterior. |
| `cuenta_contrasena_nueva` | Contraseña nueva |
| `cuenta_contrasena_guardar` | Guardar contraseña |
| `cuenta_contrasena_rechazada` | Elige una contraseña distinta a la que ya tenías |
| `cuenta_aviso_contrasena` | Tu contraseña quedó actualizada |

## 9. Llegada por el enlace de recuperación

Son los pasos 5 y 6 del recorrido de `CONTRATOS-API.md` (`DEC-27`).

1. `MainActivity` importa la sesión del enlace. Si el fragmento trae
   `type=recovery`, le avisa a `SesionViewModel`.
2. `SesionViewModel` espera a que `sesionActual()` entregue la `Sesion`, con
   el tope de 8 segundos de P-01, y emite el rol.
3. `GrafoNavegacion` deja la pantalla de inicio del rol como base de la pila y
   abre P-18 encima, en la vista de contraseña.

| Caso | Qué pasa |
|---|---|
| Aplicación cerrada | Abre directo en P-18, vista de contraseña, con la barra inferior del rol |
| Aplicación abierta en P-02 | Lo mismo |
| Atrás desde la vista de contraseña | Resumen de P-18. **Se puede salir sin cambiarla:** el usuario ya tiene sesión |
| Atrás desde el resumen | Pantalla de inicio del rol. Otro atrás cierra la aplicación |
| La ficha no se puede leer en 8 segundos | La aplicación se queda donde estaba. No se avisa nada |
| Enlace vencido o ya usado | La aplicación abre en P-02, sin aviso. HU-04 no lo pide |
| Se gira el teléfono | El enlace no se procesa otra vez |
