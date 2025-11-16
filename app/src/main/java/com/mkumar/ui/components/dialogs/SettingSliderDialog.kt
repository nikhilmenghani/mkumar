package com.mkumar.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.App.Companion.globalClass
import com.mkumar.ui.components.bottomsheets.ShortBottomSheet
import kotlin.math.roundToInt

@Composable
fun SliderDialog() {
    val dialog = globalClass.singleSliderDialog
    if (dialog.show) {
        var v by rememberSaveable { mutableStateOf(dialog.value) }
        ShortBottomSheet(
            title = dialog.title,
            sheetContent = {
                SettingSlider(
                    title = dialog.sliderTitle,
                    description = dialog.description,
                    value = v,
                    onValueChange = { v = it },
                    defaultValue = dialog.defaultProductHighlightIntensity,
                    range = 0..135,
                    step = 1,
                    unit = "%",
                    onConfirm = {
                        dialog.onConfirm(v)
                        dialog.dismiss()
                    },
                    onDismiss = dialog::dismiss
                )
            },
            onDismiss = dialog::dismiss,
        )
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    defaultValue: Int = 50,
    description: String? = null,
    range: IntRange = 0..100,
    step: Int = 1,
    unit: String? = "%",
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    val span = (range.last - range.first).coerceAtLeast(1)
    val stepCount = (span / step).coerceAtLeast(1)
    val materialSteps = (stepCount - 1).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .semantics {
                contentDescription = "$title slider, value $value ${unit.orEmpty()}"
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Current value label
            Text(
                text = buildString {
                    append(value)
                    if (!unit.isNullOrBlank()) {
                        append(' ')
                        append(unit)
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Reset button (appears only if changed)
            if (value != defaultValue) {
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        onValueChange(defaultValue)
                        onValueChangeFinished?.invoke()
                    },
                    enabled = enabled,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Reset")
                }
            }
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { raw ->
                val snapped = (raw / step).roundToInt() * step
                val coerced = snapped.coerceIn(range.first, range.last)
                onValueChange(coerced)
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = materialSteps.takeIf { step > 1 && it <= 100 } ?: 0,
            enabled = enabled,
            onValueChangeFinished = {
                onValueChangeFinished?.invoke()
                // Optional: haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Min/Max labels
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${range.first}${unit.orEmpty().let { if (it.isBlank()) "" else " $it" }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${range.last}${unit.orEmpty().let { if (it.isBlank()) "" else " $it" }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                onClick = onDismiss
            ) {
                Text(
                    "Dismiss",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(),
                onClick = onConfirm
            ) {
                Text(
                    "Confirm",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingSlider_Preview() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                var v by rememberSaveable { mutableStateOf(72) }
                SettingSlider(
                    title = "Invoice opacity",
                    description = "Controls the watermark/overlay intensity on the invoice.",
                    value = v,
                    onValueChange = { v = it },
                    defaultValue = 80,
                    range = 0..100,
                    step = 1,
                    unit = "%"
                )
            }
        }
    }
}