package com.mkumar.ui.components.fabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    if (text.isNotBlank()) {
        ExtendedFloatingActionButton(
            modifier = Modifier.then(Modifier.width(fabWidth)),
            onClick = onClick,
            icon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    icon()
                }
            },
            text = {
                if (loading) {
                    Text("Processing...")
                } else {
                    Text(text)
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                icon()
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
                text = "Add a new Customer",
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
                text = "MKumar v0.6 Available",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update"
                    )
                },
                onClick = {}
            )
        }
    }
}