package com.example.fitswap.data.di

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Executor de Room para `androidTest`: envuelve cada tarea en un [CountingIdlingResource]
 * registrado en Espresso. Desde M11, `RoomRoutineRepository` y hermanos resuelven de forma
 * genuinamente asíncrona (a diferencia de los `Fake*Repository` de antes, que emitían
 * síncronamente desde un `MutableStateFlow`) — sin esto, `composeTestRule` considera la app "idle"
 * mientras una consulta a Room todavía está en vuelo en el executor de background, y un
 * `performClick()` seguido de una aserción inmediata puede correr antes de que la consulta
 * termine. Nunca se usa en producción, solo desde [TestDatabaseModule].
 */
object TestRoomExecutor {
    private val idlingResource = CountingIdlingResource("RoomQueries")
    private val delegate = Executors.newFixedThreadPool(4)
    private val registered = AtomicBoolean(false)

    val executor: Executor = Executor { command ->
        idlingResource.increment()
        delegate.execute {
            try {
                command.run()
            } finally {
                idlingResource.decrement()
            }
        }
    }

    fun ensureRegistered() {
        if (registered.compareAndSet(false, true)) {
            IdlingRegistry.getInstance().register(idlingResource)
        }
    }
}
