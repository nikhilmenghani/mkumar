package com.mkumar.ui.screens.customer.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.ProductType


@Composable
fun ProductTypePicker(
    selected: ProductType?,
    onSelected: (ProductType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        FilterChip(selected = selected == ProductType.LENS, onClick = { onSelected(ProductType.LENS) }, label = { Text("Lens") })
        FilterChip(selected = selected == ProductType.FRAME, onClick = { onSelected(ProductType.FRAME) }, label = { Text("Frame") })
        FilterChip(selected = selected == ProductType.CONTACT_LENS, onClick = { onSelected(ProductType.CONTACT_LENS) }, label = { Text("Contact Lens") })
    }
}