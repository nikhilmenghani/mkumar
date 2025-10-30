@file:Suppress("UnusedImport")

package com.mkumar.ui.mock

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp

@Composable
fun OrderEntryGridMock(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text("Order Entry", style = MaterialTheme.typography.headlineSmall)
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "Arunbhai",
                            onValueChange = {},
                            label = { Text("Customer") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "02/03/2025",
                            onValueChange = {},
                            label = { Text("Date") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "021314",
                            onValueChange = {},
                            label = { Text("Order No") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "7283840499",
                            onValueChange = {},
                            label = { Text("Mobile") },
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Grid Header
            ElevatedCard {
                Column {
                    // table header
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", Modifier.weight(.5f), fontWeight = FontWeight.SemiBold)
                        Text("Description", Modifier.weight(3f), fontWeight = FontWeight.SemiBold)
                        Text("Type", Modifier.weight(1.5f), fontWeight = FontWeight.SemiBold)
                        Text("Qty", Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        Text("Rate", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        Text("Amount", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(36.dp)) // actions column
                    }
                    Divider()

                    // --- Row 1
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1", Modifier.weight(.5f))
                        OutlinedTextField(
                            value = "PROGRESSIVE HC",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(3f)
                        )
                        OutlinedTextField(
                            value = "GLASS",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = "1",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "1,250",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text("1,250", Modifier.weight(1.5f), textAlign = TextAlign.End)
                        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }

                    // --- Row 2
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2", Modifier.weight(.5f))
                        OutlinedTextField(
                            value = "THUNDER TR",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(3f)
                        )
                        OutlinedTextField(
                            value = "FRAME",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(value = "1", onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = "300", onValueChange = {}, readOnly = true, modifier = Modifier.weight(1.5f))
                        Text("300", Modifier.weight(1.5f), textAlign = TextAlign.End)
                        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }

                    // --- Row 3
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3", Modifier.weight(.5f))
                        OutlinedTextField(
                            value = "THUNDER 200RS.",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(3f)
                        )
                        OutlinedTextField(value = "FRAME", onValueChange = {}, readOnly = true, modifier = Modifier.weight(1.5f))
                        OutlinedTextField(value = "1", onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = "150", onValueChange = {}, readOnly = true, modifier = Modifier.weight(1.5f))
                        Text("150", Modifier.weight(1.5f), textAlign = TextAlign.End)
                        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }

                    // --- Add Item (visual only)
                    Divider()
                    TextButton(
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Item")
                    }
                }
            }

            // Totals block
            ElevatedCard {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Subtotal / Discount / Other
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = "1,700",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subtotal (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = " - ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Discount (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = " - ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Other (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Grand total / Advance / Balance
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = "1,850",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grand Total (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "500",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Advance (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "1,350",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Balance (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Footer actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {}) { Text("Save") }
                Button(onClick = {}) { Text("Generate PDF") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun OrderEntryGridMockPreview() {
    MaterialTheme { OrderEntryGridMock() }
}
