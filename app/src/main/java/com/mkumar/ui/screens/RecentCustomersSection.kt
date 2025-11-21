package com.mkumar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.screens.search.SearchResultItem

@Composable
fun RecentCustomersSection(
    customers: List<UiCustomerMini>,
    openCustomer: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        LazyColumn {
            items(customers, key = { it.id }) { c ->
                SearchResultItem(c) {
                    openCustomer(c.id)
                }
            }
        }
    }
}
