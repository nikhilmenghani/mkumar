package com.mkumar.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductType

/** Simple chip model for the Product row */
//data class ProductChip(val label: String, val isSaved: Boolean = false)

/** Strongly typed overflow action to avoid type inference issues */
data class MenuAction(
    val title: String,
    val onClick: (CustomerFormState) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListCard2(
    customer: CustomerFormState,
    productChips: List<ProductEntry> = emptyList(),
    modifier: Modifier = Modifier,
    onClick: (CustomerFormState) -> Unit = {},
    onSync: (CustomerFormState) -> Unit = {},
    onGenerateBill: (CustomerFormState) -> Unit = {},
    onCall: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {},
    onArchive: (CustomerFormState) -> Unit = {},
    onDelete: (CustomerFormState) -> Unit = {},
    overflowActions: List<MenuAction> = listOf(
        MenuAction("Edit", onEdit),
        MenuAction("Share") { /* TODO */ },
        MenuAction("Duplicate") { /* TODO */ },
        MenuAction("Delete", onDelete),
    ),
    maxVisibleChips: Int = 5,
    mergeDuplicateProducts: Boolean = true
) {
    // Optional de-duplication of product chips
    val normalizedChips = remember(productChips, mergeDuplicateProducts) {
        if (!mergeDuplicateProducts) productChips
        else productChips
            .groupBy { it.productType }
            .map { (k, v) -> ProductEntry(productType = k, isSaved = v.any { it.isSaved }) }
    }

    val haptics = LocalHapticFeedback.current

    // No confirmValueChange; react via LaunchedEffect when state lands
    val swipeState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f }
    )

    LaunchedEffect(swipeState.currentValue) {
        when (swipeState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> { // swipe right → Call
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onCall(customer)
                swipeState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.EndToStart -> { // swipe left → Archive (safe)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onArchive(customer)
                swipeState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            else -> Unit
        }
    }

    // Clip the whole swipe container to the card shape so background matches rounded corners
    val cardShape = MaterialTheme.shapes.extraLarge

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = { SwipeBackgroundPreview(swipeState, cardShape) },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        modifier = modifier.clip(cardShape)
    ) {
        Card(
            onClick = { onClick(customer) },
            shape = cardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 92.dp) // <-- moved min height to the Card
                .semantics { contentDescription = "Customer ${customer.name}" }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp) // compact
            ) {
                // Header: avatar + name | Sync + Overflow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InitialsAvatarCompact(name = customer.name.ifBlank { "Customer" })
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = customer.name.ifBlank { "Unnamed customer" },
                        // Bigger name per request
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onSync(customer) }) {
                        // Replaced Upload with Sync icon
                        Icon(Icons.Outlined.Sync, contentDescription = "Sync to GitHub")
                    }
                    OverflowMenu(items = overflowActions, customer = customer)
                }

                // Phone row — replace icon with text label "Phone"
                if (customer.phone.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
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

                // Products: bag icon + text chips
                if (normalizedChips.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = "Products",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))

                        val overflowCount = (normalizedChips.size - maxVisibleChips).coerceAtLeast(0)
                        val visible = if (overflowCount > 0) normalizedChips.take(maxVisibleChips) else normalizedChips

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            items(visible, key = { it.productType.label }) { chip ->
                                AssistChip(

                                    onClick = { /* open product details */ },
                                    label = {
                                        Text(chip.productType.label)
                                    },
                                    leadingIcon = if (chip.isSaved) {
                                        { Icon(Icons.Outlined.Check, contentDescription = "Saved", modifier = Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                            if (overflowCount > 0) {
                                item(key = "overflow") {
                                    AssistChip(
                                        onClick = { /* open full product list */ },
                                        label = { Text("+${overflowCount}") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Divider + Generate Bill CTA
                Spacer(Modifier.height(8.dp))
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = { onGenerateBill(customer) },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Generate Bill")
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Bill")
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeBackgroundPreview(
    swipeState: SwipeToDismissBoxState,
    shape: androidx.compose.ui.graphics.Shape
) {
    val target = swipeState.targetValue
    val bgColor = when (target) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiaryContainer
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val label = when (target) {
        SwipeToDismissBoxValue.StartToEnd -> "Call"
        SwipeToDismissBoxValue.EndToStart -> "Archive"
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .clip(shape) // ensure rounded corners on background too
            .background(bgColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (target) {
            SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
            SwipeToDismissBoxValue.EndToStart -> Arrangement.End
            else -> Arrangement.SpaceBetween
        }
    ) {
        if (target == SwipeToDismissBoxValue.StartToEnd) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        } else if (target == SwipeToDismissBoxValue.EndToStart) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.Archive, contentDescription = null)
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
            .size(32.dp)
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
        ProductEntry(productType = ProductType.Lens),
        ProductEntry(productType = ProductType.Frame),
        ProductEntry(productType = ProductType.ContactLens)
    )

    Column(Modifier.padding(16.dp)) {
        CustomerListCard2(
            customer = customer,
            productChips = chips,
            onClick = {},
            onSync = {},
            onGenerateBill = {},
            onCall = {},
            onEdit = {},
            onArchive = {},
            onDelete = {},
            maxVisibleChips = 5
        )
    }
}
