@file:OptIn(ExperimentalFoundationApi::class)

package com.mkumar.ui.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.mkumar.data.CustomerFormState
import com.mkumar.ui.theme.AppColors

data class MenuAction(
    val title: String,
    val onClick: (CustomerFormState) -> Unit
)

@Composable
fun CustomerListCard2(
    customer: CustomerFormState,
    modifier: Modifier = Modifier,
    onClick: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {},
    onDelete: (CustomerFormState) -> Unit = {},
    extraActions: List<MenuAction> = emptyList(),
    showPhoneRow: Boolean = true
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val actions = buildList {
        add(MenuAction("Edit", onEdit))
        addAll(extraActions)
        add(MenuAction("Delete", onDelete))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = AppColors.outlinedCardColors(),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Customer ${customer.name}" }
            .combinedClickable(
                onClick = { onClick(customer) },
                onLongClick = { menuExpanded = true }   // <— long-press opens menu
            )
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                InitialsAvatarCompact(name = customer.name.ifBlank { "Customer" })
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = customer.name.ifBlank { "Unnamed customer" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customer.phone.isNotBlank() && showPhoneRow) {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Phone",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                ProOverflowMenu(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    items = buildList {
                        add(
                            ProMenuItem(
                                title = "Edit",
                                supportingText = "Update name or phone",
                                icon = Icons.Outlined.Edit, // import
                                onClick = { onEdit(customer) }
                            )
                        )
                        // Future actions can go here (share, duplicate, etc.)
                        add(
                            ProMenuItem(
                                title = "Delete",
                                supportingText = "Remove this customer",
                                icon = Icons.Outlined.Delete, // import
                                destructive = true,
                                startNewGroup = true,
                                onClick = { onDelete(customer) }
                            )
                        )
                    },
                    anchor = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatarCompact(name: String) {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val initials = when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "CU"
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ProOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<ProMenuItem>,           // see data class below
    anchor: @Composable () -> Unit      // the anchor (usually the ⋮ IconButton)
) {
    Box {
        // Anchor
        anchor()

        // Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            properties = PopupProperties(clippingEnabled = true),
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

                val isDestructive = item.destructive
                val colors = if (isDestructive) {
                    MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    MenuDefaults.itemColors()
                }

                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                    leadingIcon = item.icon?.let { ic ->
                        { Icon(ic, contentDescription = null) }
                    },
                    trailingIcon = item.trailing?.let { tr ->
                        { tr() }
                    },
                    onClick = {
                        onExpandedChange(false)
                        item.onClick()
                    },
                    colors = colors,
                    modifier = Modifier.heightIn(min = 48.dp) // bigger touch target
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

/* ------------------------------- PREVIEWS -------------------------------- */

@Preview(name = "CustomerListCard2 – Light", showBackground = true)
@Composable
private fun PreviewCustomerListCard2_Light() {
    MaterialTheme { Surface { PreviewContent() } }
}

@Preview(name = "CustomerListCard2 – Dark", showBackground = true)
@Composable
private fun PreviewCustomerListCard2_Dark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface { PreviewContent() }
    }
}

@Composable
private fun PreviewContent() {
    val customer = CustomerFormState(
        id = "12345",
        name = "Courteney White",
        phone = "(123) 456-7890"
    )

    Column(Modifier.padding(16.dp)) {
        CustomerListCard2(
            customer = customer,
            onClick = {},
            onDelete = {},
            extraActions = emptyList()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewProOverflowMenu() {
    var expanded by remember { mutableStateOf(true) }
    MaterialTheme {
        Box(Modifier.padding(32.dp)) {
            ProOverflowMenu(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                items = listOf(
                    ProMenuItem(
                        title = "Edit",
                        supportingText = "Edit this item",
                        icon = Icons.Outlined.Edit,
                        onClick = {}
                    ),
                    ProMenuItem(
                        title = "Delete",
                        supportingText = "Delete this item",
                        icon = Icons.Outlined.Delete,
                        destructive = true,
                        startNewGroup = true,
                        onClick = {}
                    )
                ),
                anchor = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Show menu")
                    }
                }
            )
        }
    }
}
