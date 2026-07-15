# Handoff: Gestor de Suscripciones (Android)

## Overview
App móvil Android para registrar y organizar suscripciones recurrentes (streaming, música, almacenamiento en la nube, compras/supermercado online, delivery de comida, transporte, y categorías personalizadas). Muestra el gasto mensual/anual total, agrupa suscripciones por categoría, permite crear categorías y suscripciones, y guarda datos de acceso (sitio web, usuario, contraseña) por servicio.

## About the Design Files
Los archivos de este paquete son **referencias de diseño creadas en HTML** (un prototipo interactivo construido como Design Component) — muestran el look & feel y el comportamiento previsto, no código de producción para copiar directamente. La tarea es **recrear este diseño en el entorno de la app Android real** (Kotlin/Jetpack Compose recomendado, o el stack que ya use el proyecto), aplicando los patrones y componentes propios de esa base de código.

## Fidelity
**Alta fidelidad (hifi).** Colores, tipografía, espaciados e interacciones están definidos y deben recrearse con precisión. El prototipo es completamente funcional (estado en memoria, sin backend): agregar/editar/eliminar suscripciones y categorías, expandir/colapsar, cambio de tema, cambio de estilo visual.

## Estilos visuales (3 variantes intercambiables)
La app ofrece 3 tratamientos visuales alternos, seleccionables en runtime con un botón en el header (ícono de ciclo, junto al "+"). Implementar como un modo de tema global de la app (persistente en preferencias de usuario):

1. **Lista** (default) — tech minimalista. Fondo oscuro/claro conmutable (switch sol/luna). Filas de categoría con ícono en cuadrado con degradado y sombra interior sutil ("bajo relieve"), tipografía Space Grotesk para números.
2. **Máquina de escribir** — monocromático, fondo salvia (#E7EDE3), bordes finos 1.4px negros, tipografía monoespaciada (JetBrains Mono) en mayúsculas, filas separadas por líneas (no tarjetas/sombras), signo "+/−" en vez de chevron para expandir.
3. **Suave (neumórfico)** — paleta cálida crema (#EFE9DD), tarjetas con sombra dual neumórfica (relieve suave), íconos de categoría en cuadrado con sombra interior "presionada".

Los 3 modos comparten la misma estructura de datos y flujos; solo cambian tokens visuales (color, tipografía, forma de las tarjetas) y el ícono de categoría (con contorno vs. relleno con degradado).

## Screens / Views

### 1. Inicio (Home)
**Propósito:** vista principal — resumen de gasto y listado de categorías con sus suscripciones.

**Layout:** columna con padding horizontal 20px. De arriba a abajo:
- Header fijo (56px alto): título "Suscripciones" (700 21px Space Grotesk) a la izquierda; a la derecha, en fila con gap 8px: botón circular 32px (ícono de ciclo, alterna estilo visual) + botón circular 40px (ícono de suma, agrega suscripción) + botón circular 40px (switch sol/luna, alterna tema claro/oscuro).
- Tarjeta de resumen: fondo `--surface`, borde 1px `--border`, radio 20px, padding 20px 22px. Contiene: label "GASTO MENSUAL TOTAL" (uppercase, 12px, `--textMuted`), monto grande (700 38px Space Grotesk, tabular-nums), fila con "$X al año" y "N suscripciones activas" (13px, `--textMuted`), y si hay suscripciones en USD, una nota de conversión ("Convierte USD a CLP a $950 por dólar (referencial, Google Finance)").
- Lista de categorías (una tarjeta por categoría, ver "Componente: Fila de categoría").
- Botón "+ Nueva categoría" al final (borde punteado).

**Componente: Fila de categoría (modo Lista)**
- Contenedor: radio 20px, fondo `--surface`, sombra `inset 0 1px 1px var(--shadowHi), inset 0 -2px 5px var(--shadowLo)`.
- Fila clickeable (toggle expandir): padding 15px 16px, gap 13px.
  - Ícono 44×44px, radio 14px, `background: linear-gradient(155deg, color-mix(color 88% white) 0%, color 60%, color-mix(color 80% black) 100%)`, sombra `0 3px 8px -2px color-mix(color 55% transparent), inset 0 1px 1px rgba(255,255,255,.35)`. Dentro: ícono SVG blanco (stroke 1.7) según tipo de categoría (ver "Íconos por categoría"), o iniciales (2 letras, Space Grotesk 700 13px) si no hay ícono mapeado.
  - Nombre de categoría (13.5px 600 Manrope, `--textMuted`) sobre el monto (19px 700 Space Grotesk, `--text`).
  - A la derecha: botón editar (lápiz, 28px), chip con conteo "N Suscrip." (uppercase 10.5px, fondo `--surface2`, radio 100px), chevron (rota 180° si expandido).
- Al expandir: lista de suscripciones (filas), separadas por borde inferior 1px `--border`. Cada fila: logo 34×34px radio 10px fondo color del logo + inicial(es) blanco Space Grotesk 700 13px; nombre (14.5px 600) y "Próx. cobro · D mmm" (12px `--textMuted`); a la derecha precio (14px 700 tabular-nums) y ciclo ("Mensual"/"Anual", 11.5px `--textMuted`).
- Suscripciones ordenadas por fecha de próximo cobro ascendente.
- Botón "+ Agregar suscripción" al pie de la lista expandida.

### 2. Detalle de categoría (usado por el modo "Suave")
**Propósito:** listado de suscripciones de una categoría sin expandir/colapsar en línea.
**Layout:** header con back + título + botón editar categoría. Cuerpo: ícono grande de categoría (56px) centrado, subtotal grande, conteo; luego lista de suscripciones (misma fila que en Home) dentro de una tarjeta; botón "+ Agregar suscripción".

### 3. Detalle de suscripción
**Propósito:** ver todos los datos de una suscripción.
**Layout:** header con back + "Detalle". Cuerpo centrado: logo 64×64px radio 18px + nombre (20px 700) + chip de categoría (accent). Tarjeta con filas (borde inferior entre filas):
- Precio (+ si es USD, línea secundaria "≈ $X CLP" con la conversión referencial)
- Ciclo de pago
- Próximo cobro
- Método de pago
- Sitio web (enlace clickeable, abre en navegador)
- Usuario
- Contraseña (oculta por defecto como puntos "•", botón de ojo para mostrar/ocultar)

Botones: "Editar suscripción" (fondo `--accent`), y "Eliminar suscripción" (outline) que pide confirmación inline (tarjeta con borde `--danger` y botones Cancelar/Eliminar) antes de borrar.

### 4. Agregar/Editar suscripción
**Propósito:** formulario para crear o modificar una suscripción.
**Campos (en orden):**
1. Nombre (texto)
2. Categoría (chips horizontales scrollable, una por categoría existente, resaltado con el color de la categoría al seleccionar)
3. Moneda (2 botones: CLP / USD) y Ciclo (2 botones: Mensual / Anual) en fila
4. Precio (numérico)
5. Próxima fecha de cobro (date picker)
6. Método de pago (texto libre, ej. "Visa •••• 4242")
7. Sitio web (texto, ej. "netflix.com")
8. Usuario (texto)
9. Contraseña (campo con botón mostrar/ocultar)
10. Color del logo (selector de 8 swatches circulares de la paleta)

Botón "Guardar suscripción" (fondo `--accent`, ancho completo). Si es edición, botón adicional "Eliminar suscripción" (outline, rojo).

### 5. Agregar/Editar categoría
**Campos:** vista previa del ícono/mono grande centrado, Nombre (texto), Color (8 swatches). Botón "Guardar categoría". Si es edición: botón "Eliminar categoría" que, si tiene suscripciones asociadas, muestra advertencia inline ("También se eliminarán sus N suscripciones") con Cancelar/Eliminar.

## Íconos por categoría
Se infieren por palabra clave en el nombre de la categoría (case-insensitive, `includes`); si no hay coincidencia, se usan las iniciales:
- streaming, video, tv → ícono "play"
- music, música, musica, audio → ícono "nota musical"
- almacen, nube, cloud, drive → ícono "nube"
- compra, super, mercado, tienda → ícono "carrito"
- comida, delivery, food, restaur → ícono "caja/delivery"
- transporte, auto, car, movilidad, viaje → ícono "auto"

(Los paths SVG exactos están en la función `iconForCategory` del archivo adjunto — usar como referencia para recrear los glifos, o sustituir por el icon set nativo del proyecto Android, ej. Material Symbols equivalentes: play_circle, music_note, cloud, shopping_cart, local_shipping/restaurant, directions_car.)

## Interactions & Behavior
- **Expandir/colapsar categoría** (modo Lista): tap en la fila alterna `expanded`; chevron rota 180°.
- **Alternar estilo visual**: botón de ciclo en el header avanza Lista → Máquina de escribir → Suave → Lista… Se debe persistir la elección del usuario (ej. SharedPreferences/DataStore).
- **Alternar tema claro/oscuro**: botón sol/luna; no aplica en modo "Máquina de escribir" ni "Suave" (paleta fija en esos modos).
- **Guardar suscripción**: valida que `nombre` no esté vacío y que haya categoría seleccionada. Si el logo no tiene letra definida, se autogenera desde la primera letra del nombre.
- **Guardar categoría**: valida `nombre` no vacío; genera iniciales (mono) automáticamente a partir de las primeras letras de hasta 2 palabras del nombre.
- **Eliminar suscripción / categoría**: requieren confirmación inline (no modal nativo) — tarjeta con mensaje y dos botones.
- **Mostrar/ocultar contraseña**: toggle de ícono ojo/ojo-tachado, tanto en el detalle como en el formulario.
- **Cálculo de gasto mensual total**: suma de todas las suscripciones convertidas a equivalente mensual (`Anual / 12`) y a CLP (`USD × tasa`); se muestra también el equivalente anual (`× 12`).
- **Conversión de moneda**: cada suscripción tiene `currency: 'CLP' | 'USD'`. Al calcular totales, las suscripciones en USD se convierten a CLP usando una tasa de referencia (`USD_CLP_RATE = 950` en el prototipo). **Importante para producción:** reemplazar esta constante por una consulta real al valor del dólar (ej. API de Google Finance / mindicador.cl / un proveedor de tasas), cacheado y actualizado periódicamente — no hardcodear.
- **Animaciones**: fade-in sutil (`opacity 0→1, translateY 6px→0`, ~180ms) al cambiar de pantalla.

## State Management
Estado mínimo requerido (a nivel de app / ViewModel):
- `theme`: 'dark' | 'light'
- `layoutStyle` / `styleOverride`: 'list' | 'typewriter' | 'soft' (preferencia persistida)
- `categories`: lista de `{ id, name, color, mono, subs: [] }`
- `sub`: `{ id, name, categoryId, price, currency, cycle, nextDate, payment, website, username, password, logoColor, logoLetter }`
- Navegación entre pantallas: home, detalle de categoría, detalle de suscripción, editar suscripción, editar categoría (back stack simple).
- UI transitoria: categoría expandida (por categoría), mostrar/ocultar contraseña, confirmación de borrado pendiente.

No hay backend en el prototipo — todo vive en memoria. Para producción, definir persistencia (Room/DataStore local, y opcionalmente sync remoto) y considerar cifrado del campo contraseña (no debería guardarse en texto plano).

## Design Tokens

### Colores — modo Lista (oscuro)
- `--bg: #0B0D10` `--surface: #15181D` `--surface2: #1D2128` `--border: #262B33`
- `--text: #F2F4F7` `--textMuted: #8A94A6` `--accent: #5B8CFF` `--accentSoft: rgba(91,140,255,.16)` `--danger: #FF6B6B`

### Colores — modo Lista (claro)
- `--bg: #F3F4F7` `--surface: #FFFFFF` `--surface2: #F0F1F5` `--border: #E3E6EC`
- `--text: #14161A` `--textMuted: #6B7280` `--accent: #3763E0` `--accentSoft: rgba(55,99,224,.10)` `--danger: #E0453B`

### Colores — modo Máquina de escribir
- `--bg / --surface: #E7EDE3` `--surface2: #DCE4D7` `--border / --accent / --text: #1C1E1B`
- `--textMuted: #5B6157` `--danger: #8B3A2B`

### Colores — modo Suave (neumórfico)
- `--bg / --surface: #EFE9DD` `--surface2: #E6DFD0` `--border: rgba(90,74,52,.12)`
- `--text: #2E2013` `--textMuted: #8C8171` `--accent: #A9782E` `--danger: #B5493B`

### Paleta de categorías/logos (swatches selector)
`#5B8CFF, #22C55E, #F59E0B, #EF4444, #A855F7, #06B6D4, #EC4899, #84CC16`

### Tipografía
- Titulares y números: **Space Grotesk** (500/600/700)
- Texto de cuerpo/labels: **Manrope** (400/500/600/700/800)
- Modo Máquina de escribir: **JetBrains Mono** (500/600/700) para todo el texto de esa variante
- Tamaños mínimos usados: 10.5px (labels chip) → 38px (monto total). Nunca bajar de ~10px.

### Radios y sombras
- Tarjetas: radio 18–24px según variante
- Íconos: radio 10–20px
- Botones circulares: 28–40px de diámetro
- Sombra "bajo relieve" (Lista): `inset 0 1px 1px var(--shadowHi), inset 0 -2px 5px var(--shadowLo)`
- Sombra neumórfica (Suave): `8px 8px 16px var(--shadowLo), -6px -6px 14px var(--shadowHi)` (tarjetas), inset equivalente para elementos "presionados"

## Assets
No se usan imágenes externas. Los "logos" de servicios son placeholders: círculo/cuadrado de color + inicial(es) del nombre (ej. Netflix → fondo `#E50914`, letra "N"). Los íconos de categoría son SVG de línea dibujados a mano (paths en el archivo adjunto). En producción, reemplazar los placeholders de logo por los logotipos reales de cada servicio (el usuario los sube/selecciona) y opcionalmente sustituir los íconos de categoría por un icon set del proyecto (Material Symbols u otro).

## Files
- `Gestor de Suscripciones.dc.html` — prototipo funcional completo (todas las pantallas, los 3 estilos visuales, lógica de estado en la clase `Component`). Referencia principal para colores exactos, estructura de datos, copy y comportamiento. Ábrelo en un navegador para interactuar con el prototipo.
