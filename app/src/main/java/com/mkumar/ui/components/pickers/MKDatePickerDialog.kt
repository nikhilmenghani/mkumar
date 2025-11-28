package com.mkumar.ui.components.pickers

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Generic date picker that:
 *
 * - Accepts UTC epochMillis from DB
 * - Converts to LocalDate in device timezone for display
 * - Lets user pick a LocalDate
 * - Converts chosen LocalDate back to UTC epochMillis
 */
@Composable
fun MKDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val utcZone = ZoneId.of("UTC")

    // LocalDate -> millis (UTC midnight)
    val initialMillis = initialDate
        .atStartOfDay(utcZone)
        .toInstant()
        .toEpochMilli()

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        // millis is UTC midnight -> LocalDate
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(utcZone)
                            .toLocalDate()

                        onConfirm(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
