package com.mkumar.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    icon: ImageVector? = null,
    highlightConfirmAsDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlightConfirmAsDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(0.8f),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = ButtonDefaults.ContentPadding,
                    onClick = onDismiss
                ) {
                    Text(
                        text = dismissLabel,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                }
                Button(
                    modifier = Modifier.weight(1.35f),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = ButtonDefaults.ContentPadding,
                    colors = if (highlightConfirmAsDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    onClick = onConfirm
                ) {
                    Text(
                        text = confirmLabel,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                }
            }
        },
        dismissButton = {},
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmActionDialogPreview() {
    MaterialTheme {
        ConfirmActionDialog(
            title = "Confirm Action",
            message = "Are you sure you want to proceed with this action?",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfirmActionDialogPreviewDark() {
    MaterialTheme {
        ConfirmActionDialog(
            title = "Confirm Action",
            message = "Are you sure you want to proceed with this action?",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmActionDialogDestructivePreview() {
    MaterialTheme {
        ConfirmActionDialog(
            title = "Delete Product",
            message = "This action cannot be undone. Are you sure you want to delete this product?",
            onConfirm = {},
            onDismiss = {},
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            icon = androidx.compose.material.icons.Icons.Default.Delete,
            highlightConfirmAsDestructive = true
        )
    }
}
