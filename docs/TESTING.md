# RepLog — Estrategia de pruebas

> El repo hoy es solo el scaffold de Android Studio — no hay tests porque no hay código de
> features todavía. Este documento fija el estándar a seguir a medida que se implementen los
> requisitos must-have de [`PRD.md`](PRD.md), no una descripción de tests existentes.

## Pirámide de pruebas

Sigue la convención estándar de un proyecto Android + Compose, alineada con los comandos ya
definidos en la raíz del repo ([`CLAUDE.md`](../CLAUDE.md) / [`README.md`](README.md)):

| Nivel | Ubicación | Qué cubre | Herramientas |
|---|---|---|---|
| **Unitarias** | `app/src/test` (JVM, `./gradlew test`) | Dominio: use cases, lógica de sustitución de ejercicios, sugerencia de peso/reps, temporizador, agregación de Resumen/Reporte, ViewModels — sin dependencias de Android. | JUnit4/5, `kotlinx-coroutines-test` para código asíncrono/Flows, MockK o fakes para colaboradores. |
| **Integración** | `app/src/test` (Robolectric) o `app/src/androidTest` (instrumentadas, `./gradlew connectedAndroidTest`) según corresponda | Persistencia (DAOs de Room una vez elegida la librería), import/export JSON/CSV (round-trip), almacenamiento de preferencias (Configuraciones). | Room testing utilities, Robolectric si no requiere un dispositivo real. |
| **Aceptación / UI** | `app/src/androidTest`, Compose UI Testing | Flujos completos de principio a fin mapeados a requisitos must-have del PRD (ver tabla de trazabilidad más abajo). | `createComposeRule` / `createAndroidComposeRule`, `androidx.compose.ui.test`. |

La mayoría de la lógica de producto (cálculo de pesos, flujo lineal de tipos de serie, sustitución
de ejercicios, agregaciones) debe vivir en clases de dominio puras de Kotlin, testeables sin
Android — esto es lo que hace viable tener buena cobertura unitaria sin depender de emulador.

## Trazabilidad a producto

Cada prueba de aceptación debe poder rastrearse a un requisito must-have de
[`PRD.md`](PRD.md#must-have-mvp). Usar formato Given/When/Then en el nombre o comentario del test
ayuda a mantener esa trazabilidad legible. Ejemplos (no exhaustivo):

| Requisito (PRD) | Escenario de aceptación | Tipo de prueba |
|---|---|---|
| Peso efectivo nunca vacío | Dado un ejercicio sin historial, al entrar a "Ejercicio activo" el peso sugerido es 1 kg | Unitaria (dominio) + UI |
| Reps con steppers | Dado un objetivo de 8 reps en la rutina, el campo se pre-llena en 8 y los botones +/- lo ajustan sin tocar el objetivo de la rutina | Unitaria + UI |
| Flujo lineal de tipos de serie | Dado que estoy en "aproximación", no puedo saltar a "efectiva" sin completar las series de aproximación, ni volver a "calentamiento" | Unitaria (dominio) |
| Edición de peso por etapa | Dado que avancé de "calentamiento" a "aproximación", el peso de la serie de calentamiento queda de solo lectura | Unitaria + UI |
| Cambiar ejercicio | Al hacer swap a un sustituto, el registro de series ya hechas en la rutina no se pierde | Integración + UI |
| Temporizador de descanso | Al registrar una serie, arranca una cuenta regresiva con la duración configurada para ese tipo de serie | Unitaria (dominio) |
| Menú de sesión | Desde "Ejercicio activo", tocar otro ejercicio del día navega a él sin perder series ya registradas | UI |
| Terminar rutina | Al confirmar "Terminar rutina?" se cierra la sesión y vuelve a Rutinas; al cancelar, se mantiene en el menú de sesión | UI |
| Editar rutina | Eliminar un día o ejercicio se refleja en el detalle de la rutina; la rutina por defecto no ofrece "Eliminar rutina" | Integración + UI |
| Import/export | Exportar y volver a importar el mismo archivo reproduce el mismo estado (rutinas, ejercicios, historial) | Integración |

## Convenciones

- **Nombres de test**: usar backticks con descripción en palabras
  (`` fun `peso efectivo es 1kg cuando no hay historial previo`() ``) en vez de
  `camelCase` abreviado — más legible como documentación viva del comportamiento.
- **Ubicación por capa**: dominio/ViewModels en `app/src/test`; cualquier cosa que dependa del
  framework de Android (Room, Compose, Context) en `app/src/androidTest` salvo que Robolectric la
  cubra desde `app/src/test`.
- **Mocks vs. fakes**: preferir fakes simples (ej. un repositorio en memoria) sobre mocks para
  colaboradores con lógica sencilla — un fake ejercita el contrato real en vez de solo verificar
  llamadas. Reservar mocks (MockK) para dependencias con efectos secundarios costosos o difíciles
  de fakear (ej. temporizador del sistema, almacenamiento de archivos).
- **Determinismo**: nada de `Thread.sleep` ni dependencia del reloj real — usar
  `kotlinx-coroutines-test` (`TestDispatcher`/`runTest`) para lógica con coroutines y temporizador,
  y una fuente de tiempo inyectable en vez de `System.currentTimeMillis()` directo en dominio.
