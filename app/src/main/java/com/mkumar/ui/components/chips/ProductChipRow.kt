package com.mkumar.ui.components.chips

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry

@Composable
fun ProductChipRow(
    products: List<ProductEntry>,
    selectedId: String?,
    onChipClick: (String) -> Unit,
    onChipDelete: (String) -> Unit,
    getCurrentBuffer: (ProductEntry) -> com.mkumar.data.ProductFormData?,
    hasUnsavedChanges: (ProductEntry) -> Boolean
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            products.forEach { product ->
                FilterChip(
                    selected = product.id == selectedId,
                    onClick = { onChipClick(product.id) },
                    label = {
                        Text("${product.type.label} #${products.indexOf(product) + 1}")
                    },
                    leadingIcon = {
                        when {
                            product.isSaved && !hasUnsavedChanges(product) ->
                                Icon(Icons.Default.Check, contentDescription = "Saved")
                            product.isSaved && hasUnsavedChanges(product) ->
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Unsaved changes",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            else -> null
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onChipDelete(product.id)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Product",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                )
            }
        }

        // Fading edge for visual hint
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(32.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}
