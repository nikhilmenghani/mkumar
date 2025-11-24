package com.mkumar.ui.components.badges

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HighlightedBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun HighlightedBadgeRowAndColumnPreview() {
    Column {
        // Row of badges
        Row {
            HighlightedBadge(text = "₹2400")
            HighlightedBadge(
                text = "PAID",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            HighlightedBadge(
                text = "DUE",
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
            HighlightedBadge(
                text = "NEW",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HighlightedBadge(
                text = "₹1200",
                backgroundColor = Color(0xFF1565C0).copy(alpha = 0.3f),
                contentColor = Color(0xFF0D47A1)
            )
        }
        // Spacer for separation
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
        // Column of badges
        Column {
            HighlightedBadge(text = "₹2400")
            HighlightedBadge(
                text = "PAID",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            HighlightedBadge(
                text = "DUE",
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
            HighlightedBadge(
                text = "NEW",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HighlightedBadge(
                text = "₹1200",
                backgroundColor = Color(0xFF1565C0).copy(alpha = 0.3f),
                contentColor = Color(0xFF0D47A1)
            )
        }
    }
}
