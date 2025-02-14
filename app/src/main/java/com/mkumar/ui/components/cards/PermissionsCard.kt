package com.mkumar.ui.components.cards


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.theme.NikThemePreview


@Composable
fun PermissionsCard(
    title: String = "Test Permission",
    description: String = "This is a test permission",
    isPermissionGranted: Boolean = false,
    onRequestPermission: () -> Unit,
    permissionsText: String = "Request Permission"
) {
    // Background and icon color based on permission status
    val backgroundColor = if (isPermissionGranted) Color(0xFFB9F6CA) else Color(0xFFF8D7DA)
    val iconColor = if (isPermissionGranted) Color(0xFF388E3C) else Color(0xFFC62828)
    val buttonText = if (isPermissionGranted) "Permission Granted" else permissionsText

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Status Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }

            // Divider
            HorizontalDivider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Action Button
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPermissionGranted) Color(0xFFB9F6CA) else Color(0xFFF36170),
                    contentColor = Color.Black
                )
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
fun PermissionsCard2(
    title: String = "Test Permission",
    description: String = "This is a test permission",
    isPermissionGranted: Boolean = false,
    onRequestPermission: () -> Unit
) {
    // Background and icon color based on permission status
    val backgroundColor = if (isPermissionGranted) Color(0xFFB9F6CA) else Color(0xFFF8D7DA)
    val permissionIcon = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Refresh

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                }

                // Divider
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRequestPermission
            ) {
                Icon(
                    imageVector = permissionIcon,
                    contentDescription = "Check Root Access",
                    tint = Color.Black
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSamplePermissionsManagerCard() {
    NikThemePreview {
        Column{
            PermissionsCard(
                title = "Test Permission",
                description = "This is a test permission",
                isPermissionGranted = true,
                onRequestPermission = { },
                permissionsText = "Request Permission"
            )
            PermissionsCard2(
                title = "Test Permission",
                description = "This is a test permission",
                isPermissionGranted = true,
                onRequestPermission = { },
            )
        }
    }
}