package com.example.fitswap

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag

/**
 * Espera hasta que exista un nodo con [tag] — necesario desde M11 para pantallas cuyo ViewModel
 * resuelve su estado inicial con una consulta real a Room (asíncrona de verdad, a diferencia de
 * los `Fake*Repository` de antes que emitían síncronamente desde un `MutableStateFlow`). El
 * auto-sync de Compose Test espera recomposición/animaciones, no I/O de background arbitrario, así
 * que un `performClick()` seguido de una aserción inmediata puede correr antes de que la consulta
 * termine — usar esto en el primer punto de cada pantalla nueva que dependa de datos cargados así.
 */
fun ComposeTestRule.waitUntilTagExists(tag: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
}
