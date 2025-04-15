package grapes.microservices.views.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRangeSlider(
    selectedRange: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    limitInterval: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int? = null,
    valueFormatter: (Float) -> String = { it.roundToInt().toString() },
    onValueChangeFinished: (() -> Unit)? = null
) {
    Column(modifier = modifier
        .padding(horizontal = 16.dp)) {
        // Rangée pour afficher les labels Min et Max actuels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Espace les labels
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valueFormatter(selectedRange.start),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = valueFormatter(selectedRange.endInclusive),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val startInteractionSource = remember { MutableInteractionSource() }
        val endInteractionSource = remember { MutableInteractionSource() }

        RangeSlider(
            value = selectedRange,
            onValueChange = onRangeChange,
            valueRange = limitInterval,
            // Slider interval : 0 = no clip interval; n = n possible values
            steps = steps ?: 0,
            onValueChangeFinished = onValueChangeFinished,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.secondary
            ),
            // --- Start Cursor ---
            startThumb = {
                SliderDefaults.Thumb(
                    interactionSource = startInteractionSource, // Source d'interaction
                    thumbSize = DpSize(30.dp, 30.dp), // Utilise la taille passée en paramètre
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary
                    )
                )
            },
            // --- End Cursor ---
            endThumb = {
                SliderDefaults.Thumb(
                    interactionSource = endInteractionSource, // Source d'interaction
                    thumbSize = DpSize(30.dp, 30.dp), // Utilise la taille passée en paramètre
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        )
    }
}


// --- Exemple d'utilisation et Preview ---

@Preview(showBackground = true)
@Composable
fun RangeSliderWithLabelsPreview() {
    MaterialTheme {
        var selectedPriceRange by remember {
            mutableStateOf(0f..100f) // Initialisé à toute la plage
        }
        val possiblePriceRange = 0f..100f
        val priceSteps = (possiblePriceRange.endInclusive.toInt() - possiblePriceRange.start.toInt()) - 1


        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sélectionnez une plage de prix (€)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            MyRangeSlider(
                selectedRange = selectedPriceRange,
                onRangeChange = { newRange -> selectedPriceRange = newRange },
                limitInterval = possiblePriceRange,
                steps = priceSteps, // Slider avec pas de 1€
                valueFormatter = { "€${it.roundToInt()}" } // Formateur pour afficher le symbole €
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Sélectionnez une note (continu)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            var selectedRatingRange by remember { mutableStateOf(0.5f..4.5f) }
            val possibleRatingRange = 0.0f..5.0f

            MyRangeSlider(
                selectedRange = selectedRatingRange,
                onRangeChange = { newRange -> selectedRatingRange = newRange },
                limitInterval = possibleRatingRange,
                steps = null,
                valueFormatter = { String.format("%.1f", it) }
            )
        }
    }
}