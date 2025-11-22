package com.mkumar.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.App.Companion.globalClass
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHeaderCardPro(
    customerName: String,
    mobile: String,
    displayedDate: String,
    invoiceNumber: String,
    isDateReadOnly: Boolean,
    onPickDateTime: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDateDialog by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (showDateDialog) 180f else 0f,
        label = "rotateCalendar"
    )

    // ─────────────────────────────────────────────
    // DATE PICKER (IST conversion — correct & stable)
    // ─────────────────────────────────────────────
    if (showDateDialog) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val ist = ZoneId.of("Asia/Kolkata")

                            val pickedUtcDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            val istInstant = pickedUtcDate
                                .atStartOfDay(ist)
                                .toInstant()

                            onPickDateTime(istInstant)
                        }
                        showDateDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // ─────────────────────────────────────────────
    // PROFESSIONAL ROUNDED CARD WITH BORDER
    // ─────────────────────────────────────────────
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        tonalElevation = 0.dp, // border replaces elevation
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ─────────── Row 1: Name + Invoice Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = globalClass.preferencesManager.invoicePrefs.invoicePrefix + "$invoiceNumber",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ─────────── Row 2: Phone (left) + Received Date (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // LEFT → Phone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = mobile,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.weight(1f))  // pushes date to the right

                // RIGHT → Received Date + Calendar Icon
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "Received: $displayedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!isDateReadOnly) {
                        Spacer(Modifier.width(6.dp))

                        IconButton(
                            onClick = { showDateDialog = true },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Change date",
                                modifier = Modifier
                                    .size(16.dp) // same visual weight as text
                                    .rotate(rotation),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
