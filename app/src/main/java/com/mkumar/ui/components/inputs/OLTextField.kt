package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun OLTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onCommit: (String) -> Unit,
    onValueChange: (String) -> Unit = {}
) {
    var localValue by remember { mutableStateOf(value) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = localValue,
        onValueChange = {
            localValue = it
            onValueChange(it) // Optional: for live updates if needed
        },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .onFocusChanged {
            if (isFocused && !it.isFocused) {
                onCommit(localValue)
            }
            isFocused = it.isFocused
        }
    )
}
