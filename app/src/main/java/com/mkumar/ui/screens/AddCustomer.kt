package com.mkumar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerInfo
import com.mkumar.data.CustomerOrder

@Composable
fun SingleChoiceChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    label: String,
    leadingIcon: ImageVector = Icons.Outlined.Check,
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 4.dp),
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        label = { Text(text = label) },
        leadingIcon = {
            Row {
                AnimatedVisibility(visible = selected) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            }
        },
    )
}

@Composable
fun FilterChipExample(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        onClick = { onClick },
        label = {
            Text(label)
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCustomer(customerOrder: CustomerOrder, options: List<String>) {
    val customerInfo = customerOrder.customerInfo
    var name by remember { mutableStateOf(customerInfo.name) }
    var phone by remember { mutableStateOf(customerInfo.phoneNumber) }
    var email by remember { mutableStateOf(customerInfo.email) }
    var selectedChip by remember { mutableStateOf(options.firstOrNull() ?: "") }
    val selectedOptions = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.padding(16.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option in selectedOptions

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedOptions.remove(option)
                        } else {
                            selectedOptions.add(option)
                        }
                    },
                    label = { Text(option) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show selected options for demo
        Text("Selected Options: ${selectedOptions.joinToString()}")
    }

//    Column(modifier = Modifier.padding(16.dp)) {
//        Text(text = "Customer Information")
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(text = "Select the item")
//        LazyRow(modifier = Modifier.fillMaxWidth()) {
//            items(options) { option ->
//                SingleChoiceChip(
//                    selected = selectedChip == option,
//                    label = option,
//                    onClick = { selectedChip = option }
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        when (selectedChip) {
//            "Frame" -> {
//                FrameForm(customerOrder)
//            }
//            "Contact Lens" -> {
//                LensForm(customerOrder)
//            }
//            "Watch" -> {
//                // Watch specific fields
//            }
//            "Wall Clock" -> {
//                // Wall Clock specific fields
//            }
//            "Manual Entry" -> {
//                // Manual Entry specific fields
//            }
//        }
//    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddCustomer() {
    val customerInfo = CustomerInfo(name = "John Doe", phoneNumber = "1234567890", email = "a@b.com")
    val customerOrder = CustomerOrder(customerInfo = customerInfo)
    val options = listOf("Frame", "Contact Lens", "Watch", "Wall Clock", "Manual Entry")
    AddCustomer(customerOrder = customerOrder, options = options)
}