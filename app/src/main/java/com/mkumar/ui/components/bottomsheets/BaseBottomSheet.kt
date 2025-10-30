package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    title: String,
    sheetContent: @Composable () -> Unit,
    onDismiss: () -> Unit = {},
    showTitle: Boolean = true,
    showNext: Boolean = false,
    showPrevious: Boolean = false,
    showDone: Boolean = false,
    showDismiss: Boolean = false,
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                onDismiss()
            }
        },
        sheetState = bottomSheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            ) {
                if (showTitle) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Divider
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Dynamic Content
                // Content takes remaining space above FAB row
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxWidth()
                ) {
                    sheetContent()
                }
            }

            // Buttons Row with Left & Right Alignment
            if (showNext || showPrevious || showDone || showDismiss) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side (Previous Button)
                    if (showPrevious) {
                        FloatingActionButton(
                            onClick = { onPreviousClick() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(56.dp)) // Ensures alignment consistency
                    }

                    // Right Side (Next, Done, Dismiss)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (showNext) {
                            FloatingActionButton(
                                onClick = { onNextClick() },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next"
                                )
                            }
                        }

                        if (showDone) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                        onDoneClick()
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Done")
                            }
                        }

                        if (showDismiss) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                        onDismissClick()
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }
            }
        }
    }
}
