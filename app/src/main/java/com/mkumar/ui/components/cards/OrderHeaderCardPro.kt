package com.mkumar.ui.components.cards

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    receivedAt: Long?,
    invoiceNumber: String,
    isDateReadOnly: Boolean,
    invoicePrefix: String,
    onPickDateTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val currentLocalDate = receivedAt?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
    } ?: LocalDate.now(zone)
    val displayedDate = currentLocalDate.format(
        DateTimeFormatter.ofPattern(DateFormat.DEFAULT_DATE_ONLY.pattern)
    )
    var showPicker by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (showPicker) 180f else 0f,
        label = "rotateCalendar"
    )

    if (showPicker) {
        MKDatePickerDialog(
            initialDate = currentLocalDate,
            onDismiss = { showPicker = false },
            onConfirm = { pickedLocalDate ->
                onPickDateTime(
                    pickedLocalDate.atStartOfDay(zone).toInstant().toEpochMilli()
                )
                showPicker = false
            }
        )
    }

    val clipboard = LocalClipboardManager.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InitialsAvatarCompact(name = customerName.ifBlank { "Customer" })
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = customerName,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy customer name",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    clipboard.setText(AnnotatedString(customerName))
                                }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mobile,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy phone number",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(15.dp)
                                .clickable {
                                    clipboard.setText(AnnotatedString(mobile))
                                }
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$invoicePrefix$invoiceNumber",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Received date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                if (!isDateReadOnly) {
                    IconButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Change received date",
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(rotation),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
