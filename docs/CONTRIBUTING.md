# Convención de ramas y versionado

Este repo usa un esquema **main-only** (sin `development`/`staging`): todo el trabajo se hace en
una rama de corta vida con un prefijo convencional y se mergea directo a `main`. El prefijo decide
cómo sube la versión — el cálculo lo hace [GitVersion](https://gitversion.net/) según
[`GitVersion.yml`](../GitVersion.yml), corriendo en el stage `version` del pipeline de GitLab.

| Prefijo de rama | Ejemplo | Incrementa |
|---|---|---|
| `feat/*` | `feat/selector-de-medidas` | **minor** (`0.1.0` → `0.2.0`) |
| `fix/*` | `fix/menu-sesion-swipe` | **patch** (`0.1.0` → `0.1.1`) |
| `refactor/*` | `refactor/repositorio-sets` | **major** (`0.1.0` → `1.0.0`) |
| `docs/*` | `docs/actualizar-prd` | sin bump |
| `cicd/*` | `cicd/agregar-stage-release` | sin bump |

## Regla crítica: el merge tiene que ser un commit de merge real

GitVersion lee el nombre de la rama origen desde el **mensaje del commit de merge**
(`Merge branch 'feat/x' into 'main'`). Un **squash merge** o un **rebase merge** no generan ese
mensaje — GitVersion no tiene de dónde leer el incremento y la versión se queda quieta.

El proyecto en GitLab ya está configurado para esto (`merge_method: merge`,
`squash_option: default_off`) — **no cambiar esa configuración**. Al mergear un merge request desde
la UI, dejar la opción de squash desmarcada.

## `main` no recibe pushes directos

Todo cambio entra por una rama con alguno de los prefijos de la tabla, mergeada a `main`. Un commit
directo sobre `main` no rompe nada (no hay protección de rama configurada todavía), pero no tiene
mensaje de merge que leer, así que no mueve la versión — queda como un commit "suelto" hasta el
próximo merge real.

## Válvula de escape manual

Un commit (de merge o no) con `+semver: none` o `+semver: skip` en el mensaje fuerza que ese commit
no incremente nada, sea cual sea su rama de origen.
