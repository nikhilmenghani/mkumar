package com.mkumar.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mkumar.App.Companion.globalClass
import com.mkumar.data.SingleText
import com.mkumar.ui.components.bottomsheets.ShortBottomSheet
import com.mkumar.ui.components.buttons.ClearButton
import kotlinx.coroutines.delay

@Composable
fun SingleTextDialog() {
    val dialog = globalClass.singleTextDialog
    if (dialog.show) {
        ShortBottomSheet(
            title = dialog.title,
            sheetContent = { SingleText(dialog) },
            onDismiss = dialog::dismiss
        )
    }
}

@Composable
fun SingleText(dialog: SingleText) {
    var textState by remember {
        mutableStateOf(
            TextFieldValue(dialog.text, selection = TextRange(dialog.text.length))
        )
    }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // Let the bottom sheet attach before requesting input focus.
        delay(150)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text(dialog.description) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            maxLines = 3,
            trailingIcon = {
                if (textState.text.isNotEmpty()) {
                    ClearButton { textState = TextFieldValue("") }
                }
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                dialog.onConfirm(textState.text)
                dialog.dismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Confirm")
        }
    }
}
