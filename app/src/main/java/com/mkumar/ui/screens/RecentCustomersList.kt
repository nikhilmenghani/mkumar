package com.mkumar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerFormState

@Composable
fun RecentCustomersList(
    customers: List<CustomerFormState>,
    onCustomerClick: (CustomerFormState) -> Unit
) {
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val customersSectionHeight = screenHeight * 0.35f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(customersSectionHeight)   // fixed height for dashboard section
            .clip(MaterialTheme.shapes.medium)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(customers) { customer ->
                RecentCustomerCard(customer) { onCustomerClick(customer) }
            }
        }
    }
}

@Composable
fun RecentCustomerCard(
    customer: CustomerFormState,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(customer.name, style = MaterialTheme.typography.titleMedium)
            Text(customer.phone, style = MaterialTheme.typography.bodySmall)
        }
    }
}
