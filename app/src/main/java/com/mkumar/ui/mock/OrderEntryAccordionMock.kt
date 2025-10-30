package com.mkumar.ui.mock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryAccordionMockScaffold(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Entry (Accordion Mock)") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // <- respects status bar & app bar height
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "Arunbhai", onValueChange = {},
                            label = { Text("Customer") },
                            readOnly = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "02/03/2025", onValueChange = {},
                            label = { Text("Date") },
                            readOnly = true, modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "021314", onValueChange = {},
                            label = { Text("Order No") },
                            readOnly = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "7283840499", onValueChange = {},
                            label = { Text("Mobile") },
                            readOnly = true, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Accordion items (mock)
            val mockItems = listOf(
                "Progressive HC" to "GLASS",
                "Thunder TR" to "FRAME",
                "Thunder 200RS" to "FRAME"
            )
            mockItems.forEachIndexed { idx, (title, type) ->
                ExpandableItemCard(
                    index = idx + 1,
                    title = title,
                    type = type,
                    rate = (1000 + idx * 300).toString(),
                    amount = (1000 + idx * 300).toString()
                )
            }

            // Add Item
            OutlinedButton(onClick = { /* no-op (mock) */ }, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Item")
            }

            // Totals
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
private fun ExpandableItemCard(
    index: Int,
    title: String,
    type: String,
    rate: String,
    amount: String
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Collapsed summary row
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$index.", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(28.dp))
                Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Text(type, modifier = Modifier.width(84.dp), textAlign = TextAlign.End)
                Spacer(Modifier.width(16.dp))
                Text("₹$amount", modifier = Modifier.width(92.dp), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Expanded form
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut()
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(title, {}, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(type, {}, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField("1", {}, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(rate, {}, label = { Text("Rate (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(amount, {}, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField("", {}, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { expanded = false }) { Text("Cancel") }
                        Button(onClick = { expanded = false }) { Text("Save Item") }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun OrderEntryAccordionMockScaffoldPreview() {
    MaterialTheme {
        OrderEntryAccordionMockScaffold()
    }
}
