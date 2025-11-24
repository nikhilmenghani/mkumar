package com.mkumar.ui.components.inputs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.headers.FractionedSectionHeader
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
        FractionedSectionHeader("Price Details")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = unitPrice,
                label = "Unit Price (₹)",
                placeholder = "e.g. 1,200",
                mode = FieldMode.Integer,
                modifier = Modifier.weight(1f),
                onValueChange = { txt ->
                    val filtered = txt.filter { it.isDigit() || it == ',' }
                    unitPrice = filtered
                    onUnitPriceChange(filtered)
                    recalculateTotal()
                }
            )
            OLTextField(
                value = discountPct,
                label = "Discount %",
                placeholder = "e.g. 10",
                mode = FieldMode.Percent0to100,
                modifier = Modifier.weight(1f),
                onValueChange = { txt ->
                    val clamped =
                        txt.filter { it.isDigit() }.take(3).toIntOrNull()?.coerceIn(0, 100)
                            ?.toString() ?: ""
                    discountPct = clamped
                    onDiscountChange(clamped)
                    recalculateTotal()
                }
            )
            OLTextField(
                value = total,
                label = "Total (₹)",
                mode = FieldMode.Integer,
                modifier = Modifier.weight(1f),
                enabled = false,
                onValueChange = {},
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
