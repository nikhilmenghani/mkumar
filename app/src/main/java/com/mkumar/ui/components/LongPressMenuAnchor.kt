package com.mkumar.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun LongPressMenuAnchor(
    onClick: () -> Unit,
    menuItems: List<ProMenuItem>,
    content: @Composable () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (menuExpanded) menuExpanded = false else onClick()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = true
                    }
                )
            }
    ) {

        // This box serves as the anchor, positioned at the top-right of the card
        Box(
            modifier = Modifier
                .align(Alignment.Center) // RIGHT SIDE
        ) {
            ProOverflowMenuIcons(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                items = menuItems,
                anchor = { /* No visible icon — invisible anchor */ }
            )
        }

        content()
    }
}
