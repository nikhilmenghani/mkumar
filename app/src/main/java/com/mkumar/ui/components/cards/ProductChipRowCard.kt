package com.mkumar.ui.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.ui.components.chips.ProductChipRow


@Composable
fun ProductChipRowCard(
    products: List<ProductEntry>?,
    selectedId: String?,
    onChipClick: (String) -> Unit,
    onChipDelete: (String) -> Unit,
    getCurrentBuffer: (ProductEntry) -> ProductFormData? = { _ -> null },
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean = { _, _ -> false }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Products Added",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductChipRow(
                products = products,
                selectedId = selectedId,
                onChipClick = onChipClick,
                onChipDelete = onChipDelete,
                getCurrentBuffer = getCurrentBuffer,
                hasUnsavedChanges = hasUnsavedChanges
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProductChipRowCardPreview() {
    val sampleProducts = listOf(
        ProductEntry(id = "1", productType = ProductType.fromLabel("Lens"), isSaved = true),
        ProductEntry(id = "2", productType = ProductType.fromLabel("ContactLens"), isSaved = false),
        ProductEntry(id = "3", productType = ProductType.fromLabel("Frame"), isSaved = true)
    )
    ProductChipRowCard(
        products = sampleProducts,
        selectedId = "2",
        onChipClick = {},
        onChipDelete = {},
        getCurrentBuffer = { null },
        hasUnsavedChanges = { _, _ -> false }
    )
}
