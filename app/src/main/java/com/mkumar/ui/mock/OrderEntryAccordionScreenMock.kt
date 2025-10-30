package com.mkumar.ui.mock

import com.mkumar.ui.order.mock.OrderAccordionItem
import com.mkumar.ui.order.mock.OrderHeaderCard
import com.mkumar.ui.order.mock.OrderTotalsCard2
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryAccordionScreenMock(modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    val today = remember { LocalDate.now().toString() } // yyyy-MM-dd

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header (readonly)
        OrderHeaderCard(
            customerName = "Arunbhai",
            date = today,
            mobile = "7283840499"
        )

        // Accordion items (mock)
        val items = listOf(
            OrderLineMock(1, "Progressive HC", "GLASS", "1", "1000", "1000"),
            OrderLineMock(2, "Thunder TR", "FRAME", "1", "1250", "1250"),
            OrderLineMock(3, "Thunder 200RS", "FRAME", "1", "1500", "1500"),
        )
//        items.forEach { line ->
//            OrderAccordionItem(
//                index = line.index,
//                title = line.description,
//                type = line.type,
//                qty = line.qty,
//                rate = line.rate,
//                amount = line.amount,
//                collapsedHeight = 76.dp
//            )
//        }

        // Add Item (mock)
        OutlinedButton(
            onClick = { /* no-op in mock */ },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add New Item")
        }

        // Totals
        OrderTotalsCard2(
            subtotal = "1,700",
            discount = "-",
            other = "-",
            grandTotal = "1,850",
            advance = "500",
            balance = "1,350"
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save Draft") }
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Generate PDF") }
        }
    }
}

data class OrderLineMock(
    val index: Int,
    val description: String,
    val type: String,
    val qty: String,
    val rate: String,
    val amount: String
)

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun OrderEntryAccordionScreenMockPreview() {
    MaterialTheme { OrderEntryAccordionScreenMock() }
}
