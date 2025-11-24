package com.mkumar.ui.components.cards

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import com.mkumar.ui.theme.AppColors


@Composable
fun OrderTotalsCard(
    initialAdvanceTotal: Int,
    onAdvanceTotalChange: (Int) -> Unit,
    totalAmount: Int,
    adjustedAmount: Int,
    onAdjustedAmountChange: (Int) -> Unit,
) {
    var adjustedAmount by remember { mutableIntStateOf(adjustedAmount) }
    var advanceTotal by remember { mutableIntStateOf(initialAdvanceTotal) }
    val remainingBalance by remember (adjustedAmount, advanceTotal) {
        mutableIntStateOf(adjustedAmount - advanceTotal)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = AppColors.elevatedCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalAmount.toString(),
                    onValueChange = {},
                    label = { Text("Total (₹)") },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = remainingBalance.toString(),
                    onValueChange = {

                    },
                    label = { Text("Remaining Balance (₹)") },
                    enabled = false,
                    singleLine = true,
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
                    value = adjustedAmount.toString(),
                    onValueChange = {
                        adjustedAmount = it.toIntOrNull() ?: 0
                        onAdjustedAmountChange(adjustedAmount)
                    },
                    label = { Text("Adjusted Total") },
                    placeholder = { Text("e.g. 200") },
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
                    value = advanceTotal.toString(),
                    onValueChange = {
                        advanceTotal = it.toIntOrNull() ?: 0
                        onAdvanceTotalChange(advanceTotal)
//                        recalculateRemainingBalance()
                    },
                    label = { Text("Advance Total") },
                    placeholder = { Text("e.g. 200") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OrderTotalsNoCard(
    initialAdvanceTotal: Int,
    onAdvanceTotalChange: (Int) -> Unit,
    adjustedAmount: Int,
    onAdjustedAmountChange: (Int) -> Unit
) {
    var adjustedAmount by remember(adjustedAmount) { mutableIntStateOf(adjustedAmount) }
    var advanceTotal by remember(initialAdvanceTotal) { mutableIntStateOf(initialAdvanceTotal) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OLTextField(
            value = adjustedAmount.toString(),
            label = "Adjusted Total",
            placeholder = "e.g. 200",
            mode = FieldMode.Integer,
            modifier = Modifier.weight(1f),
            onValueChange = {
                adjustedAmount = it.toIntOrNull() ?: 0
                onAdjustedAmountChange(adjustedAmount)
            },
        )

        OLTextField(
            value = advanceTotal.toString(),
            label = "Advance Total",
            placeholder = "e.g. 200",
            mode = FieldMode.Integer,
            modifier = Modifier.weight(1f),
            onValueChange = {
                advanceTotal = it.toIntOrNull() ?: 0
                onAdvanceTotalChange(advanceTotal)
            },
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewOrderTotalsCard() {
    OrderTotalsNoCard(
        initialAdvanceTotal = 5000,
        adjustedAmount = 5000,
        onAdjustedAmountChange = {},
        onAdvanceTotalChange = {},
    )
}

