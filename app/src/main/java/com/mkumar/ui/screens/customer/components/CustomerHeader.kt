package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.model.CustomerHeaderUi
import com.mkumar.model.UiCustomer
import com.mkumar.ui.components.cards.InitialsAvatarCompact
import com.mkumar.ui.theme.NikThemePreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CustomerHeader(
    header: CustomerHeaderUi,
    modifier: Modifier = Modifier,
) {
    val customer = header.customer ?: return
    val joined = DateTimeFormatter.ofPattern("MMM d, yyyy").format(
        Instant.ofEpochMilli(customer.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InitialsAvatarCompact(name = customer.name.ifBlank { "Customer" })
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${header.totalOrders} orders",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomerMetric(
                    label = "Customer since",
                    value = joined,
                    modifier = Modifier.weight(1f)
                )
                CustomerMetric(
                    label = "Total business",
                    value = "₹${header.totalSpent}",
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                CustomerMetric(
                    label = "Payment due",
                    value = "₹${header.totalRemaining}",
                    modifier = Modifier.weight(1f),
                    valueColor = if (header.totalRemaining > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomerMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 420)
@Composable
fun CustomerHeaderPreview() = NikThemePreview {
    CustomerHeader(
        header = CustomerHeaderUi(
            customer = UiCustomer(
                id = "123",
                name = "Mahendra Menghani",
                phone = "+91 98765 43210",
                createdAt = nowUtcMillis() - 86_400_000L * 365,
            ),
            totalOrders = 42,
            totalSpent = 12_500,
            totalRemaining = 2_500
        ),
        modifier = Modifier.padding(16.dp)
    )
}
