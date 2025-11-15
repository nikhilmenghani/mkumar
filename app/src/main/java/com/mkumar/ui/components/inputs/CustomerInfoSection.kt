package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun CustomerInfoSection(
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {

        OLTextField(
            value = name,
            label = "Customer Name",
            mode = FieldMode.TitleCase(),
            onValueChange = onNameChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OLTextField(
            value = phone,
            label = "Phone Number",
            mode = FieldMode.Phone(prefixCountryCode = false),
            onValueChange = onPhoneChange,
            imeActionOverride = ImeAction.Done,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            onDone = onSubmit
        )
    }
}
