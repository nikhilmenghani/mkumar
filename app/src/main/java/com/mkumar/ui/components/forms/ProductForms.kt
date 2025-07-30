package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData

@Composable
fun FrameForm(
    initialData: ProductFormData.FrameData? = null,
    onChange: (ProductFormData.FrameData) -> Unit,
    showSave: Boolean = true,
    onSave: (ProductFormData.FrameData) -> Unit
) {
    var brand by remember { mutableStateOf(initialData?.brand.orEmpty()) }
    var color by remember { mutableStateOf(initialData?.color.orEmpty()) }
    var size by remember { mutableStateOf(initialData?.size.orEmpty()) }

    Column {
        var isBrandFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (isBrandFocused && !it.isFocused) {
                        onChange(ProductFormData.FrameData(brand, color, size))
                    }
                    isBrandFocused = it.isFocused
                }
        )

        var isColorFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isColorFocused && !it.isFocused) {
                        onChange(ProductFormData.FrameData(brand, color, size))
                    }
                    isColorFocused = it.isFocused
                }
        )

        var isSizeFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("Size") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isSizeFocused && !it.isFocused) {
                        onChange(ProductFormData.FrameData(brand, color, size))
                    }
                    isSizeFocused = it.isFocused
                }
        )

        if (showSave) {
            Button(
                onClick = { onSave(ProductFormData.FrameData(brand, color, size)) }
            ) {
                Text("Save Frame")
            }
        }
    }
}

@Composable
fun LensForm(
    initialData: ProductFormData.LensData? = null,
    onChange: (ProductFormData.LensData) -> Unit,
    showSave: Boolean = true,
    onSave: (ProductFormData.LensData) -> Unit
) {
    var leftSphere by remember { mutableStateOf(initialData?.leftSphere.orEmpty()) }
    var leftAxis by remember { mutableStateOf(initialData?.leftAxis.orEmpty()) }
    var rightSphere by remember { mutableStateOf(initialData?.rightSphere.orEmpty()) }
    var rightAxis by remember { mutableStateOf(initialData?.rightAxis.orEmpty()) }

    Column {
        var leftSphereFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = leftSphere,
            onValueChange = { leftSphere = it },
            label = { Text("Left Sphere") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (leftSphereFocused && !it.isFocused) {
                        onChange(ProductFormData.LensData(leftSphere, leftAxis, rightSphere, rightAxis))
                    }
                    leftSphereFocused = it.isFocused
                }
        )

        var leftAxisFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = leftAxis,
            onValueChange = { leftAxis = it },
            label = { Text("Left Axis") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (leftAxisFocused && !it.isFocused) {
                        onChange(ProductFormData.LensData(leftSphere, leftAxis, rightSphere, rightAxis))
                    }
                    leftAxisFocused = it.isFocused
                }
        )

        var rightSphereFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = rightSphere,
            onValueChange = { rightSphere = it },
            label = { Text("Right Sphere") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (rightSphereFocused && !it.isFocused) {
                        onChange(ProductFormData.LensData(leftSphere, leftAxis, rightSphere, rightAxis))
                    }
                    rightSphereFocused = it.isFocused
                }
        )

        var rightAxisFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = rightAxis,
            onValueChange = { rightAxis = it },
            label = { Text("Right Axis") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (rightAxisFocused && !it.isFocused) {
                        onChange(ProductFormData.LensData(leftSphere, leftAxis, rightSphere, rightAxis))
                    }
                    rightAxisFocused = it.isFocused
                }
        )

        if (showSave) {
            Button(
                onClick = {
                    onSave(ProductFormData.LensData(leftSphere, leftAxis, rightSphere, rightAxis))
                }
            ) {
                Text("Save Lens")
            }
        }
    }
}

@Composable
fun ContactLensForm(
    initialData: ProductFormData.ContactLensData? = null,
    onChange: (ProductFormData.ContactLensData) -> Unit,
    showSave: Boolean = true,
    onSave: (ProductFormData.ContactLensData) -> Unit
) {
    var power by remember { mutableStateOf(initialData?.power.orEmpty()) }
    var duration by remember { mutableStateOf(initialData?.duration.orEmpty()) }

    Column {
        var isPowerFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("Power") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isPowerFocused && !it.isFocused) {
                        onChange(ProductFormData.ContactLensData(power, duration))
                    }
                    isPowerFocused = it.isFocused
                }
        )

        var isDurationFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isDurationFocused && !it.isFocused) {
                        onChange(ProductFormData.ContactLensData(power, duration))
                    }
                    isDurationFocused = it.isFocused
                }
        )

        if (showSave) {
            Button(
                onClick = { onSave(ProductFormData.ContactLensData(power, duration)) }
            ) {
                Text("Save Contact Lens")
            }
        }
    }
}