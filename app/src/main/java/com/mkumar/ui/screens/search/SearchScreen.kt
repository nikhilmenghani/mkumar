package com.mkumar.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mkumar.repository.impl.SearchMode
import com.mkumar.repository.impl.UiCustomerMini
import com.mkumar.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    vm: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit = { navController.popBackStack() },
    openCustomer: (String) -> Unit = { id -> navController.navigate("CustomerDetail/$id") }
) {
    val state by vm.ui.collectAsState()

// Autofocus + show keyboard on open
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
// Request focus after composition; IME usually opens automatically
        focusRequester.requestFocus()
// Explicitly ask keyboard to show for reliability
        keyboard?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
// Mode toggle
            ModeToggle(mode = state.mode, onChange = vm::updateMode)
// Search field
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::updateQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                placeholder = { Text("Search name or phone…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.updateQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    capitalization = KeyboardCapitalization.Words
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboard?.hide() }
                )
            )


            if (state.isSearching) LinearProgressIndicator(Modifier.fillMaxWidth())

            when {
                state.query.isNotBlank() && state.results.isEmpty() && !state.isSearching -> EmptyState()
                else -> ResultList(items = state.results, onClick = openCustomer)
            }
        }
    }
}

@Composable
private fun ModeToggle(mode: SearchMode, onChange: (SearchMode) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AssistChip(
            onClick = { onChange(SearchMode.QUICK) },
            label = { Text("Fast") },
            leadingIcon = { Text("⚡") },
            enabled = mode != SearchMode.QUICK
        )
        Spacer(Modifier.width(8.dp))
        AssistChip(
            onClick = { onChange(SearchMode.FLEXIBLE) },
            label = { Text("Flexible") },
            leadingIcon = { Text("🔎") },
            enabled = mode != SearchMode.FLEXIBLE
        )
        Spacer(Modifier.weight(1f))
        val hint = if (mode == SearchMode.QUICK) "Fast -> Starts with" else "Flexible -> Contains"
        Text(hint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No matches. Try a different query.")
    }
}


@Composable
private fun ResultList(items: List<UiCustomerMini>, onClick: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { c -> CustomerRow(c, onClick) }
    }
}


@Composable
private fun CustomerRow(c: UiCustomerMini, onClick: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick(c.id) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(c.name, style = MaterialTheme.typography.titleMedium)
            Text(c.phone ?: "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}