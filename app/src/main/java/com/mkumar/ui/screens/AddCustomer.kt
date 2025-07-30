package com.mkumar.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.data.ProductType
import com.mkumar.ui.components.chips.ProductChipRow
import com.mkumar.ui.components.forms.ProductFormSwitcher
import com.mkumar.ui.components.inputs.CustomerInfoSection
import com.mkumar.ui.components.selectors.ProductSelector
import com.mkumar.viewmodel.CustomerViewModel

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

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar("Product saved!")
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Customer") })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { customerViewModel.addNewProduct() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                    onAddClick = customerViewModel::addProduct
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

                Button(
                    onClick = { /* TODO: Generate Bill via GitHub */ },
                    enabled = formState.products.any { it.isSaved }
                ) {
                    Text("Generate Bill")
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }
}
