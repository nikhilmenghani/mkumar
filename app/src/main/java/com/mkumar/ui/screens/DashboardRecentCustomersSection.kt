package com.mkumar.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.model.UiCustomerMini

@Composable
fun DashboardRecentCustomersSection(
    customers: List<UiCustomerMini>,
    onCustomerClick: (UiCustomerMini) -> Unit
) {
    if (customers.isEmpty()) return

    Text(
        text = "Recent Customers",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        customers.forEach { customer ->
            DashboardCustomerCard(
                customer = customer,
                onClick = { onCustomerClick(customer) }
            )
        }
    }
}


@Composable
fun DashboardCustomerCard(
    customer: UiCustomerMini,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
