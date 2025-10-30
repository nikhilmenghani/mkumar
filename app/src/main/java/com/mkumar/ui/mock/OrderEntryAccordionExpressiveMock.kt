package com.mkumar.ui.mock

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryAccordionExpressiveMock(modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Order Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 30.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Customer Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "Arunbhai",
                            onValueChange = {},
                            label = { Text("Customer Name") },
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
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "021314",
                            onValueChange = {},
                            label = { Text("Order ID") },
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

            // Accordion Items
            val items = listOf(
                "Progressive HC" to "GLASS",
                "Thunder TR" to "FRAME",
                "Thunder 200RS" to "FRAME"
            )
            items.forEachIndexed { index, (title, type) ->
                ExpressiveAccordionItem(
                    index = index + 1,
                    title = title,
                    type = type,
                    rate = (1000 + index * 250).toString(),
                    amount = (1000 + index * 250).toString()
                )
            }

            // Add Item Button
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Item", style = MaterialTheme.typography.labelLarge)
            }

            // Totals Section
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("1,700", {}, readOnly = true, label = { Text("Subtotal") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("-", {}, readOnly = true, label = { Text("Discount") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("-", {}, readOnly = true, label = { Text("Other Charges") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField("1,850", {}, readOnly = true, label = { Text("Grand Total") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("500", {}, readOnly = true, label = { Text("Advance") }, modifier = Modifier.weight(1f))
                        OutlinedTextField("1,350", {}, readOnly = true, label = { Text("Balance") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Footer Actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Save Draft") }

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Generate PDF") }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ExpressiveAccordionItem(
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
            .animateContentSize()
            .shadow(if (expanded) 6.dp else 2.dp, RoundedCornerShape(24.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (expanded)
                MaterialTheme.colorScheme.surfaceContainerHighest
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (expanded) 6.dp else 6.dp
        )
    ) {
        Column {
            // Collapsed header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = type,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "₹$amount",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(84.dp),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded form
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(title, {}, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(type, {}, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField("1", {}, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(rate, {}, label = { Text("Rate") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(amount, {}, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
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
private fun OrderEntryAccordionExpressiveMockPreview() {
    MaterialTheme {
        OrderEntryAccordionExpressiveMock()
    }
}
