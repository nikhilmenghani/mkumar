package com.mkumar.ui.components.fabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.theme.NikThemePreview

@Composable
fun StandardFab(
    text: String,
    icon: @Composable () -> Unit,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val fabWidth = 180.dp
    FloatingActionButton(
        modifier = Modifier.width(fabWidth),
        onClick = onClick,
        shape = androidx.compose.foundation.shape.CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2 // adjust if needed
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
private fun StandardFabPreview() {
    NikThemePreview {
        Column {
            StandardFab(
                text = "Add a new\nCustomer",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                },
                onClick = {}
            )
            Spacer(Modifier.height(16.dp))
            StandardFab(
                text = "Updating...\nVersion 2.0",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update"
                    )
                },
                loading = false,
                onClick = {}
            )
        }
    }
}