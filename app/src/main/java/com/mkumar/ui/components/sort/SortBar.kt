@file:OptIn(ExperimentalMaterial3Api::class)

package com.mkumar.ui.components.sort

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp


@Composable
fun SortBar(
    title: String,
    modifier: Modifier = Modifier,
    sortField: String,
    sortOrderAsc: Boolean,
    onSortFieldChange: (String) -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        if (expanded) 180f else 0f,
        label = "arrowRotate"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {

        // TOP ROW: TITLE + SORT SUMMARY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Larger Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,     // UPGRADED
                color = MaterialTheme.colorScheme.primary
            )

            // Larger Sort Summary
            Row(verticalAlignment = Alignment.CenterVertically) {

                val orderSymbol = if (sortOrderAsc) "↑" else "↓"

                Text(
                    text = "Sort: $sortField $orderSymbol",
                    style = MaterialTheme.typography.titleMedium,   // UPGRADED
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)                                 // UPGRADED
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        // EXPANDED PANEL
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {

                // SORT BY
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val fields = listOf("Invoice", "UpdatedAt", "Name")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fields.forEach { field ->
                        FilterChip(
                            selected = sortField == field,
                            onClick = {
                                onSortFieldChange(field)
                                expanded = false
                            },
                            label = { Text(field) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ORDER
                Text(
                    text = "Order",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FilterChip(
                        selected = sortOrderAsc,
                        onClick = {
                            onSortOrderChange(true)
                            expanded = false
                        },
                        label = { Text("Asc") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.ArrowUpward, contentDescription = null)
                        }
                    )

                    FilterChip(
                        selected = !sortOrderAsc,
                        onClick = {
                            onSortOrderChange(false)
                            expanded = false
                        },
                        label = { Text("Desc") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.ArrowDownward, contentDescription = null)
                        }
                    )
                }
            }
        }

        // HORIZONTAL DIVIDER BEFORE CONTENT
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp
        )

    }
}
