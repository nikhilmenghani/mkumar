package com.mkumar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.components.LongPressMenuAnchor
import com.mkumar.ui.components.ProMenuItem
import com.mkumar.ui.components.cards.InitialsAvatarCompact

@Composable
fun RecentCustomersList(
    customers: List<UiCustomerMini>,
    onCustomerClick: (UiCustomerMini) -> Unit,
    onDelete: (UiCustomerMini) -> Unit = {},
    onEdit: (UiCustomerMini) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(customers, key = { it.id }) { customer ->
                RecentCustomerCard(
                    customer,
                    onClick = { onCustomerClick(customer) },
                    onEdit = { onEdit(customer) },
                    onDelete = { onDelete(customer) }
                )
            }
        }
    }
}

@Composable
fun RecentCustomerCard(
    customer: UiCustomerMini,
    onClick: () -> Unit,
    onEdit: (UiCustomerMini) -> Unit,
    onDelete: (UiCustomerMini) -> Unit
) {
    LongPressMenuAnchor(
        onClick = onClick,
        menuItems = listOf(
            ProMenuItem(
                title = "Edit",
                supportingText = "Update name or phone",
                icon = Icons.Outlined.Edit,
                onClick = { onEdit(customer) }
            ),
            ProMenuItem(
                title = "Delete",
                supportingText = "Remove customer",
                icon = Icons.Outlined.Delete,
                destructive = true,
                startNewGroup = true,
                onClick = { onDelete(customer) }
            )
        )
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                InitialsAvatarCompact(name = customer.name.ifBlank { "Customer" })
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Customer details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Phone",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
