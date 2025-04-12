package grapes.microservices.views.Home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import grapes.microservices.ui.theme.Gray90
import grapes.microservices.viewmodels.HomeViewModel
import grapes.microservices.views.components.ExpandableCard
import grapes.microservices.views.components.MyRangeSlider
import grapes.microservices.views.components.MySelectableTag
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterSettingsBottomSheet(
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>
) {
    val scope = rememberCoroutineScope()
    val space = 16.dp
    val scrollState = rememberScrollState()
    val viewmodel = koinViewModel<HomeViewModel>()

    if (isSheetOpen.value) {
        ModalBottomSheet(
            containerColor = Gray90,
            onDismissRequest = {
                scope.launch {
                    isSheetOpen.value = false
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                val currentSettings by viewmodel.filterSettings.collectAsState()

                Text("Filter settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = space))

                Spacer(Modifier.height(space))

                ExpandableCard(title = "Category" , space = space) {
                    MySelectableTag(viewmodel.getCategories(),
                        currentSettings.category,
                        onSelected = { item -> viewmodel.updateCategory(item) })
                }

                Spacer(Modifier.height(space))

                ExpandableCard(title = "Family", space = space) {
                    MySelectableTag(viewmodel.getFamilies(),
                        currentSettings.family,
                        onSelected = { item -> viewmodel.updateFamily(item) })
                }

                Spacer(Modifier.height(space))

                ExpandableCard(title = "Price", space = space) {
                    MyRangeSlider(
                        selectedRange = currentSettings.articleMinMaxPrice.currentInterval,
                        onRangeChange = { newRange -> viewmodel.updatePriceRange(newRange) },
                        limitInterval = currentSettings.articleMinMaxPrice.sliderLimit,
                        steps = null,
                        valueFormatter = { String.format("%.2f", it) }
                    )
                }

                Spacer(Modifier.height(space))

                ExpandableCard(title = "Rating", space = space) {
                    MyRangeSlider(
                        selectedRange = currentSettings.articleMinMaxRatting.currentInterval,
                        onRangeChange = { newRange -> viewmodel.updateRattingRange(newRange) },
                        limitInterval = currentSettings.articleMinMaxRatting.sliderLimit,
                        steps = 4,
                        valueFormatter = { String.format("%.0f", it) }
                    )

                }
            }
        }
    }
}

@Preview
@Composable
fun MyFilterSettingsBottomSheetPreview() {
    ExpandableCard(title = "Price", space = 16.dp) {
        var selectedRatingRange by remember { mutableStateOf(0.5f..4.5f) }
        val possibleRatingRange = 0.0f..5.0f

        MyRangeSlider(
            selectedRange = selectedRatingRange,
            onRangeChange = { newRange -> selectedRatingRange = newRange },
            limitInterval = possibleRatingRange,
            steps = null,
            valueFormatter = { String.format("%.2f", it) }
        )
    }
}
