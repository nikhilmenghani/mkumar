package com.mkumar.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.ItemPriceEditor
import com.mkumar.ui.components.inputs.OLTextField
import com.mkumar.ui.components.sort.SortBar
import com.mkumar.ui.theme.NikThemePreview

@Composable
private fun InputAndSortPreviewContent() {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField("Aarav Sharma", "Customer name", onValueChange = {}, mode = FieldMode.TitleCase())
            OLTextField("9876543210", "Phone number", onValueChange = {}, mode = FieldMode.Phone())
            ItemPriceEditor(
                initialUnitPrice = "2400",
                initialDiscountPct = "10",
                initialQuantity = "1",
                onUnitPriceChange = {},
                onDiscountChange = {},
                onQuantityChange = {},
                onTotalChange = {}
            )
            SortBar(
                title = "Orders",
                sortField = "Invoice",
                sortOrderAsc = true,
                onSortFieldChange = {},
                onSortOrderChange = {},
                paymentDueOnly = true,
                onPaymentDueOnlyChange = {},
                sortFields = listOf("Invoice", "UpdatedAt")
            )
        }
    }
}

@Preview(name = "Inputs and sorting · Light", showBackground = true, widthDp = 420, heightDp = 700)
@Composable
private fun InputsLightPreview() = NikThemePreview { InputAndSortPreviewContent() }

@Preview(name = "Inputs and sorting · Dark", showBackground = true, widthDp = 420, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputsDarkPreview() = NikThemePreview { InputAndSortPreviewContent() }
