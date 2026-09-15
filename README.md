# RepLog (FitSwap)

[![Pipeline](https://gitlab.com/Al3xDiaz/fit-swap/badges/main/pipeline.svg)](https://gitlab.com/Al3xDiaz/fit-swap/-/pipelines)
[![Coverage](https://gitlab.com/Al3xDiaz/fit-swap/badges/main/coverage.svg)](https://gitlab.com/Al3xDiaz/fit-swap/-/graphs/main/charts)
[![Release](https://gitlab.com/Al3xDiaz/fit-swap/-/badges/release.svg)](https://gitlab.com/Al3xDiaz/fit-swap/-/releases)
[![GitLab](https://img.shields.io/badge/GitLab-repo-fc6d26?logo=gitlab&logoColor=white)](https://gitlab.com/Al3xDiaz/fit-swap)
[![GitHub](https://img.shields.io/badge/GitHub-mirror-181717?logo=github&logoColor=white)](https://github.com/Al3xDiaz/fit-swap)

App nativa de Android para llevar el registro de entrenamientos en el gimnasio con fricción
mínima: ejercicios, series (calentamiento / aproximación / efectiva), rutinas e historial — con un
flujo rápido de **"cambiar ejercicio"** por un sustituto equivalente cuando una máquina o barra
está ocupada, sin perder el hilo de la rutina.

> **Estado actual:** app funcional (Kotlin + Jetpack Compose, Room, Hilt) con rutinas, historial,
> medidas corporales, temporizadores y el flujo de cambio de ejercicio implementados. CI en GitLab
> corre build, tests, lint, coverage y publica una release firmada por cada cambio en `main` — ver
> [`docs/`](docs/) para el resto de la documentación y [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md)
> para la convención de ramas.

## Descargar

Escaneá el QR para bajar el APK de la última release (Android 7.0+, hay que permitir instalar
desde "orígenes desconocidos"):

<p align="center"><img src="docs/img/download-apk.png" alt="QR para descargar la última release" width="180"></p>

Es un link permanente — apunta siempre a la release más reciente, no hace falta regenerarlo:
[`.../releases/permalink/latest/downloads/app-release.apk`](https://gitlab.com/Al3xDiaz/fit-swap/-/releases/permalink/latest/downloads/app-release.apk)

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
| [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) | Convención de ramas (`feat/*`, `fix/*`, ...) y cómo se calcula la versión con GitVersion |
| [`gym-app-ideas.md`](gym-app-ideas.md) | Notas originales de producto (español) — fuente de la documentación anterior |
| [`rutina_semanal_optimizada.md`](rutina_semanal_optimizada.md) | Rutina semanal que se precargará como rutina por defecto |
