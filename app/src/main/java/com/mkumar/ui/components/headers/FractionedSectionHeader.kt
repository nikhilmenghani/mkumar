package com.mkumar.ui.components.headers

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FractionedSectionHeader(
    text: String,
    fraction: Float = 0.1f              // 0.5f = center, 0.2f = left-biased
) {
    require(fraction in 0f..1f) { "fraction must be between 0.0 and 1.0" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left divider (takes `fraction` width)
        Divider(
            modifier = Modifier.weight(fraction),
            thickness = 1.dp
        )

        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Right divider (rest of the space)
        Divider(
            modifier = Modifier.weight(1f - fraction),
            thickness = 1.dp
        )
    }
}
