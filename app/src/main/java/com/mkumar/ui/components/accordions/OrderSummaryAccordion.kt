package com.mkumar.ui.components.accordions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.cards.OrderTotalsNoCard

@Composable
fun OrderSummaryAccordion(
    totalAmount: Int,
    adjustedAmount: Int,
    onAdjustedAmountChange: (Int) -> Unit,
    advanceTotal: Int,
    onAdvanceTotalChange: (Int) -> Unit,
    remainingBalance: Int,                // read-only (precomputed in VM or state)
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    title: String = "Order summary"
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevronRotation")

    Surface(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        onClick = { expanded = !expanded } // whole header is tappable
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                // Show a quick summary even when collapsed
                Text(
                    text = "Remaining ₹$remainingBalance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()
                    OrderTotalsNoCard(
                        initialAdvanceTotal = advanceTotal,
                        onAdvanceTotalChange = onAdvanceTotalChange,
                        totalAmount = totalAmount,                 // read-only
                        adjustedAmount = adjustedAmount,
                        onAdjustedAmountChange = onAdjustedAmountChange,
                        remainingBalance = remainingBalance
                    )
                }
            }
        }
    }
}
