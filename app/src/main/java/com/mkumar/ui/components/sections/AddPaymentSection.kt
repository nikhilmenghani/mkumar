package com.mkumar.ui.components.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.formatAsDateTime
import com.mkumar.common.extension.toLong
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentSection(
    onAdd: (amount: Int, date: Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate: Instant? by remember { mutableStateOf(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = "Add Payment",
            style = MaterialTheme.typography.titleSmall
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            OLTextField(
                value = amountText,
                label = "Amount",
                mode = FieldMode.Integer,
                modifier = Modifier.weight(1f),
                onValueChange = { amountText = it }
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedDate?.formatAsDateTime() ?: "Select Date")
            }
        }

        Button(
            enabled = amountText.isNotBlank() && selectedDate != null,
            onClick = {
                onAdd(amountText.toInt(), selectedDate!!.toLong())
                amountText = ""
                selectedDate = null
            }
        ) {
            Text("Save Payment")
        }
    }
    val pickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis!!
                        val date = Instant.ofEpochMilli(millis)
                        selectedDate = date
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
