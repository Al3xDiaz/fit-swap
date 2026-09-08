package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon

private data class MeasurementGuideItem(
    val id: String,
    val title: String,
    val instruction: String,
)

/** El peso se excluye a propósito — no es una circunferencia, no tiene técnica que explicar. */
private val guideItems = listOf(
    MeasurementGuideItem(
        "neck", "Cuello",
        "Cinta justo debajo de la laringe (nuez de Adán), ligeramente inclinada hacia abajo por delante."
    ),
    MeasurementGuideItem(
        "waist", "Cintura",
        "A la altura del ombligo o el punto más angosto del torso, sin contraer el abdomen, al final de una exhalación normal."
    ),
    MeasurementGuideItem(
        "hip", "Cadera",
        "En el punto más ancho de los glúteos, de pie con los pies juntos."
    ),
    MeasurementGuideItem(
        "chest", "Pecho",
        "A la altura de los pezones, cinta paralela al piso, en una respiración normal (sin inhalar ni exhalar al máximo)."
    ),
    MeasurementGuideItem(
        "arm", "Brazo",
        "En el punto más ancho del bíceps contraído, con el brazo paralelo al piso."
    ),
    MeasurementGuideItem(
        "leg", "Pierna",
        "En el punto más ancho del muslo, justo debajo del glúteo, de pie con el peso repartido en ambas piernas."
    ),
    MeasurementGuideItem(
        "calf", "Pantorrilla",
        "En el punto más ancho, de pie con el peso repartido en ambas piernas."
    ),
    MeasurementGuideItem(
        "glute", "Glúteo",
        "En el punto más ancho de los glúteos (mismo punto que cadera, pero mirando específicamente el volumen glúteo)."
    ),
    MeasurementGuideItem(
        "forearm", "Antebrazo",
        "En el punto más ancho, cerca del codo, con el puño cerrado y el antebrazo flexionado."
    ),
    MeasurementGuideItem(
        "shoulder", "Hombro",
        "Rodeando la parte más ancha, pasando por ambos deltoides, cinta paralela al piso."
    ),
    MeasurementGuideItem(
        "wrist", "Muñeca",
        "Justo debajo del hueso saliente (apófisis estiloides), en el punto más delgado."
    ),
)

@Composable
fun MeasurementGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar(title = "Cómo medir", navigationIcon = { BackNavigationIcon(onBack = onBack) }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("measurementGuideList"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recomendaciones generales", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Medí siempre a la misma hora del día (por ejemplo, al levantarte) y del mismo lado " +
                            "del cuerpo. Usá una cinta métrica flexible, sin apretar ni dejarla floja, y promediá " +
                            "2 mediciones de cada zona para reducir el margen de error.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                }
            }
            items(guideItems, key = { it.id }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("measurementGuideItem_${item.id}")
                        .padding(vertical = 8.dp)
                ) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.instruction, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider()
            }
        }
    }
}
