package com.mkumar.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mkumar.model.SearchBy
import com.mkumar.model.SearchMode
import com.mkumar.model.SearchType
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.screens.RecentCustomersSection
import com.mkumar.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

// -------------------------------------------------------------------------------------
// Main Composable
// -------------------------------------------------------------------------------------

@Composable
fun SearchScreen(
    navController: NavHostController,
    vm: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit = { navController.popBackStack() },
    openCustomer: (String) -> Unit = { id -> navController.navigate("CustomerDetail/$id") }
) {
    val ui by vm.ui.collectAsState()
    var showAdvancedOptions by remember { mutableStateOf(false) }

    // Auto-focus
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        // Header
        SearchHeader(
            query = ui.query,
            isSearching = ui.isSearching,
            onBackClick = onBack,
            onQueryChange = vm::updateQuery,
            onStopClick = vm::stopSearch,
            onAdvancedToggle = { showAdvancedOptions = !showAdvancedOptions },
            focusRequester = focusRequester
        )

        // Advanced Options (Mode + SearchBy + SearchType)
        AnimatedVisibility(
            visible = showAdvancedOptions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SearchAdvancedOptions(
                mode = ui.mode,
                searchBy = ui.searchBy,
                searchType = ui.searchType,
                onModeChange = vm::updateMode,
                onSearchByChange = vm::updateSearchBy,
                onSearchTypeChange = vm::updateSearchType
            )
        }

        // Search progress UI
        AnimatedVisibility(
            visible = ui.isSearching,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SearchProgress()
        }

        // Results
        SearchResultsSection(
            results = ui.results,
            recent = ui.recent,
            isSearching = ui.isSearching,
            query = ui.query,
            onClear = vm::clearResults,
            openCustomer = openCustomer,
            onDismissRequest = onBack
        )
    }
}

// -------------------------------------------------------------------------------------
// Header
// -------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHeader(
    query: String,
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStopClick: () -> Unit,
    onAdvancedToggle: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            shape = CircleShape,
            placeholder = {
                Text(
                    text = "Search name or phone…",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        isSearching -> {
                            IconButton(onClick = onStopClick) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = "Stop search"
                                )
                            }
                        }

                        query.isNotEmpty() -> {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }

                    IconButton(onClick = onAdvancedToggle) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Advanced options"
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                // Search is already debounced on every change.
                onSearch = { /* no-op; search is automatic */ }
            )
        )
    }
}

// -------------------------------------------------------------------------------------
// Advanced Options
// -------------------------------------------------------------------------------------

@Composable
private fun SearchAdvancedOptions(
    mode: SearchMode,
    searchBy: SearchBy,
    searchType: SearchType,
    onModeChange: (SearchMode) -> Unit,
    onSearchByChange: (SearchBy) -> Unit,
    onSearchTypeChange: (SearchType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .padding(16.dp)
    ) {
        Text(
            "Advanced Options",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // Fast / Flexible
        ModeToggle(mode, onModeChange)

        Spacer(Modifier.height(16.dp))

        // Search By (Name / Phone / Invoice)
        Text(
            "Search by",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Row {
            SearchByChip("Name", SearchBy.NAME, searchBy, onSearchByChange)
            Spacer(Modifier.width(8.dp))
            SearchByChip("Phone", SearchBy.PHONE, searchBy, onSearchByChange)
            Spacer(Modifier.width(8.dp))
            SearchByChip("Invoice", SearchBy.INVOICE, searchBy, onSearchByChange)
        }

        // Search Type (Customers / Orders)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                "Return results:",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))

            Switch(
                checked = searchType == SearchType.ORDERS,
                onCheckedChange = { checked ->
                    val newType = if (checked) SearchType.ORDERS else SearchType.CUSTOMERS
                    onSearchTypeChange(newType)
                }
            )

            Spacer(Modifier.width(8.dp))

            Text(
                if (searchType == SearchType.ORDERS) "Orders" else "Customers",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SearchByChip(
    label: String,
    value: SearchBy,
    selected: SearchBy,
    onChange: (SearchBy) -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onChange(value) },
        color = if (selected == value)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected == value)
                MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModeToggle(mode: SearchMode, onChange: (SearchMode) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionChip(
            label = "Fast",
            selected = mode == SearchMode.QUICK,
            onClick = { onChange(SearchMode.QUICK) }
        )
        Spacer(Modifier.width(8.dp))
        OptionChip(
            label = "Flexible",
            selected = mode == SearchMode.FLEXIBLE,
            onClick = { onChange(SearchMode.FLEXIBLE) }
        )
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -------------------------------------------------------------------------------------
// Progress
// -------------------------------------------------------------------------------------

@Composable
private fun SearchProgress() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Searching…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// -------------------------------------------------------------------------------------
// Results
// -------------------------------------------------------------------------------------

@Composable
private fun SearchResultsSection(
    results: List<UiCustomerMini>,
    recent: List<UiCustomerMini>,
    isSearching: Boolean,
    query: String,
    onClear: () -> Unit,
    openCustomer: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        val headerTitle =
            if (query.isBlank()) "Results" else "Results (${results.size})"

        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                headerTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))

            if (query.isNotBlank() && results.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }

        // When query is blank → show recent customers (if any)
        if (query.isBlank()) {
            if (recent.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recent customers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                RecentCustomersSection(
                    customers = recent,
                    openCustomer = { id ->
                        openCustomer(id)
                    },
                    onDismissRequest = onDismissRequest
                )
            }
            return
        }

        // When query is non-blank but no results and not searching → empty state
        if (results.isEmpty() && !isSearching) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No matches found.")
            }
            return
        }

        // Results list
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            itemsIndexed(results, key = { _, it -> it.id }) { _, c ->
                SearchResultItem(
                    c = c,
                    onClick = {
                        openCustomer(c.id)     // navigate properly
                    }
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    c: UiCustomerMini,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                c.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                c.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
