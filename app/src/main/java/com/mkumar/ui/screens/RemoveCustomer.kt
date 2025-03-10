package com.mkumar.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RemoveCustomer() {
    Column {
        Text(text = "Remove Customer")
        Spacer(modifier = Modifier.padding(8.dp))
    }
}