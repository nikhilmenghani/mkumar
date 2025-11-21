package com.mkumar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.screens.search.SearchResultItem

@Composable
fun RecentCustomersSection(
    customers: List<UiCustomerMini>,
    openCustomer: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Text(
            "Recently Added Customers",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn {
            items(customers, key = { it.id }) { c ->
                SearchResultItem(c) {
                    openCustomer(c.id)
                }
            }
        }
    }
}
