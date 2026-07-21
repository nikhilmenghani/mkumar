package com.mkumar.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.RecentCustomersList
import com.mkumar.ui.screens.customer.components.OrderList
import com.mkumar.ui.theme.NikThemePreview

@Composable
private fun CustomerListsPreviewContent() {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(12.dp)) {
            RecentCustomersList(
                customers = PreviewData.customers,
                onCustomerClick = {},
                modifier = Modifier.height(390.dp)
            )
            OrderList(
                orders = PreviewData.orderRows,
                onAction = {},
                modifier = Modifier.height(470.dp)
            )
        }
    }
}

@Preview(name = "Customer and order lists · Light", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun CustomerListsLightPreview() = NikThemePreview { CustomerListsPreviewContent() }

@Preview(name = "Customer and order lists · Dark", showBackground = true, widthDp = 420, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomerListsDarkPreview() = NikThemePreview { CustomerListsPreviewContent() }
