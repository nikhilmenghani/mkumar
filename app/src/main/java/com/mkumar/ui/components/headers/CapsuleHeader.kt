package com.mkumar.ui.components.headers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CapsuleHeader(
    title: String,
    modifier: Modifier = Modifier
){
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}