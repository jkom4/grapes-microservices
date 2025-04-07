package grapes.microservices.views.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.ui.theme.Gray90
import grapes.microservices.viewmodels.home.HomeViewModel
import grapes.microservices.views.components.ExpandableCard
import grapes.microservices.views.components.MyRangeSlider
import grapes.microservices.views.components.MySelectableTag
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterSettingsBottomSheet(
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    onValueChange: (filters: ArticleFilter) -> Unit,
    initialFilters: ArticleFilter,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val viewmodel = koinViewModel<HomeViewModel>()
    var editableFilters by remember(initialFilters) { mutableStateOf(initialFilters) }

    // If Filter page is not open, skip the filter page below
    if (!isSheetOpen.value) return

    ModalBottomSheet(
        containerColor = Gray90,
        onDismissRequest = {
            isSheetOpen.value = false
            onValueChange(editableFilters)
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            // Header + Reset button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)) {
                Text(
                    stringResource(R.string.filter_settings),
                    style = MaterialTheme.typography.titleLarge)

                IconButton(onClick = {
                    editableFilters = editableFilters.getReset()
                }, modifier = Modifier.size(35.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            ExpandableCard(title = stringResource(R.string.category), modifier = Modifier.padding(bottom = 8.dp)) {
                MySelectableTag(viewmodel.categories.collectAsState().value,
                    editableFilters.category,
                    onSelect = { item -> editableFilters = editableFilters.copy(category = item) })
            }

            ExpandableCard(title = stringResource(R.string.family), modifier = Modifier.padding(bottom = 8.dp)) {
                MySelectableTag(viewmodel.families.collectAsState().value,
                    editableFilters.family,
                    onSelect = { item -> editableFilters = editableFilters.copy(family = item) })
            }

            ExpandableCard(title = stringResource(R.string.price), modifier = Modifier.padding(bottom = 8.dp)) {
                val price = editableFilters.priceRange
                MyRangeSlider(
                    selectedRange = price.currentInterval,
                    onRangeChange = { newRange ->
                        val newPrice = price.copy(currentInterval = newRange)
                        editableFilters = editableFilters.copy(priceRange = newPrice) },
                    limitInterval = price.sliderLimit,
                    steps = null,
                    valueFormatter = { String.format("%.2f", it) }
                )
            }

            ExpandableCard(title = stringResource(R.string.rating), modifier = Modifier) {
                val rate = editableFilters.rattingRange
                MyRangeSlider(
                    selectedRange = rate.currentInterval,
                    onRangeChange = {
                        newRange -> val newRatting = rate.copy(currentInterval = newRange)
                        editableFilters = editableFilters.copy(rattingRange = newRatting) },
                    limitInterval = rate.sliderLimit,
                    steps = 4,
                    valueFormatter = { String.format("%.0f", it) }
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyFilterSettingsBottomSheetPreview() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()) {
        Text(
            stringResource(R.string.filter_settings),
            style = MaterialTheme.typography.titleLarge)

        IconButton(onClick = { }, modifier = Modifier.size(35.dp)) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
