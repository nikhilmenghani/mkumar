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
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData

@Composable
fun FrameForm(
    initialData: ProductFormData.FrameData? = null,
    onSave: (ProductFormData.FrameData) -> Unit
) {
    var brand by remember { mutableStateOf(initialData?.brand.orEmpty()) }
    var color by remember { mutableStateOf(initialData?.color.orEmpty()) }
    var size by remember { mutableStateOf(initialData?.size.orEmpty()) }

    Column {
        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("Size") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(onClick = { onSave(ProductFormData.FrameData(brand, color, size)) }) {
            Text("Save Frame")
        }
    }
}

@Composable
fun LensForm(
    initialData: ProductFormData.LensData? = null,
    onSave: (ProductFormData.LensData) -> Unit
) {
    var leftSphere by remember { mutableStateOf(initialData?.leftSphere.orEmpty()) }
    var leftAxis by remember { mutableStateOf(initialData?.leftAxis.orEmpty()) }
    var rightSphere by remember { mutableStateOf(initialData?.rightSphere.orEmpty()) }
    var rightAxis by remember { mutableStateOf(initialData?.rightAxis.orEmpty()) }

    Column {
        OutlinedTextField(
            value = leftSphere,
            onValueChange = { leftSphere = it },
            label = { Text("Left Sphere") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = leftAxis,
            onValueChange = { leftAxis = it },
            label = { Text("Left Axis") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = rightSphere,
            onValueChange = { rightSphere = it },
            label = { Text("Right Sphere") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = rightAxis,
            onValueChange = { rightAxis = it },
            label = { Text("Right Axis") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(onClick = {
            onSave(
                ProductFormData.LensData(
                    leftSphere, leftAxis, rightSphere, rightAxis
                )
            )
        }) {
            Text("Save Lens")
        }
    }
}

@Composable
fun ContactLensForm(
    initialData: ProductFormData.ContactLensData? = null,
    onSave: (ProductFormData.ContactLensData) -> Unit
) {
    var power by remember { mutableStateOf(initialData?.power.orEmpty()) }
    var duration by remember { mutableStateOf(initialData?.duration.orEmpty()) }

    Column {
        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("Power") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(onClick = { onSave(ProductFormData.ContactLensData(power, duration)) }) {
            Text("Save Contact Lens")
        }
    }
}
