package grapes.microservices.views.screens.home

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.ui.theme.White
import grapes.microservices.viewmodels.home.HomeViewModel
import grapes.microservices.views.components.MySearchBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterSearchLayer(
    modifier: Modifier,
    currentFilters: ArticleFilter?,
    viewModel: HomeViewModel
) {
    val isBottomSheetOpen = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val filterSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,)

    if (currentFilters == null) return

    MyFilterSettingsBottomSheet(
        sheetState = filterSettingsSheetState,
        isSheetOpen = isBottomSheetOpen,
        initialFilters = currentFilters,
        onValueChange = { filters ->
            viewModel.updateFilters(filters)
        }
    )

    Row(
        modifier = modifier
    ) {
        MySearchBar(modifier = Modifier.weight(1f), query = currentFilters.query,
            onValueChange = {
                newQuery -> viewModel.updateFilters(currentFilters.copy(query = newQuery))
            })

        Spacer(modifier = Modifier.width(20.dp))

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
                painter = painterResource(id = R.drawable.settings), // Ton image de profil
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(26.dp),
                colorFilter = ColorFilter.tint(White)
            )
        }
    }
}