package com.mkumar.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.data.ProductType
import com.mkumar.ui.components.chips.ProductChipRow
import com.mkumar.ui.components.forms.ProductFormSwitcher
import com.mkumar.ui.components.inputs.CustomerInfoSection
import com.mkumar.ui.components.selectors.ProductSelector
import com.mkumar.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomer(
    navController: NavHostController,
    customerViewModel: CustomerViewModel
) {
    val formState by customerViewModel.formState.collectAsStateWithLifecycle()
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedProductType = remember { mutableStateOf<ProductType?>(null) }

    // JSON dialog state
    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonPreview by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar("Product saved!")
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Customer") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Save FAB
                FloatingActionButton(
                    onClick = {
                        // include unsaved edits in preview so it matches what's on screen
                        jsonPreview = customerViewModel.serializeToJson(includeUnsavedEdits = true)
                        showJsonDialog = true
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save / Preview JSON")
                }

                // Existing Add Product FAB
                FloatingActionButton(
                    onClick = {
                        selectedProductType.value?.let { type ->
                            customerViewModel.addProduct(type)
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomerInfoSection(
                    name = formState.name,
                    phone = formState.phone,
                    onNameChange = customerViewModel::updateCustomerName,
                    onPhoneChange = customerViewModel::updateCustomerPhone
                )

                ProductSelector(
                    availableTypes = ProductType.allTypes,
                    selectedType = selectedProductType.value,
                    onTypeSelected = { selectedProductType.value = it }
                )

                ProductChipRow(
                    products = formState.products,
                    selectedId = formState.selectedProductId,
                    onChipClick = customerViewModel::openForm,
                    onChipDelete = customerViewModel::removeProduct,
                    getCurrentBuffer = customerViewModel::getEditingProductData,
                    hasUnsavedChanges = customerViewModel::hasUnsavedChanges
                )

                ProductFormSwitcher(
                    selectedProduct = formState.products.find { it.id == formState.selectedProductId },
                    openForms = customerViewModel.openForms,
                    getEditingBuffer = customerViewModel::getEditingProductData,
                    updateEditingBuffer = customerViewModel::updateEditingBuffer,
                    onOwnerChange = customerViewModel::updateProductOwnerName,
                    hasUnsavedChanges = customerViewModel::hasUnsavedChanges,
                    onFormSave = { id, data ->
                        customerViewModel.saveProductFormData(id, data)
                        showSnackbar = true
                    }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Bottom gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                        )
                    )
            )
        }
    }

    // JSON Preview Dialog
    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = { showJsonDialog = false },
            title = { Text("Form JSON") },
            text = {
                val scroll = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 420.dp)
                        .verticalScroll(scroll)
                ) {
                    SelectionContainer {
                        Text(
                            text = jsonPreview,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showJsonDialog = false }) { Text("Close") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("MKumar JSON", jsonPreview))
                    scope.launch { snackbarHostState.showSnackbar("JSON copied") }
                }) {
                    Text("Copy")
                }
            }
        )
    }
}
