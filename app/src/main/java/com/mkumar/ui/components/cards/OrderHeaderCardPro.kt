package com.mkumar.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.App.Companion.globalClass
import com.mkumar.common.extension.DateFormat
import com.mkumar.ui.components.pickers.MKDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHeaderCardPro(
    customerName: String,
    mobile: String,
    receivedAt: Long?,              // raw UTC millis from DB
    invoiceNumber: String,
    isDateReadOnly: Boolean,
    onPickDateTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()

    // 1) Convert receivedAt (UTC) → LocalDate
    val currentLocalDate: LocalDate = if (receivedAt != null) {
        Instant.ofEpochMilli(receivedAt)
            .atZone(zone)
            .toLocalDate()
    } else {
        LocalDate.now(zone)
    }

    // 2) For initial picker value
    var selectedDate by remember { mutableStateOf(currentLocalDate) }

    // 3) Display text
    val displayedDate = selectedDate.format(
        DateTimeFormatter.ofPattern(DateFormat.DEFAULT_DATE_ONLY.pattern)
    )

    var showPicker by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (showPicker) 180f else 0f,
        label = "rotateCalendar"
    )

    // ───────────────────────────
    // DATE PICKER
    // ───────────────────────────
    if (showPicker) {
        MKDatePickerDialog(
            initialDate = selectedDate,    // CORRECT: LocalDate
            onDismiss = { showPicker = false },
            onConfirm = { pickedLocalDate ->   // returns LocalDate
                selectedDate = pickedLocalDate

                // Convert local date → local midnight → UTC millis
                val pickedUtc = pickedLocalDate
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli()

                onPickDateTime(pickedUtc)
                showPicker = false
            }
        )
    }

    val clipboard = LocalClipboardManager.current

    // ───────────────────────────
    // UI CARD CONTENT
    // ───────────────────────────
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // Row 1 — Name + Copy + Invoice
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy name",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                clipboard.setText(AnnotatedString(customerName))
                            }
                    )
                }

                Text(
                    text = "#" + globalClass.preferencesManager.invoicePrefs.invoicePrefix + invoiceNumber,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Row 2 — Phone + Copy + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

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

                    Spacer(Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy phone",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                clipboard.setText(AnnotatedString(mobile))
                            }
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "Received: $displayedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!isDateReadOnly) {
                        Spacer(Modifier.width(6.dp))

                        IconButton(
                            onClick = { showPicker = true },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Change date",
                                modifier = Modifier
                                    .size(16.dp)
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

