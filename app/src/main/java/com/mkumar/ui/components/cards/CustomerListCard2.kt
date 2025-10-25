package com.mkumar.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductType
import com.mkumar.ui.theme.AppColors

/** Strongly typed overflow action */
data class MenuAction(
    val title: String,
    val onClick: (CustomerFormState) -> Unit
)

@Composable
fun CustomerListCard2(
    customer: CustomerFormState,
    modifier: Modifier = Modifier,
    onClick: (CustomerFormState) -> Unit = {},
    onSync: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {},
    onDelete: (CustomerFormState) -> Unit = {},
    overflowActions: List<MenuAction> = listOf(
//        MenuAction("Edit", onEdit),
//        MenuAction("Share") { /* TODO */ },
//        MenuAction("Duplicate") { /* TODO */ },
        MenuAction("Delete", onDelete),
    ),
    showPhoneRow: Boolean = true
) {
    Card(
        onClick = { onClick(customer) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = AppColors.outlinedCardColors(),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Customer ${customer.name}" }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: avatar + name | Sync + Overflow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                InitialsAvatarCompact(name = customer.name.ifBlank { "Customer" })
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
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
//                IconButton(onClick = { onSync(customer) }) {
//                    Icon(Icons.Outlined.Sync, contentDescription = "Sync to GitHub")
//                }
                OverflowMenu(items = overflowActions, customer = customer)
            }
        }
    }
}

@Composable
private fun OverflowMenu(
    items: List<MenuAction>,
    customer: CustomerFormState
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.title) },
                    onClick = {
                        expanded = false
                        action.onClick(customer)
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

/* ------------------------------- PREVIEWS -------------------------------- */

@Preview(name = "CustomerListCard2 – Light", showBackground = true)
@Composable
private fun PreviewCustomerListCard2_Light() {
    MaterialTheme {
        Surface { PreviewContent() }
    }
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
    val chips = listOf(
        ProductEntry(productType = ProductType.Lens, isSaved = true),
        ProductEntry(productType = ProductType.Frame),
        ProductEntry(productType = ProductType.ContactLens)
    )

    Column(Modifier.padding(16.dp)) {
        CustomerListCard2(
            customer = customer,
            onClick = {},
            onSync = {},
            onEdit = {},
            onDelete = {},
        )
    }
}
