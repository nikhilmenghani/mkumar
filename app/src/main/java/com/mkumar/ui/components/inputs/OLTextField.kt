package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    var hadFocus by remember { mutableStateOf(false) }

    val ime = imeActionOverride ?: mode.defaultIme

    val textStyle = TextStyle(
        fontSize = 14.sp,                // smaller input text
        color = MaterialTheme.colorScheme.onSurface
    )

    val labelStyle = TextStyle(
        fontSize = 11.sp,                // compact floating label
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val placeholderStyle = TextStyle(
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.outline
    )

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
        onValueChange = { new ->
            onValueChange(mode.sanitizeOnChange(new))
        },
        textStyle = textStyle,
        label = { Text(label, style = labelStyle, maxLines = 1) },
        placeholder = {
            if (placeholder != null) {
                Text(placeholder, style = placeholderStyle, maxLines = 1)
            }
        },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = mode.keyboardType,
            imeAction = ime
        ),
        enabled = enabled,
        keyboardActions = actions,
        modifier = modifier
            .heightIn(min = 42.dp)          // compact height
            .padding(bottom = 4.dp)         // reduced spacing below
            .onFocusChanged { fs ->
                if (hadFocus && !fs.isFocused) {
                    val formatted = mode.formatOnCommit(value)
                    if (formatted != value) onValueChange(formatted)
                    onCommit?.invoke()
                }
                hadFocus = fs.isFocused
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Selection-aware variant for fields whose focus is requested programmatically.
 *
 * Keep the [TextFieldValue] at the call site so selection is changed only for the
 * explicit open/focus event. It must not be recreated from a String on every
 * keystroke, otherwise cursor jumps can cause fast input to be lost.
 */
@Composable
fun OLTextField(
    value: TextFieldValue,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
    mode: FieldMode = FieldMode.PlainText,
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val textStyle = TextStyle(
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    val labelStyle = TextStyle(
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val placeholderStyle = TextStyle(
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.outline
    )

    OutlinedTextField(
        value = value,
        onValueChange = { updated ->
            val sanitized = mode.sanitizeOnChange(updated.text)
            if (sanitized == updated.text) {
                onValueChange(updated)
            } else {
                val cursor = updated.selection.end.coerceAtMost(sanitized.length)
                onValueChange(TextFieldValue(sanitized, TextRange(cursor)))
            }
        },
        textStyle = textStyle,
        label = { Text(label, style = labelStyle, maxLines = 1) },
        placeholder = {
            if (placeholder != null) {
                Text(placeholder, style = placeholderStyle, maxLines = 1)
            }
        },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = mode.keyboardType,
            imeAction = mode.defaultIme
        ),
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 42.dp)
            .padding(bottom = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
