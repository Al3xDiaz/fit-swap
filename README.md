# RepLog (FitSwap)

App nativa de Android para llevar el registro de entrenamientos en el gimnasio con fricción
mínima: ejercicios, series (calentamiento / aproximación / efectiva), rutinas e historial — con un
flujo rápido de **"cambiar ejercicio"** por un sustituto equivalente cuando una máquina o barra
está ocupada, sin perder el hilo de la rutina.

> **Estado actual:** scaffold inicial de Android Studio (Kotlin + Jetpack Compose). Todavía no hay
> código de features — la app es únicamente `MainActivity` + tema generado. La visión de producto y
> las decisiones de arquitectura pendientes están documentadas en [`docs/`](docs/).

## Quickstart

```bash
./gradlew assembleDebug   # compila el APK debug
./gradlew test            # tests unitarios (JVM)
./gradlew check           # lint + tests
```

Requiere Android SDK (`minSdk 24`, `compileSdk`/`targetSdk 37`) — ver
[`docs/README.md`](docs/README.md) para detalles de stack y comandos.

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/README.md`](docs/README.md) | Overview técnico: stack, estructura de carpetas, comandos de Gradle |
| [`docs/PRD.md`](docs/PRD.md) | Product Requirements Doc: problema, usuarios, requisitos, métricas, fuera de alcance |
| [`docs/DESIGN_DOC.md`](docs/DESIGN_DOC.md) | Design Doc: contexto, objetivos, diseño propuesto, alternativas, preguntas abiertas |
| [`docs/DIAGRAMS.md`](docs/DIAGRAMS.md) | Diagrama de arquitectura y flujo de pantallas (base para storyboard en Figma) |
| [`gym-app-ideas.md`](gym-app-ideas.md) | Notas originales de producto (español) — fuente de la documentación anterior |
| [`rutina_semanal_optimizada.md`](rutina_semanal_optimizada.md) | Rutina semanal que se precargará como rutina por defecto |
