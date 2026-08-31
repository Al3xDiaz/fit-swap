# RepLog — Documentación técnica

Overview técnico del proyecto. Para el resto de la documentación de producto/diseño, ver el índice
en el [README de la raíz](../README.md#documentación).

## Estado actual

Scaffold por defecto de Android Studio ("Empty Activity" + Compose). No hay código de features
todavía: solo `MainActivity` y el tema Compose generado. Todas las decisiones de arquitectura
(modelo de datos, persistencia, navegación, DI) están abiertas — ver
[`DESIGN_DOC.md`](DESIGN_DOC.md#preguntas-abiertas).

## Stack

- **Lenguaje:** Kotlin
- **UI:** 100% Jetpack Compose (Material 3, vía Compose BOM) — sin layouts XML
- **Build:** Gradle con Kotlin DSL (`build.gradle.kts`), AGP vía version catalog
  (`gradle/libs.versions.toml`)
- **`applicationId`:** `com.example.fitswap`
- **`minSdk`:** 24 · **`targetSdk`/`compileSdk`:** 37
- **Sin definir aún:** inyección de dependencias, networking, persistencia, librería de navegación
  (son decisiones abiertas, no convenciones existentes — ver [`DESIGN_DOC.md`](DESIGN_DOC.md))

## Estructura de carpetas

```
app/
  src/
    main/java/com/example/fitswap/
      ui/theme/        # colores, tipografía, FitSwapTheme
      MainActivity.kt
    test/               # tests unitarios (JVM)
    androidTest/        # tests instrumentados (requieren device/emulador)
gradle/
  libs.versions.toml    # version catalog — toda dependencia nueva se agrega aquí, no inline
docs/                    # esta carpeta
```

## Comandos

Módulo Gradle único (`:app`).

| Comando | Qué hace |
|---|---|
| `./gradlew assembleDebug` | Compila el APK debug |
| `./gradlew test` | Tests unitarios (JVM, `app/src/test`) |
| `./gradlew test --tests "com.example.fitswap.ExampleUnitTest"` | Corre una sola clase de test |
| `./gradlew connectedAndroidTest` | Tests instrumentados (`app/src/androidTest`, necesita device/emulador conectado) |
| `./gradlew lint` | Lint |
| `./gradlew check` | Lint + tests |

## Ver también

- [`PRD.md`](PRD.md) — qué se va a construir y por qué (producto)
- [`DESIGN_DOC.md`](DESIGN_DOC.md) — cómo se va a construir (arquitectura, decisiones pendientes)
- [`DIAGRAMS.md`](DIAGRAMS.md) — diagrama de arquitectura y flujo de pantallas
- [`TESTING.md`](TESTING.md) — estrategia y convenciones de pruebas (unitarias, integración, aceptación)
- [`../gym-app-ideas.md`](../gym-app-ideas.md) — notas originales de producto, fuente de todo lo anterior
