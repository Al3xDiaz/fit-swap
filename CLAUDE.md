# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

FitSwap (working name "RepLog") is a native Android app for logging gym workouts, currently
scaffolded from the default Android Studio "Empty Activity" Compose template — there is no
feature code yet beyond the generated `MainActivity`/theme files. The product vision lives in
`gym-app-ideas.md` (Spanish): a minimal-friction workout logger (exercises, sets, routines,
history) whose key differentiator is a fast "swap exercise" flow — each exercise has a curated
list of equivalent substitutes (same muscle group/movement pattern) so a user can switch mid-workout
without losing their routine's flow. Read that file before proposing architecture or features, since
no code yet reflects these decisions (data model, offline-first, local vs. account-based storage,
and the exercise/substitute catalog source are all still open questions listed there). Those
decisions have also been formalized under `docs/` (PRD, design doc, architecture/screen-flow
diagrams) — see `docs/README.md` for the index; `docs/DESIGN_DOC.md#preguntas-abiertas` mirrors the
same open questions.

## Commands

Single Gradle module (`:app`), Kotlin + Jetpack Compose, AGP via version catalog
(`gradle/libs.versions.toml`).

- Build debug APK: `./gradlew assembleDebug`
- Run unit tests (JVM, `app/src/test`): `./gradlew test`
- Run a single unit test class: `./gradlew test --tests "com.example.fitswap.ExampleUnitTest"`
- Run instrumented tests (`app/src/androidTest`, needs a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`
- Full check (lint + tests): `./gradlew check`

## Architecture

- `applicationId` / package root: `com.example.fitswap`; `minSdk 24`, `targetSdk`/`compileSdk 37`.
- UI is 100% Jetpack Compose (Material 3, via the Compose BOM) — no XML layouts. Compose theming
  (colors, typography, `FitSwapTheme`) lives under `app/src/main/java/com/example/fitswap/ui/theme/`.
- No dependency injection, networking, persistence, or navigation library is set up yet — any of
  these are architectural decisions to make (in line with the open questions in
  `gym-app-ideas.md`), not existing conventions to follow.
- Dependency versions and plugin ids are centralized in `gradle/libs.versions.toml` (the version
  catalog) and referenced from `app/build.gradle.kts` as `libs.xxx` — add new dependencies there,
  not as inline coordinate strings.
