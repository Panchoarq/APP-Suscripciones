# Devlog — Gestor de Suscripciones (Android)

Registro del proceso de desarrollo de la app, en orden cronológico.

## 2026-07-14 — Definición y arquitectura

- Punto de partida: diseño de alta fidelidad en HTML (prototipo interactivo) en [`design_handoff_gestor_suscripciones/`](design_handoff_gestor_suscripciones/), con spec completo en su `README.md` (pantallas, tokens de diseño, 3 estilos visuales: Lista / Máquina de escribir / Suave).
- Decisión de arquitectura: app Android **nativa** (Kotlin + Jetpack Compose), **100% local** — sin sincronización entre dispositivos, sin backend, sin Firebase. Cada persona instala su propia copia en su celular.
- Justificación: al no requerir sync, no hay necesidad de autenticación ni de un servicio en la nube; simplifica drásticamente el proyecto (sin costos de hosting, sin cuentas externas más allá de Google Play si se publica).
- `applicationId` elegido: `com.pancho.suscripciones`.
- Alcance inicial acordado: implementar primero el **modo visual "Lista"** (dark/light) completo y funcional; los modos "Máquina de escribir" y "Suave" quedan para una siguiente iteración.

## Stack técnico

- **UI**: Jetpack Compose + Material 3, tema propio con tokens de color extraídos del prototipo HTML (fondo, superficie, texto, muted, accent, danger — variantes claro/oscuro).
- **Persistencia**: Room (SQLite) — entidades `CategoryEntity` y `SubscriptionEntity`.
- **Preferencias**: DataStore (tema claro/oscuro, estilo visual elegido).
- **Seguridad**: contraseñas de servicios cifradas con AES-256-GCM vía Android Keystore (`PasswordCrypto.kt`) — nunca se guardan en texto plano.
- **Navegación**: Navigation Compose (Home, Detalle de suscripción, Editar suscripción, Editar categoría).
- **Conversión de moneda**: tasa USD→CLP referencial hardcodeada (950), con TODO explícito para conectar a `mindicador.cl` en el futuro.

## Pantallas implementadas (modo Lista)

1. **Home**: tarjeta de resumen de gasto mensual/anual + lista de categorías expandibles con sus suscripciones.
2. **Detalle de suscripción**: datos completos, contraseña oculta con toggle mostrar/ocultar, eliminar con confirmación inline.
3. **Agregar/Editar suscripción**: formulario completo según spec (nombre, categoría, moneda, ciclo, precio, fecha, método de pago, sitio web, usuario, contraseña, color de logo).
4. **Agregar/Editar categoría**: nombre, color, eliminación con advertencia si tiene suscripciones asociadas.

## Puesta en marcha y debugging (build real en dispositivo)

- Proyecto abierto en Android Studio; se aceptó la actualización sugerida de AGP 8.5.2 → 8.9.1 y Gradle → 8.11.1.
- **Bug 1**: `Unresolved reference: clickable` en `SubscriptionEditScreen.kt` — un modifier de extensión mal escrito (`this.then(clickable(...))` sin receiver). Corregido a `this.clickable(onClick = onClick)`.
- **Ícono de la app**: placeholder vectorial inicial generado por código; luego reemplazado por el usuario con un ícono ilustrado propio vía el asistente "Image Asset" de Android Studio.
- Probado en dispositivo físico (Android, conexión USB con depuración habilitada) — build y ejecución exitosos.
- **Bug 2**: en modo oscuro, el texto de las pantallas de formulario (Agregar categoría/suscripción) se veía blanco sobre blanco. Causa: esas pantallas no aplicaban el fondo del tema explícitamente, heredando el fondo blanco por defecto de Android mientras el texto usaba colores de tema oscuro. Fix: `Surface` con `color = MaterialTheme.colorScheme.background` envolviendo el `NavHost` completo en `MainActivity.kt`.
- **Bug 3**: tras el fix anterior, apareció el problema inverso en Home en modo oscuro — textos oscuros sobre fondo oscuro (casi invisibles). Causa: varios `Text()`/`Icon()` no tenían `color`/`tint` explícito y dependían de la propagación automática de `LocalContentColor`, que no resolvía correctamente el color de contenido del `Surface` anidado. Fix: se agregó `color = MaterialTheme.colorScheme.onBackground` / `onSurface` explícito a todos los textos e íconos afectados en Home, Detalle y ambos formularios.

## Logos de suscripciones (asociación automática de ícono)

Funcionalidad agregada a pedido: reconocer y asignar automáticamente el logo/color de marca al agregar una suscripción, en dos capas (decisión: ambas combinadas, local primero):

1. **Registro de marcas local** (`BrandRegistry.kt`): ~35 servicios comunes (Netflix, Spotify, Disney+, HBO Max, Uber, Rappi, Google One, Dropbox, etc.) con su color de marca real y dominio conocido. Coincidencia por palabra clave al escribir el nombre — instantáneo, sin internet.
2. **Descarga de logo online como respaldo** (`LogoDownloader.kt`): botón "Buscar logo online" que descarga el favicon del sitio (servicio público de favicons de Google, sin API key) y lo guarda en almacenamiento interno del dispositivo — después de la primera descarga funciona sin conexión.
3. Se agregó el campo `logoImagePath` a `SubscriptionEntity`, lo que subió la versión de esquema de Room de 1 a 2 (`fallbackToDestructiveMigration()`, aceptable en esta etapa sin usuarios reales — implica que los datos de prueba cargados antes de este cambio se pierden en el próximo build).
4. Renderizado del logo descargado en Home y en Detalle de suscripción (con fallback a iniciales + color si no hay imagen).

## Datos por defecto, fecha de cobro editable, medios de pago y respaldo

- **Seed de datos por defecto**: al primer inicio (flag `hasSeededDefaults` en DataStore, para que no se repita si luego el usuario borra todo) se precargan 6 categorías (Cloud, Compras, Delivery, Streaming, Supermercado, Transporte) con 15 suscripciones comunes distribuidas entre ellas (OneDrive, Google Drive, Mercado Libre, Rappi Pro, Uber Eats Pro, Netflix, Amazon Prime, HBO Max, Disney+, Paramount+, Apple TV+, Spotify, Jumbo, Uber, Cabify), con precio en $0 como placeholder para que el usuario complete el valor real. Todo se puede eliminar libremente, igual que cualquier categoría/suscripción agregada a mano. Se agregó "Mercado Libre" a `BrandRegistry.kt` para que tome su color de marca automáticamente.
- **Fecha de cobro editable**: el campo `nextChargeDateEpochDay` ya existía en el modelo pero no era editable desde la UI (quedaba fijo en "hoy + 1 mes" al crear). Se agregó un selector de fecha (`DatePickerDialog` de Material 3) en el formulario de suscripción.
- **Medios de pago configurables**: nueva entidad `PaymentMethodEntity` (Room, versión de esquema 2→3) con lista editable desde la nueva pantalla de Opciones (agregar/eliminar). El campo de método de pago en el formulario de suscripción pasó de texto libre a selección por chips sobre esa lista. Se sembran 3 medios de pago genéricos por defecto (Tarjeta de crédito, Tarjeta de débito, Transferencia).
- **Pantalla de Opciones** (`OptionsScreen.kt`, accesible desde un ícono de engranaje en Home): administración de medios de pago + sección de respaldo.
- **Exportar/Importar respaldo a la nube**: `BackupManager.kt` serializa categorías, suscripciones y medios de pago a JSON (usando `org.json`, sin dependencias nuevas) y usa Storage Access Framework (`ActivityResultContracts.CreateDocument`/`OpenDocument`) para que el usuario elija dónde guardarlo/leerlo — incluyendo Google Drive u otros proveedores de documentos, sin integrar una API de nube específica. Decisiones de producto acordadas: las contraseñas se incluyen **en texto plano** dentro del JSON (la clave de cifrado del Android Keystore no sobrevive una reinstalación o cambio de equipo, así que sin esto el respaldo no sería restaurable — se advierte en la propia pantalla que el archivo debe guardarse en un lugar privado); al importar, los datos se **fusionan** con lo existente evitando duplicados por nombre (categoría, medio de pago, suscripción dentro de su categoría), en vez de reemplazar todo.
- Bump de versión de esquema Room a 3 (sigue con `fallbackToDestructiveMigration()`, mismo criterio que el bump anterior).
- Verificado con `gradlew compileDebugKotlin` y `gradlew assembleDebug`: build exitoso.

## Pendiente / próximos pasos

- Implementar los modos visuales "Máquina de escribir" y "Suave" (theming alterno).
- Reemplazar `FontFamily.Default` por las tipografías reales del diseño (Space Grotesk, Manrope) — instrucciones en `Type.kt`.
- Conectar la tasa de cambio USD→CLP a `mindicador.cl` en vez del valor hardcodeado.
- Firmar un APK/AAB de "release" si se decide publicar en Play Store (actualmente solo se generó un APK de debug para compartir directamente).
- Completar precios reales de las suscripciones precargadas por defecto (quedan en $0 como placeholder).
