package com.mkumar.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.model.UiCustomerMini

@Composable
fun CustomerListCard(
    customer: UiCustomerMini,
    modifier: Modifier = Modifier,
    onClick: (UiCustomerMini) -> Unit = {},
    onUploadToGitHub: (UiCustomerMini) -> Unit = {},
    onGeneratePdf: (UiCustomerMini) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Customer card for ${customer.name}"
            },
        shape = MaterialTheme.shapes.extraLarge, // expressive: softer, larger radius
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        onClick = { onClick(customer) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: avatar + name + meta
            ListItem(
                headlineContent = {
                    Text(
                        text = customer.name.ifBlank { "Unnamed customer" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    val phone = customer.phone.takeIf { it.isNotBlank() } ?: "N/A"
                    Text(
                        text = "Phone: $phone",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    InitialsAvatar(
                        name = customer.name.ifBlank { "C U" },
                        size = 40.dp
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(Modifier.height(12.dp))

            // Subtle divider for hierarchy
            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            // Actions bar – expressive: tonal + outlined pair
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onUploadToGitHub(customer) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = "Upload to GitHub"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Upload")
                }

                OutlinedButton(
                    onClick = { onGeneratePdf(customer) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = "Generate PDF"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("PDF")
                }
            }
        }
    }
}

@Composable
private fun InitialsAvatar(
    name: String,
    size: androidx.compose.ui.unit.Dp
) {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    val initials = when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "CU"
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomerListCard() {
    val sampleCustomer = UiCustomerMini(
        id = "12345",
        name = "Jane Doe",
        phone = "9876543210"
        // Add other fields as needed
    )
    CustomerListCard(
        customer = sampleCustomer,
        onClick = {},
        onUploadToGitHub = {},
        onGeneratePdf = {}
    )
}
