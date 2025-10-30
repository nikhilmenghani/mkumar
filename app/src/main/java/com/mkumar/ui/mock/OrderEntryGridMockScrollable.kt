@file:Suppress("UnusedImport")

package com.mkumar.ui.mock

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OrderEntryGridMockScrollable(modifier: Modifier = Modifier) {
    // Fixed column widths to keep rows aligned while horizontally scrolling.
    val cNo: Dp = 56.dp
    val cDesc: Dp = 420.dp
    val cType: Dp = 140.dp
    val cQty: Dp = 80.dp
    val cRate: Dp = 120.dp
    val cAmt: Dp = 120.dp
    val cAct: Dp = 64.dp
    val tableWidth = cNo + cDesc + cType + cQty + cRate + cAmt + cAct

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Surface(modifier.fillMaxSize()) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(vScroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Order Entry (Scrollable Grid)", style = MaterialTheme.typography.headlineSmall)

            // Header card (customer basics)
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("Arunbhai", {}, readOnly = true, label = { Text("Customer") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("02/03/2025", {}, readOnly = true, label = { Text("Date") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("021314", {}, readOnly = true, label = { Text("Order No") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("7283840499", {}, readOnly = true, label = { Text("Mobile") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // GRID (horizontally scrollable)
            ElevatedCard {
                Column(
                    Modifier
                        .horizontalScroll(hScroll) // <-- horizontal scroll container
                        .width(tableWidth)
                ) {
                    // Header row
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderCell("#", cNo)
                        HeaderCell("Description", cDesc)
                        HeaderCell("Type", cType)
                        HeaderCell("Qty", cQty, end = true)
                        HeaderCell("Rate", cRate, end = true)
                        HeaderCell("Amount", cAmt, end = true)
                        Spacer(Modifier.width(cAct))
                    }
                    Divider()

                    // --- Row 1
                    TableRow(
                        index = "1",
                        desc = "PROGRESSIVE HC",
                        type = "GLASS",
                        qty = "1",
                        rate = "1,250",
                        amount = "1,250",
                        widths = arrayOf(cNo, cDesc, cType, cQty, cRate, cAmt, cAct)
                    )
                    // --- Row 2
                    TableRow(
                        index = "2",
                        desc = "THUNDER TR",
                        type = "FRAME",
                        qty = "1",
                        rate = "300",
                        amount = "300",
                        widths = arrayOf(cNo, cDesc, cType, cQty, cRate, cAmt, cAct)
                    )
                    // --- Row 3
                    TableRow(
                        index = "3",
                        desc = "THUNDER 200RS.",
                        type = "FRAME",
                        qty = "1",
                        rate = "150",
                        amount = "150",
                        widths = arrayOf(cNo, cDesc, cType, cQty, cRate, cAmt, cAct)
                    )

                    Divider()
                    TextButton(
                        onClick = { /* no-op in mock */ },
                        modifier = Modifier
                            .padding(8.dp)
                            .width(160.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Item")
                    }
                }
            }

            // Totals (stays normal width; only the table scrolls)
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("1,700", {}, readOnly = true, label = { Text("Subtotal (₹)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("-", {}, readOnly = true, label = { Text("Discount (₹)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("-", {}, readOnly = true, label = { Text("Other (₹)") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("1,850", {}, readOnly = true, label = { Text("Grand Total (₹)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("500", {}, readOnly = true, label = { Text("Advance (₹)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("1,350", {}, readOnly = true, label = { Text("Balance (₹)") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { }) { Text("Save") }
                Button(onClick = { }) { Text("Generate PDF") }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, end: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 12.dp),
        fontWeight = FontWeight.SemiBold,
        textAlign = if (end) TextAlign.End else TextAlign.Start
    )
}

@Composable
private fun TableRow(
    index: String,
    desc: String,
    type: String,
    qty: String,
    rate: String,
    amount: String,
    widths: Array<Dp>
) {
    val cNo = widths[0]
    val cDesc = widths[1]
    val cType = widths[2]
    val cQty = widths[3]
    val cRate = widths[4]
    val cAmt = widths[5]
    val cAct = widths[6]

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(index, Modifier.width(cNo).padding(horizontal = 12.dp))
        OutlinedTextField(desc, {}, readOnly = true, modifier = Modifier.width(cDesc))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(type, {}, readOnly = true, modifier = Modifier.width(cType))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(qty, {}, readOnly = true, modifier = Modifier.width(cQty))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(rate, {}, readOnly = true, modifier = Modifier.width(cRate))
        Spacer(Modifier.width(8.dp))
        Text(amount, Modifier.width(cAmt).padding(horizontal = 12.dp), textAlign = TextAlign.End)
        Box(Modifier.width(cAct), contentAlignment = Alignment.Center) {
            IconButton(onClick = { /* no-op */ }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OrderEntryGridMockScrollablePreview() {
    MaterialTheme { OrderEntryGridMockScrollable() }
}
