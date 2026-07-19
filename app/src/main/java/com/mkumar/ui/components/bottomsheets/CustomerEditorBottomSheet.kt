package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CustomerEditorFocus { NAME, PHONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditorBottomSheet(
    isEditing: Boolean,
    name: String,
    phone: String,
    initialFocus: CustomerEditorFocus,
    canSubmit: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val nameFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }

    fun submit() {
        if (!canSubmit) return
        keyboard?.hide()
        scope.launch {
            sheetState.hide()
            onSubmit()
        }
    }

    LaunchedEffect(initialFocus) {
        // Wait until the sheet has entered and its text fields are attached.
        delay(250)
        when (initialFocus) {
            CustomerEditorFocus.NAME -> nameFocus.requestFocus()
            CustomerEditorFocus.PHONE -> phoneFocus.requestFocus()
        }
        keyboard?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditing) "Edit customer" else "New customer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isEditing) "Update the customer details below."
                        else "Complete the missing detail to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledIconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OLTextField(
                    value = name,
                    label = "Customer name",
                    placeholder = "Enter full name",
                    mode = FieldMode.TitleCase(),
                    onValueChange = onNameChange,
                    onNext = { phoneFocus.requestFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus)
                )
                OLTextField(
                    value = phone,
                    label = "Phone number",
                    placeholder = "10-digit phone number",
                    mode = FieldMode.Phone(prefixCountryCode = false),
                    onValueChange = onPhoneChange,
                    imeActionOverride = ImeAction.Done,
                    onDone = ::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(phoneFocus)
                )
            }

            Button(
                onClick = ::submit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(
                    text = if (isEditing) "Save changes" else "Add customer",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
