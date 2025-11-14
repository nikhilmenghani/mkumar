package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShortBottomSheet(
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

    // 👇 detect if keyboard is visible
    val imeVisible = WindowInsets.isImeVisible

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion { onDismiss() }
        },
        sheetState = bottomSheetState
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                if (showTitle) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                val actionsHeight = 56.dp + 16.dp * 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (!imeVisible) actionsHeight else 0.dp)
                ) {
                    sheetContent()
                }
            }

            // 👇 Hide FAB row when IME is visible
            if (!imeVisible && (showNext || showPrevious || showDone || showDismiss)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showPrevious) {
                        FloatingActionButton(
                            onClick = onPreviousClick,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        }
                    } else {
                        Spacer(Modifier.width(56.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (showNext) {
                            FloatingActionButton(onClick = onNextClick, modifier = Modifier.size(56.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
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
