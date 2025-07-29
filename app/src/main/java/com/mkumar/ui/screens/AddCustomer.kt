package com.mkumar.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
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

    // Trigger snackbar when save completes
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION: Customer Info
            CustomerInfoSection(
                name = formState.name,
                phone = formState.phone,
                onNameChange = customerViewModel::updateCustomerName,
                onPhoneChange = customerViewModel::updateCustomerPhone
            )

            // SECTION: Product Type Selector
            ProductSelector(
                availableTypes = ProductType.allTypes,
                onAddClick = customerViewModel::addProduct
            )

            // SECTION: Product Chip Row
            ProductChipRow(
                products = formState.products,
                selectedId = formState.selectedProductId,
                onChipClick = customerViewModel::selectProduct,
                onChipDelete = customerViewModel::removeProduct
            )

            // SECTION: Dynamic Form (Placeholder for now)
            ProductFormSwitcher(
                selectedProduct = formState.products.find { it.id == formState.selectedProductId },
                onOwnerChange = { id, name -> customerViewModel.updateProductOwnerName(id, name) },
                onFormSave = { id, data ->
                    customerViewModel.saveProductFormData(id, data)
                    showSnackbar = true
                }
            )

            // SECTION: Generate Bill Button
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* TODO: GitHub integration */ },
                enabled = formState.products.any { it.isSaved }
            ) {
                Text("Generate Bill")
            }
        }
    }
}
