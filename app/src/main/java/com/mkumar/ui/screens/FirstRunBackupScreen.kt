package com.mkumar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mkumar.data.PreferencesManager
import com.mkumar.viewmodel.BackupUiState
import com.mkumar.viewmodel.BackupViewModel

@Composable
fun FirstRunBackupScreen(
    preferences: PreferencesManager,
    onStartFresh: () -> Unit,
    onRestored: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    var token by remember { mutableStateOf(preferences.githubPrefs.token) }
    var owner by remember { mutableStateOf(preferences.githubPrefs.githubOwner) }
    var repository by remember { mutableStateOf(preferences.githubPrefs.githubRepo) }
    val state by viewModel.state.collectAsState()
    val working = state is BackupUiState.Working

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Restore M Kumar", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter the token for your dedicated GitHub backup repository. Owner and repository are optional; the app can discover a repository containing the M Kumar manifest.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("GitHub token") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = owner,
            onValueChange = { owner = it },
            label = { Text("Repository owner (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = repository,
            onValueChange = { repository = it },
            label = { Text("Repository name (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = token.isNotBlank() && !working,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                preferences.githubPrefs.token = token.trim()
                preferences.githubPrefs.githubOwner = owner.trim()
                preferences.githubPrefs.githubRepo = repository.trim()
                viewModel.restoreLatest(onRestored)
            }
        ) {
            if (working) CircularProgressIndicator()
            else Text("Find and restore backup")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            enabled = !working,
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartFresh
        ) {
            Text("Start fresh")
        }
        val message = when (val current = state) {
            BackupUiState.Idle -> ""
            is BackupUiState.Working -> current.message
            is BackupUiState.Message -> current.message
            is BackupUiState.Error -> current.message
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, color = if (state is BackupUiState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        }
    }
}
