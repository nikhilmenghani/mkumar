@file:Suppress("unused")
package com.mkumar.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

// ---------- Utils ----------

private fun Color.luminance(): Double {
    // sRGB -> linearized
    fun chan(v: Double): Double = if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
    val r = chan(red.toDouble())
    val g = chan(green.toDouble())
    val b = chan(blue.toDouble())
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}
private fun contrastRatio(a: Color, b: Color): Double {
    val l1 = a.luminance()
    val l2 = b.luminance()
    val light = max(l1, l2)
    val dark = min(l1, l2)
    return (light + 0.05) / (dark + 0.05)
}
private fun autoReadableOn(bg: Color): Color {
    val black = Color(0xFF000000)
    val white = Color(0xFFFFFFFF)
    return if (contrastRatio(black, bg) >= contrastRatio(white, bg)) black else white
}
private fun Color.toHex(): String = "#%08X".format(toArgb())

// ---------- Building blocks ----------

@Composable
private fun RoleSwatch(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val hasContrast = contrastRatio(fg, bg) >= 3.0 // WCAG AA-ish threshold for UI text
    val previewText = if (hasContrast) fg else autoReadableOn(bg)

    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, c.outline, MaterialTheme.shapes.medium)
            .background(bg)
            .padding(14.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = previewText)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, color = previewText, fontWeight = FontWeight.SemiBold)
                Text("bg ${bg.toHex()} • on ${fg.toHex()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = previewText.copy(alpha = 0.85f)
                )
                if (!hasContrast) {
                    Text("low contrast → preview uses ${previewText.toHex()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = previewText.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            AssistChip(
                onClick = {},
                label = { Text("Chip", color = previewText) },
                leadingIcon = { Icon(Icons.Filled.Check, null, tint = previewText) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = bg,
                    labelColor = previewText,
                    leadingIconContentColor = previewText
                ),
                border = BorderStroke(1.dp, c.outline)
            )
        }
    }
}

@Composable
private fun ExampleCombos() {
    val c = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Background with a standard surface card
        Surface(color = c.background, contentColor = c.onBackground, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Background + Card", color = c.onBackground, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Surface / onSurface", color = c.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Primary text on card body.", color = c.onSurface)
                        Text("Secondary text uses onSurfaceVariant.", color = c.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {}) { Text("Primary") }
                            FilledTonalButton(onClick = {}) { Text("Tonal") }
                            OutlinedButton(onClick = {}) { Text("Outline") }
                        }
                    }
                }
            }
        }

        // Tonal section (surfaceContainer) often used for grouped content
        Surface(
            color = c.surfaceContainerHigh,
            contentColor = c.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tonal section (surfaceContainerHigh)", fontWeight = FontWeight.SemiBold)
                Text("Use onSurfaceVariant for meta/secondary.", color = c.onSurfaceVariant)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = c.surfaceVariant,
                    color = c.primary
                )
            }
        }

        // Primary header / hero
        Surface(
            color = c.primaryContainer,
            contentColor = c.onPrimaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, null, tint = c.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Primary container header", fontWeight = FontWeight.Bold)
                    Text("Big sections, hero blocks, top bars.")
                }
                FilledTonalButton(
                    onClick = {},
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = c.primary,
                        contentColor = c.onPrimary
                    )
                ) { Text("CTA") }
            }
        }

        // Error banner
        Surface(
            color = c.errorContainer,
            contentColor = c.onErrorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Error, null)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Something went wrong", fontWeight = FontWeight.SemiBold)
                    Text("Explain the issue in one short line.")
                }
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = c.onErrorContainer)
                ) { Text("Details") }
            }
        }
    }
}

// ---------- The gallery ----------

@Composable
fun ColorRolesGalleryImproved(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme

    val groups: List<Pair<String, List<Pair<String, Pair<Color, Color>>>>> = listOf(
        "Foundations (app surfaces)" to listOf(
            "background / onBackground" to (c.background to c.onBackground),
            "surface / onSurface" to (c.surface to c.onSurface),
            "surfaceVariant / onSurfaceVariant" to (c.surfaceVariant to c.onSurfaceVariant),
            "inverseSurface / inverseOnSurface" to (c.inverseSurface to c.inverseOnSurface),
            "surfaceBright" to (c.surfaceBright to c.onSurface),
            "surfaceDim" to (c.surfaceDim to c.onSurface),
            "surfaceContainerLowest" to (c.surfaceContainerLowest to c.onSurface),
            "surfaceContainerLow" to (c.surfaceContainerLow to c.onSurface),
            "surfaceContainer" to (c.surfaceContainer to c.onSurface),
            "surfaceContainerHigh" to (c.surfaceContainerHigh to c.onSurface),
            "surfaceContainerHighest" to (c.surfaceContainerHighest to c.onSurface),
        ),
        "Brand accents" to listOf(
            "primary / onPrimary" to (c.primary to c.onPrimary),
            "primaryContainer / onPrimaryContainer" to (c.primaryContainer to c.onPrimaryContainer),
            "inversePrimary" to (c.inversePrimary to autoReadableOn(c.inversePrimary)),
            "secondary / onSecondary" to (c.secondary to c.onSecondary),
            "secondaryContainer / onSecondaryContainer" to (c.secondaryContainer to c.onSecondaryContainer),
            "tertiary / onTertiary" to (c.tertiary to c.onTertiary),
            "tertiaryContainer / onTertiaryContainer" to (c.tertiaryContainer to c.onTertiaryContainer),
        ),
        "Fixed accents (large surfaces)" to listOf(
            "primaryFixed / onPrimaryFixed" to (c.primaryFixed to c.onPrimaryFixed),
            "primaryFixedDim / onPrimaryFixedVariant" to (c.primaryFixedDim to c.onPrimaryFixedVariant),
            "secondaryFixed / onSecondaryFixed" to (c.secondaryFixed to c.onSecondaryFixed),
            "secondaryFixedDim / onSecondaryFixedVariant" to (c.secondaryFixedDim to c.onSecondaryFixedVariant),
            "tertiaryFixed / onTertiaryFixed" to (c.tertiaryFixed to c.onTertiaryFixed),
            "tertiaryFixedDim / onTertiaryFixedVariant" to (c.tertiaryFixedDim to c.onTertiaryFixedVariant),
        ),
        "Feedback & chrome" to listOf(
            "error / onError" to (c.error to c.onError),
            "errorContainer / onErrorContainer" to (c.errorContainer to c.onErrorContainer),
            "outline (as fg on surface)" to (c.surface to c.outline),
            "outlineVariant (as fg on surface)" to (c.surface to c.outlineVariant),
            "scrim (fg shown white)" to (c.scrim to Color.White.copy(alpha = 0.9f)),
        )
    )

    Surface(
        color = c.background,
        contentColor = c.onBackground,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Practical combinations up top
            item {
                Text("Practical Combinations", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                ExampleCombos()
            }

            groups.forEach { (section, roles) ->
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        section.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onSurfaceVariant
                    )
                }
                items(roles) { (label, pair) ->
                    val (bg, fg) = pair
                    RoleSwatch(label, bg, fg)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ---------- Previews ----------

@Preview(name = "Gallery • Light (Static)", showBackground = true)
@Composable
private fun PreviewGalleryLightStatic() {
    NikThemePreview(useDynamicColor = false) {
        ColorRolesGalleryImproved()
    }
}

@Preview(name = "Gallery • Light (Dynamic)", showBackground = true)
@Composable
private fun PreviewGalleryLightDynamic() {
    NikThemePreview(useDynamicColor = true) {
        ColorRolesGalleryImproved()
    }
}

@Preview(name = "Gallery • Dark (Static)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGalleryDarkStatic() {
    // Force dark by flipping system theme in Preview settings or rely on isSystemInDarkTheme()
    NikThemePreview(useDynamicColor = false) {
        ColorRolesGalleryImproved()
    }
}

@Preview(name = "Gallery • Dark (Dynamic)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGalleryDarkDynamic() {
    NikThemePreview(useDynamicColor = true) {
        ColorRolesGalleryImproved()
    }
}


