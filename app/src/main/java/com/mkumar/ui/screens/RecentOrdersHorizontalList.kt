//Safe to delete
//package com.mkumar.ui.screens
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.Delete
//import androidx.compose.material.icons.outlined.PictureAsPdf
//import androidx.compose.material.icons.outlined.Share
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.mkumar.model.OrderWithCustomerInfo
//import com.mkumar.ui.components.LongPressMenuAnchor
//import com.mkumar.ui.components.ProMenuItem
//
//@Composable
//fun RecentOrdersHorizontalList(
//    orders: List<OrderWithCustomerInfo>,
//    onOrderClick: (String, String) -> Unit
//) {
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        contentPadding = PaddingValues(horizontal = 4.dp)
//    ) {
//        items(orders) { order ->
//            RecentOrderHorizontalCard(
//                order,
//                onOpen = {
//                    onOrderClick(order.id, order.customerId)
//                },
//                onInvoice = {
//                    onOrderClick(order.id, order.customerId)
//                },
//                onShare = {
//                    onOrderClick(order.id, order.customerId)
//                },
//                onDelete = {
//                    onOrderClick(order.id, order.customerId)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun RecentOrderHorizontalCard(
//    order: OrderWithCustomerInfo,
//    onOpen: () -> Unit,
//    onInvoice: () -> Unit,
//    onShare: () -> Unit,
//    onDelete: () -> Unit
//) {
//    LongPressMenuAnchor(
//        onClick = onOpen,
//        menuItems = listOf(
//            ProMenuItem(
//                title = "Invoice",
//                supportingText = "Generate or view PDF",
//                icon = Icons.Outlined.PictureAsPdf,
//                onClick = { onInvoice() }
//            ),
//            ProMenuItem(
//                title = "Share",
//                supportingText = "Share order",
//                icon = Icons.Outlined.Share,
//                startNewGroup = true,
//                onClick = { onShare() }
//            ),
//            ProMenuItem(
//                title = "Delete",
//                supportingText = "Remove order",
//                icon = Icons.Outlined.Delete,
//                destructive = true,
//                startNewGroup = true,
//                onClick = { onDelete() }
//            )
//        )
//    ) {
//        Surface(
//            tonalElevation = 2.dp,
//            shape = MaterialTheme.shapes.large,
//            modifier = Modifier
//                .width(260.dp)
//                .height(160.dp)
//        ) {
//            Column(Modifier.padding(16.dp)) {
//                Text(order.customerName, style = MaterialTheme.typography.titleMedium)
//                Text(order.customerPhone, style = MaterialTheme.typography.bodySmall)
//
//                Spacer(Modifier.height(6.dp))
//
//                Text(
//                    text = "Invoice: ${order.invoiceNumber}",
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.primary
//                )
//
//                Spacer(Modifier.weight(1f))
//
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                    Text("₹${order.totalAmount}", style = MaterialTheme.typography.titleMedium)
//                    Text(
//                        "₹${order.remainingBalance}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
//        }
//    }
//}
//
//
