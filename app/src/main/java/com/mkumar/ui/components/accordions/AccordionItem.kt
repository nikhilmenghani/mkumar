@file:Suppress("unused")

package com.mkumar.ui.components.accordions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AccordionItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable (() -> Unit))? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Allow both controlled and uncontrolled expansion
    val internalExpanded = rememberSaveable { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded.value
    val toggle: () -> Unit = {
        if (onExpandedChange != null && expanded != null) onExpandedChange(!isExpanded)
        else internalExpanded.value = !isExpanded
    }

    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = toggle
                )
                .semantics {
                    role = Role.Button
                    contentDescription = title
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.let {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) { it() }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            actions?.let {
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = it
                )
            }

            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                content()
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccordionItem() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccordionItem(
                title = "Frame – Ray-Ban RX7151",
                subtitle = "₹2,499 • Matte Black • Full Rim",
                leading = { Text("👓") }
            ) {
                Text(
                    "Size: 52-18-140\nLens: Demo lens\nWarranty: 1 year on manufacturing defects."
                )
            }

            var expanded by remember { mutableStateOf(true) }
            AccordionItem(
                title = "Contact Lenses",
                subtitle = "₹2,999 • Monthly • 6 pack",
//                leading = { Text("🟢") },
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                Text("Brand: Acme Vision\nBC: 8.6\nDIA: 14.2\nPower: -2.00")
            }
        }
    }
}
