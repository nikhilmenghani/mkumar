package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun OLTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onCommit: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
) {
    var hadFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange, // live updates
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onCommit?.invoke() },
            onDone = { onCommit?.invoke() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .onFocusChanged { fs ->
                if (hadFocus && !fs.isFocused) onCommit?.invoke()
                hadFocus = fs.isFocused
            }
    )
}