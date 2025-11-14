package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun OLTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onCommit: (() -> Unit)? = null,
    mode: FieldMode = FieldMode.PlainText,
    placeholder: String? = null,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    imeActionOverride: ImeAction? = null,
    singleLine: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    var hadFocus by remember { mutableStateOf(false) }

    val imeAction = imeActionOverride ?: mode.defaultIme

    val actions = KeyboardActions(
        onNext = {
            val formatted = mode.formatOnCommit(value)
            if (formatted != value) onValueChange(formatted)
            onCommit?.invoke()

            if (onNext != null) onNext()
            else focusManager.moveFocus(FocusDirection.Next)
        },
        onDone = {
            val formatted = mode.formatOnCommit(value)
            if (formatted != value) onValueChange(formatted)
            onCommit?.invoke()

            if (onDone != null) onDone()
            else focusManager.clearFocus()
        }
    )

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(mode.sanitizeOnChange(it)) },
        label = { Text(label, maxLines = 1) },     // prevents stretching
        placeholder = { if (placeholder != null) Text(placeholder) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = mode.keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = actions,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TextFieldDefaults.MinHeight)  // FIX: stable height at 130–200%
            .padding(bottom = 8.dp)
            .onFocusChanged { fs ->
                if (hadFocus && !fs.isFocused) {
                    val formatted = mode.formatOnCommit(value)
                    if (formatted != value) onValueChange(formatted)
                    onCommit?.invoke()
                }
                hadFocus = fs.isFocused
            }
    )
}