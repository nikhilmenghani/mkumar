package com.mkumar.ui.components.bottomsheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomer(
    title: String,
    sheetContent: @Composable () -> Unit,
    onDismiss: () -> Unit = {},
    showFloatingBar : Boolean = false
) {
    BaseBottomSheet(
        title = title,
        sheetContent = sheetContent,
        onDismiss = onDismiss,
        showFloatingBar = showFloatingBar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveCustomer(
    title: String,
    sheetContent: @Composable () -> Unit,
    onDismiss: () -> Unit = {},
    showFloatingBar : Boolean = false
    ) {
        BaseBottomSheet(
            title = title,
            sheetContent = sheetContent,
            onDismiss = onDismiss,
            showFloatingBar = showFloatingBar
        )
    }