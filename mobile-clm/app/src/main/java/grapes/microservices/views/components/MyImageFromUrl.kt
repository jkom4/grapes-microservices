package grapes.microservices.views.components

import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.layout.ContentScale
import coil3.request.error
import coil3.request.placeholder
import grapes.microservices.R

@Composable
fun MyImageFromUrl(
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Size = Size(200, 200)
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .size(size.width, size.height)
        .crossfade(true)
        .placeholder(R.drawable.default_image)
        .error(R.drawable.default_image)
        .build()

    AsyncImage(
        model = imageRequest,
        contentDescription = "article",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}