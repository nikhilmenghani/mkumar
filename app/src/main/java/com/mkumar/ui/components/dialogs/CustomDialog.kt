package com.mkumar.ui.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun CustomDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        content()
    }
}