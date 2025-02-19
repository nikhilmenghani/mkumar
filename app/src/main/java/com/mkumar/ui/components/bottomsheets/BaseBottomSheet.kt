package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    title: String,
    sheetContent: @Composable () -> Unit,
    onDismiss: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
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

            // Dynamic Content
            sheetContent()

            // Buttons Row with Proper Spacing
            if (showNext || showPrevious || showDone || showDismiss) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (showPrevious) {
                        FloatingActionButton(
                            onClick = { onPreviousClick() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        }
                        Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
                    }

                    if (showNext) {
                        FloatingActionButton(
                            onClick = { onNextClick() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                        Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
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
                        Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
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
