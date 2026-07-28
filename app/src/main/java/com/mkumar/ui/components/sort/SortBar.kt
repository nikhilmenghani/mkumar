@file:OptIn(ExperimentalMaterial3Api::class)

package com.mkumar.ui.components.sort

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SortBar(
    title: String,
    modifier: Modifier = Modifier,
    sortField: String,
    sortOrderAsc: Boolean,
    onSortFieldChange: (String) -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
    paymentDueOnly: Boolean = false,
    onPaymentDueOnlyChange: (Boolean) -> Unit = {},
    sortFields: List<String> = listOf("Invoice", "UpdatedAt", "Name"),
    action: (@Composable () -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "sortExpansion"
    )
    val fieldLabel = when (sortField) {
        "UpdatedAt" -> "Updated"
        else -> sortField
    }
    val summary = buildString {
        append(fieldLabel)
        append(if (sortOrderAsc) " · Asc" else " · Desc")
        if (paymentDueOnly) append(" · Due")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = (if (action == null) {
                    Modifier
                } else {
                    Modifier.weight(1f)
                })
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (action == null) {
                    Arrangement.End
                } else {
                    Arrangement.Center
                }
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = if (expanded) "Hide sorting controls" else "Show sorting controls",
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (action != null) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    action()
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sort and filter",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sortFields.forEach { field ->
                            FilterChip(
                                selected = sortField == field,
                                onClick = { onSortFieldChange(field) },
                                label = {
                                    Text(if (field == "UpdatedAt") "Updated" else field)
                                }
                            )
                        }
                        FilterChip(
                            selected = sortOrderAsc,
                            onClick = { onSortOrderChange(true) },
                            label = { Text("Asc") },
                            leadingIcon = {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = null)
                            }
                        )
                        FilterChip(
                            selected = !sortOrderAsc,
                            onClick = { onSortOrderChange(false) },
                            label = { Text("Desc") },
                            leadingIcon = {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = null)
                            }
                        )
                        FilterChip(
                            selected = paymentDueOnly,
                            onClick = { onPaymentDueOnlyChange(!paymentDueOnly) },
                            label = { Text("Payment due") }
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    }
}
