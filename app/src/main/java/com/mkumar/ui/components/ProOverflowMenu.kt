package com.mkumar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun ProOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<ProMenuItem>,
    // The anchor renders your content. The DropdownMenu will be positioned
    // relative to this same Box (no menuAnchor needed).
    anchor: @Composable () -> Unit,
    menuOffset: DpOffset = DpOffset.Zero
) {
    Box {
        // 1) Render the anchor content (your card’s inner content)
        anchor()

        // 2) Render the menu in the SAME Box so offset is in the same space
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            offset = menuOffset,
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            properties = PopupProperties(clippingEnabled = true, focusable = true),
            modifier = Modifier
                .shadow(8.dp, shape = MaterialTheme.shapes.extraLarge)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface)
                .widthIn(min = 220.dp)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0 && item.startNewGroup) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                val colors = if (item.destructive) {
                    MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    MenuDefaults.itemColors()
                }

                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1
                            )
                            item.supportingText?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    leadingIcon = item.icon?.let { ic -> { Icon(ic, contentDescription = null) } },
                    trailingIcon = item.trailing?.let { tr -> { tr() } },
                    onClick = {
                        onExpandedChange(false)
                        item.onClick()
                    },
                    colors = colors,
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
    }
}

data class ProMenuItem(
    val title: String,
    val supportingText: String? = null,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
    val startNewGroup: Boolean = false,
    val trailing: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit
)
