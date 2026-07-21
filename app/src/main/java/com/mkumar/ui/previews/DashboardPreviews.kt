package com.mkumar.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.sort.SortBar
import com.mkumar.ui.screens.RecentOrdersList
import com.mkumar.ui.theme.NikThemePreview

@Composable
private fun DashboardRecentOrdersPreviewContent() {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(12.dp)) {
            SortBar(
                title = "Recent Orders",
                sortField = "UpdatedAt",
                sortOrderAsc = false,
                onSortFieldChange = {},
                onSortOrderChange = {},
                paymentDueOnly = false,
                onPaymentDueOnlyChange = {}
            )
            Box(Modifier.height(780.dp)) {
                RecentOrdersList(
                    orders = PreviewData.orders,
                    invoicePrefix = "MKumar-",
                    onOrderClick = { _, _ -> },
                    onInvoiceClick = { _, _ -> },
                    onShareClick = { _, _ -> },
                    onDeleteClick = {},
                    onOpenCustomer = {}
                )
            }
        }
    }
}

@Preview(name = "Dashboard · Light", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DashboardRecentOrdersLightPreview() = NikThemePreview {
    DashboardRecentOrdersPreviewContent()
}

@Preview(name = "Dashboard · Dark", showBackground = true, widthDp = 420, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardRecentOrdersDarkPreview() = NikThemePreview {
    DashboardRecentOrdersPreviewContent()
}
