package grapes.microservices.views.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.models.data.Article
import grapes.microservices.ui.theme.White
import grapes.microservices.viewmodels.HomeViewModel
import grapes.microservices.views.components.MySearchBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterSearchLayer(
    modifier: Modifier,
    query: String,
    onSearchStarted: () -> Unit,
    onSearchResults: (List<Article>) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val isBottomSheetOpen = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val filterSettingsSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // Bottom sheet to manage filter options
    MyFilterSettingsBottomSheet(
        sheetState = filterSettingsSheetState,
        isSheetOpen = isBottomSheetOpen
    )

    Row(modifier = modifier) {
        val viewmodel = koinViewModel<HomeViewModel>()
        val currentSettings by viewmodel.filterSettings.collectAsState()

        // Search bar with callbacks
        MySearchBar(
            modifier = Modifier.weight(1f),
            query = query,
            onQueryChanged = { onQueryChanged(it) },
            onSearchStarted = { onSearchStarted() },
            onResults = { response ->
                onSearchResults(response)
            }
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Settings icon to open filter sheet
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    scope.launch { isBottomSheetOpen.value = true }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "Filter Settings Icon",
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(White)
            )
        }
    }
}