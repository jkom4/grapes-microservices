package grapes.microservices.views.components.OrderComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.MaterialTheme
import grapes.microservices.models.data.Order

@Composable
fun OrderHistoryContent(
    orders: List<Order>,
    isLoading: Boolean,
    error: String?,
    currentPage: Int,
    hasMorePages: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                top = paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
            )
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OrderListContent(
            orders = orders,
            isLoading = isLoading,
            error = error,
            modifier = Modifier.weight(1f)
        )
        PaginationControls(
            currentPage = currentPage,
            hasMorePages = hasMorePages,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage
        )
    }
}