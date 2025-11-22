package com.mkumar.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.formatAsDateTime
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.ui.components.LongPressMenuAnchor
import com.mkumar.ui.components.ProMenuItem
import com.mkumar.ui.components.ProOverflowMenuIcons

@Composable
fun RecentOrdersList(
    orders: List<OrderWithCustomerInfo>,
    onOrderClick: (orderId: String, customerId: String) -> Unit,
    onInvoiceClick: (orderId: String, invoiceNumber: Long) -> Unit,
    onShareClick: (orderId: String, invoiceNumber: Long) -> Unit,
    onDeleteClick: (orderId: String) -> Unit,
    onOpenCustomer: (customerId: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 12.dp) // FIX
        ) {
            items(orders) { order ->
                RecentOrderCardCompact(
                    order,
                    onOpen = {
                        onOrderClick(order.id, order.customerId)
                    },
                    onInvoice = {
                        onInvoiceClick(order.id, order.invoiceNumber)
                    },
                    onShare = {
                        onShareClick(order.id, order.invoiceNumber)
                    },
                    onDelete = {
                        onDeleteClick(order.id)
                    },
                    onOpenCustomer = {
                        onOpenCustomer(order.customerId)
                    }
                )
            }
        }
    }
}


@Composable
fun RecentOrderCard(
    order: OrderWithCustomerInfo,
    onOpen: () -> Unit,
    onInvoice: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    LongPressMenuAnchor(
        onClick = onOpen,
        menuItems = listOf(
            ProMenuItem(
                title = "Invoice",
                supportingText = "Generate or view PDF",
                icon = Icons.Outlined.PictureAsPdf,
                onClick = { onInvoice() }
            ),
            ProMenuItem(
                title = "Share",
                supportingText = "Share order details",
                icon = Icons.Outlined.Share,
                startNewGroup = true,
                onClick = { onShare() }
            ),
            ProMenuItem(
                title = "Delete",
                supportingText = "Remove order",
                icon = Icons.Outlined.Delete,
                destructive = true,
                startNewGroup = true,
                onClick = { onDelete() }
            )
        )
    ) {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(order.customerName, style = MaterialTheme.typography.titleMedium)
                Text(order.customerPhone, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Invoice: ${order.invoiceNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹${order.totalAmount}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Remaining: ₹${order.remainingBalance}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun RecentOrderCardCompact(
    order: OrderWithCustomerInfo,
    onOpen: () -> Unit,
    onInvoice: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onOpenCustomer : () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp) // ultra compact
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (menuExpanded) menuExpanded = false else onOpen()
                    },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = true
                    }
                )
            }
    ) {
        // Menu anchor at top-right
        Box(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            ProOverflowMenuIcons(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                items = listOf(
                    ProMenuItem(
                        title = "Invoice",
                        supportingText = "Generate or view PDF",
                        icon = Icons.Outlined.PictureAsPdf,
                        onClick = { onInvoice() }
                    ),
                    ProMenuItem(
                        title = "Share",
                        supportingText = "Share order details",
                        icon = Icons.Outlined.Share,
                        startNewGroup = true,
                        onClick = { onShare() }
                    ),
                    ProMenuItem(
                        title = "Open Customer",
                        supportingText = "Open the Customer details",
                        startNewGroup = true,
                        icon = Icons.Outlined.Person,
                        onClick = { onOpenCustomer() }
                    ),
                    ProMenuItem(
                        title = "Delete",
                        supportingText = "Remove order",
                        icon = Icons.Outlined.Delete,
                        destructive = true,
                        startNewGroup = true,
                        onClick = { onDelete() }
                    )
                ),
                anchor = { /* invisible anchor */ }
            )
        }

        // Actual card UI
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .matchParentSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {

                /* ────────────────────────────────
                   ROW 1 — Name   |   Created Date
                   ──────────────────────────────── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = order.createdAt.formatAsDateTime(), // your extension
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(4.dp))

                /* ────────────────────────────────
                   ROW 2 — Compact ledger strip
                   Left: Phone • Invoice
                   Right: ₹Total • Rem: ₹YYY
                   ──────────────────────────────── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Left inline info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (order.customerPhone.isNotBlank()) {
                            Text(
                                text = order.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text("•", style = MaterialTheme.typography.bodySmall)

                        Text(
                            text = "Invoice: ${order.invoiceNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right inline totals
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${order.totalAmount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )

                        if (order.remainingBalance > 0) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "Rem: ₹${order.remainingBalance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
