package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import grapes.microservices.ui.theme.White

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MySelectableTag(elements: List<String>,
                   selectedItem: String,
                   onSelected: (String) -> Unit) {
    FlowRow (
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .padding(7.dp)
    ) {
        // add "all" option (Empty String = all)
        val mutableList = elements.toMutableList()
        mutableList.add(0, "")

        mutableList.forEach { word ->
            Box(
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    .background(
                        if (word == selectedItem) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            White
                        },
                        CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // Handle tag selection (when select the already selected tag, unselect it)
                    .clickable {
                        if (selectedItem == word) {
                            onSelected("")
                        } else {
                            onSelected(word)
                        }
                    }
            ) {
                val text = if (word == "") "all" else word
                Text(
                    modifier = Modifier.clip(shape = CircleShape),
                    text = text,
                    color =
                    if (word == selectedItem) {
                        White
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                    ,
                    style = TextStyle(color = Color.White)
                )
            }
        }
    }
}