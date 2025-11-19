package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.model.ProductType
import com.mkumar.ui.components.fabs.AddProductSpeedMenuButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    title: String,
    sheetContent: @Composable (contentPadding: PaddingValues) -> Unit,
    onDismiss: () -> Unit = {},
    showTitle: Boolean = true,
    showNext: Boolean = false,
    showPrevious: Boolean = false,
    showDone: Boolean = false,
    showDismiss: Boolean = false,
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    showAddProduct: Boolean = false,
    addProductCommonTypes: List<ProductType> = emptyList(),
    addProductLastUsed: ProductType? = null,
    onAddProductClick: (ProductType) -> Unit = {},
    onOpenProductPicker: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion { onDismiss() }
        },
        sheetState = bottomSheetState
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            bottomBar = {
                if (!imeVisible && (showNext || showPrevious || showDone || showDismiss || showAddProduct)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Previous (or spacer)
                        if (showPrevious) {
                            FloatingActionButton(
                                onClick = onPreviousClick,
                                modifier = Modifier.size(56.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous") }
                        }
//                        else {
                        // Empty space to balance the layout
                        // enable this when Show Previous button is in picture
//                            Spacer(Modifier.width(56.dp))
//                        }

                        // Right actions
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(), // take remaining width
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ){
                            if (showAddProduct) {
                                AddProductSpeedMenuButton(
                                    commonTypes = addProductCommonTypes,
                                    lastUsed = addProductLastUsed,
                                    onAddClick = onAddProductClick,
                                    onOpenPicker = onOpenProductPicker
                                )
                            }

                            if (showNext) {
                                FloatingActionButton(
                                    onClick = onNextClick,
                                    modifier = Modifier.size(56.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next") }
                            }
                            if (showDone) {
                                FloatingActionButton(
                                    onClick = {
                                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                            onDoneClick()
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.size(56.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) { Icon(Icons.Default.Check, contentDescription = "Done") }
                            }
                            if (showDismiss) {
                                FloatingActionButton(
                                    onClick = {
                                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                            onDismissClick()
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.size(56.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                if (showTitle) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
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

                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxWidth()
                ) {
                    sheetContent(PaddingValues())
                }
            }
        }
    }
}

@Preview(name = "Phone 320dp", widthDp = 320, heightDp = 640, showBackground = true, showSystemUi = true)
@Preview(name = "Phone 360dp", widthDp = 360, heightDp = 720, showBackground = true, showSystemUi = true)
@Preview(name = "Phone 480dp", widthDp = 480, heightDp = 720, showBackground = true, showSystemUi = true)
@Composable
private fun BaseBottomSheetPreview() {
    MaterialTheme {
        BaseBottomSheet(
            title = "Add product",
            showTitle = true,
            showPrevious = false,
            showNext = false,
            showDone = true,
            showDismiss = true,
            showAddProduct = true,
            sheetContent = { /* fake content */ padding ->
                Column(Modifier.padding(padding).fillMaxWidth()) {
                    repeat(6) { Text("Row $it", Modifier.padding(8.dp)) }
                }
            },
            onDismiss = {}
        )
    }
}
