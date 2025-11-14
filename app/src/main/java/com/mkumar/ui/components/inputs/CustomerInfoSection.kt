package com.mkumar.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomerInfoSection(
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

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
            imeActionOverride = null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
