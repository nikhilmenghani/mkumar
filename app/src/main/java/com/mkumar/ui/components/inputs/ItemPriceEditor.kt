package com.mkumar.ui.components.inputs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong

@Composable
fun ItemPriceEditor(
    initialUnitPrice: String,
    initialDiscountPct: String,
    initialQuantity: String,
    onUnitPriceChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onTotalChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var unitPrice by remember { mutableStateOf(if (initialUnitPrice == "0") "" else initialUnitPrice) }
    var discountPct by remember { mutableStateOf(initialDiscountPct) }
    var quantity by remember { mutableStateOf(initialQuantity) }
    var total by remember(initialUnitPrice, initialDiscountPct, initialQuantity) {
        mutableStateOf(
            run {
                val price = initialUnitPrice.replace(",", "").toLongOrNull() ?: 0L
                val qty = initialQuantity.toIntOrNull() ?: 1
                val discount = initialDiscountPct.toIntOrNull() ?: 0
                val subtotal = price * qty
                val discounted = subtotal * (1 - discount / 100.0)
                discounted.roundToLong().toString()
            }
        )
    }


    fun recalculateTotal() {
        val price = unitPrice.replace(",", "").toLongOrNull() ?: 0L
        val qty = quantity.toIntOrNull() ?: 1
        val discount = discountPct.toIntOrNull() ?: 0
        val subtotal = price * qty
        val discounted = subtotal * (1 - discount / 100.0)
        total = discounted.roundToLong().toString()
        onTotalChange(total)
    }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { txt ->
                    val filtered = txt.filter { it.isDigit() || it == ',' }
                    unitPrice = filtered
                    onUnitPriceChange(filtered)
                    recalculateTotal()
                },
                label = { Text("Unit Price (₹)") },
                placeholder = { Text("e.g. 1,200") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = discountPct,
                onValueChange = { txt ->
                    val clamped =
                        txt.filter { it.isDigit() }.take(3).toIntOrNull()?.coerceIn(0, 100)
                            ?.toString() ?: ""
                    discountPct = clamped
                    onDiscountChange(clamped)
                    recalculateTotal()
                },
                label = { Text("Discount %") },
                placeholder = { Text("e.g. 10") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                suffix = { Text("%") },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { txt ->
                    val filtered = txt.filter { it.isDigit() }.take(4)
                    quantity = filtered
                    onQuantityChange(filtered)
                    recalculateTotal()
                },
                label = { Text("Qty") },
                placeholder = { Text("e.g. 2") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = total,
                onValueChange = {},
                label = { Text("Total (₹)") },
                enabled = false,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewItemPriceEditor() {
    ItemPriceEditor(
        initialUnitPrice = "1200",
        initialDiscountPct = "10",
        initialQuantity = "2",
        onUnitPriceChange = {},
        onDiscountChange = {},
        onQuantityChange = {},
        onTotalChange = {},
        modifier = Modifier.padding(16.dp)
    )
}
