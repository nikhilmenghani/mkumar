package com.mkumar.ui.mock

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryAccordionExpressiveMockFixed(modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    val today = remember { LocalDate.now().toString() } // yyyy-MM-dd

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Order Entry", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ----- Customer Details (read-only, no Order ID, date defaults to today)
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Customer Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "Arunbhai",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Customer Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = today,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "7283840499",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mobile") },
                            modifier = Modifier.weight(1f)
                        )
//                        OutlinedTextField(
//                            value = "Om Chashma Ghar",
//                            onValueChange = {},
//                            readOnly = true,
//                            label = { Text("Shop (readonly)") },
//                            modifier = Modifier.weight(1f)
//                        )
                    }
                }
            }

            // ----- Accordion items (mock)
            val items = listOf(
                "Progressive HC" to "GLASS",
                "Thunder TR" to "FRAME",
                "Thunder 200RS" to "FRAME"
            )
            items.forEachIndexed { idx, (title, type) ->
                ExpressiveAccordionItemV3(
                    index = idx + 1,
                    title = title,
                    type = type,
                    rate = (1000 + idx * 250).toString(),
                    amount = (1000 + idx * 250).toString()
                )
            }

            // Add Item
            OutlinedButton(onClick = { }, shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Item", style = MaterialTheme.typography.labelLarge)
            }

            // ----- Totals
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) { Text("Save Draft") }
                Button(onClick = {}, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) { Text("Generate PDF") }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ExpressiveAccordionItemV3(
    index: Int,
    title: String,
    type: String,
    rate: String,
    amount: String,
    collapsedHeight: Dp = 76.dp // fixed height for collapsed header
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)

    Box(Modifier.fillMaxWidth().padding(vertical = 0.dp)) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = shape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (expanded)
                    MaterialTheme.colorScheme.surfaceContainerHighest
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (expanded) 6.dp else 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Column {
                // Collapsed header with FIXED height
                Row(
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(collapsedHeight) // <- keeps uniform height
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$index.",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(28.dp)
                    )

                    // Allow up to 2 lines but keep the row height fixed
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )

                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.End
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "₹$amount",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.width(84.dp),
                        textAlign = TextAlign.End
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
                        Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
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

        // Optional border for collapsed state
        if (!expanded) {
            Box(
                Modifier
                    .matchParentSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun OrderEntryAccordionExpressiveMockFixedPreview() {
    MaterialTheme { OrderEntryAccordionExpressiveMockFixed() }
}
