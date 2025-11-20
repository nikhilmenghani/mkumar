package com.mkumar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerFormState
import com.mkumar.ui.components.LongPressMenuAnchor
import com.mkumar.ui.components.ProMenuItem

@Composable
fun RecentCustomersHorizontalList(
    customers: List<CustomerFormState>,
    onCustomerClick: (CustomerFormState) -> Unit,
    onDelete: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {},
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(customers) { customer ->
            RecentCustomerHorizontalCard(
                customer,
                onClick = { onCustomerClick(customer) },
                onEdit = { onEdit(customer) },
                onDelete = { onDelete(customer) }
            )
        }
    }
}

@Composable
fun RecentCustomerHorizontalCard(
    customer: CustomerFormState,
    onClick: () -> Unit,
    onEdit: (CustomerFormState) -> Unit,
    onDelete: (CustomerFormState) -> Unit
) {
    LongPressMenuAnchor(
        onClick = onClick,
        menuItems = listOf(
            ProMenuItem(
                title = "Edit",
                supportingText = "Update name or phone",
                icon = Icons.Outlined.Edit,
                onClick = { onEdit(customer) }
            ),
            ProMenuItem(
                title = "Delete",
                supportingText = "Remove customer",
                icon = Icons.Outlined.Delete,
                destructive = true,
                startNewGroup = true,
                onClick = { onDelete(customer) }
            )
        )
    ) {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .width(200.dp)
                .height(120.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(customer.name, style = MaterialTheme.typography.titleMedium)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


