package com.mkumar.ui.screens.customer.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.*


@Composable
fun CustomerOrderBottomSheet(
    state: NewOrderUi,
    onIntent: (NewOrderIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("New Sale", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))


        ProductTypePicker(selected = state.selectedType, onSelected = { onIntent(NewOrderIntent.SelectType(it)) })


        Spacer(Modifier.height(8.dp))


        ProductAccordion(
            title = "Lens",
            expanded = state.selectedType == ProductType.LENS,
            onToggle = { onIntent(NewOrderIntent.SelectType(ProductType.LENS)) },
            fixedCollapsedHeight = 72.dp
        ) {
            LensForm(state = state.lens, onChange = { onIntent(NewOrderIntent.LensChanged(it)) })
        }


        ProductAccordion(
            title = "Frame",
            expanded = state.selectedType == ProductType.FRAME,
            onToggle = { onIntent(NewOrderIntent.SelectType(ProductType.FRAME)) },
            fixedCollapsedHeight = 72.dp
        ) {
            FrameForm(state = state.frame, onChange = { onIntent(NewOrderIntent.FrameChanged(it)) })
        }


        ProductAccordion(
            title = "Contact Lens",
            expanded = state.selectedType == ProductType.CONTACT_LENS,
            onToggle = { onIntent(NewOrderIntent.SelectType(ProductType.CONTACT_LENS)) },
            fixedCollapsedHeight = 72.dp
        ) {
            ContactLensForm(state = state.contactLens, onChange = { onIntent(NewOrderIntent.ContactLensChanged(it)) })
        }


        Spacer(Modifier.height(16.dp))
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSave && !state.saving,
            onClick = { onIntent(NewOrderIntent.Save) }
        ) { Text(if (state.saving) "Saving…" else "Save Order") }


        Spacer(Modifier.height(24.dp))
    }
}