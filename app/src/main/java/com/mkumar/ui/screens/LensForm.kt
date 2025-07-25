package com.mkumar.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.CustomerOrder
import com.mkumar.ui.components.buttons.ClearButton

@Composable
fun LensForm(customerOrder: CustomerOrder) {
    val customerInfo = customerOrder.customerInfo
    var name by remember { mutableStateOf(customerInfo.name) }
    var phone by remember { mutableStateOf(customerInfo.phoneNumber) }
    var email by remember { mutableStateOf(customerInfo.email) }
    Column {
        Text(text = "Lens Form")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            trailingIcon = {
                if (name.isNotEmpty()) {
                    ClearButton { name = "" }
                }
            },
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            trailingIcon = {
                if (phone.isNotEmpty()) {
                    ClearButton { phone = "" }
                }
            },
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            trailingIcon = {
                if (email.isNotEmpty()) {
                    ClearButton { email = "" }
                }
            },
        )
    }
}