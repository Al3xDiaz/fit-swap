package com.example.fitswap

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.timer.AndroidCardioTimerController
import com.example.fitswap.timer.AndroidRestTimerController
import com.example.fitswap.timer.CardioTimerService
import com.example.fitswap.timer.CardioTimerState
import com.example.fitswap.timer.CardioTimerStatus
import com.example.fitswap.timer.RestTimerService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.junit.rules.ExternalResource

/**
 * Entry point mínimo para llegar a los controllers de timer desde una [TestRule], que no forma
 * parte del grafo de inyección de la Activity/Service — [EntryPointAccessors] es la forma
 * soportada por Hilt de resolver un binding del `SingletonComponent` fuera de una clase anotada
 * con `@AndroidEntryPoint`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimerControllersEntryPoint {
    fun restTimerController(): AndroidRestTimerController
    fun cardioTimerController(): AndroidCardioTimerController
}

/**
 * `RestTimerService`/`CardioTimerService` son foreground services reales que solo se detienen
 * solos al llegar a cero (o con la acción explícita de detener el cardio) — nada los para cuando
 * termina un test. Como toda la suite instrumentada corre en un único proceso y
 * `AndroidRestTimerController`/`AndroidCardioTimerController` son `@Singleton`, un service que
 * sobrevive a su test sigue publicando estado (incluido el "terminó" que apaga `restTimerLabel`)
 * mientras corre el test siguiente — de ahí la intermitencia al correr la suite completa que no
 * se reproduce corriendo la clase aislada (ver handoff.md).
 *
 * Esta regla detiene ambos services y resetea sus controllers al final de cada test, para que
 * cada test instrumentado arranque desde timers realmente apagados.
 */
class TimerServicesCleanupRule : ExternalResource() {

    override fun after() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.stopService(Intent(context, RestTimerService::class.java))
        context.stopService(Intent(context, CardioTimerService::class.java))

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TimerControllersEntryPoint::class.java,
        )
        entryPoint.restTimerController().updateRemaining(null)
        entryPoint.cardioTimerController().updateState(CardioTimerState(CardioTimerStatus.STOPPED, 0))
    }
}
