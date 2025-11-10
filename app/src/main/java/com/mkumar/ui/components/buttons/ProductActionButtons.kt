package com.mkumar.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData

@Composable
fun ProductActionButtons(
    productId: String,
    draft: ProductFormData,
    onDelete: (String) -> Unit,
    onSave: (String, ProductFormData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        val buttonShape = RoundedCornerShape(8.dp)

        OutlinedButton(
            onClick = { onDelete(productId) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
                containerColor = Color.Transparent
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(MaterialTheme.colorScheme.error)
            ),
            shape = buttonShape
        ) {
            Text("Delete")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = { onSave(productId, draft) },
            shape = buttonShape
        ) {
            Text("Save Item")
        }
    }
}