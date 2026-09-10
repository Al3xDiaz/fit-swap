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
import com.example.fitswap.domain.model.MeasurementField
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon

/** Instrucción de técnica por campo — título/id salen de [MeasurementField] (compartido con el
 * selector de campos del stepper de "Nueva medición"), así las 11 medidas no quedan duplicadas en
 * dos listas. El peso se excluye a propósito: no es una circunferencia, no tiene técnica que
 * explicar, y no forma parte de [MeasurementField]. */
private val instructionByField = mapOf(
    MeasurementField.NECK to "Cinta justo debajo de la laringe (nuez de Adán), ligeramente inclinada hacia abajo por delante.",
    MeasurementField.WAIST to "A la altura del ombligo o el punto más angosto del torso, sin contraer el abdomen, al final de una exhalación normal.",
    MeasurementField.HIP to "En el punto más ancho de los glúteos, de pie con los pies juntos.",
    MeasurementField.CHEST to "A la altura de los pezones, cinta paralela al piso, en una respiración normal (sin inhalar ni exhalar al máximo).",
    MeasurementField.ARM to "En el punto más ancho del bíceps contraído, con el brazo paralelo al piso.",
    MeasurementField.LEG to "En el punto más ancho del muslo, justo debajo del glúteo, de pie con el peso repartido en ambas piernas.",
    MeasurementField.CALF to "En el punto más ancho, de pie con el peso repartido en ambas piernas.",
    MeasurementField.GLUTE to "En el punto más ancho de los glúteos (mismo punto que cadera, pero mirando específicamente el volumen glúteo).",
    MeasurementField.FOREARM to "En el punto más ancho, cerca del codo, con el puño cerrado y el antebrazo flexionado.",
    MeasurementField.SHOULDER to "Rodeando la parte más ancha, pasando por ambos deltoides, cinta paralela al piso.",
    MeasurementField.WRIST to "Justo debajo del hueso saliente (apófisis estiloides), en el punto más delgado.",
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
            items(MeasurementField.entries, key = { it.tag }) { field ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("measurementGuideItem_${field.tag}")
                        .padding(vertical = 8.dp)
                ) {
                    Text(field.label, style = MaterialTheme.typography.titleMedium)
                    Text(instructionByField.getValue(field), style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider()
            }
        }
    }
}
