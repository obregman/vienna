package com.vienna.app.presentation.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.presentation.components.EmptyState
import com.vienna.app.presentation.components.ErrorState
import com.vienna.app.presentation.components.SmallLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onStockClick: (symbol: String, name: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = viewModel::onQueryChanged,
                        onSearch = {},
                        expanded = true,
                        onExpandedChange = {},
                        placeholder = { Text("Search stocks...") },
                        leadingIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        trailingIcon = {
                            if (uiState.query.isNotBlank()) {
                                IconButton(onClick = viewModel::clearQuery) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            } else if (uiState.isLoading) {
                                SmallLoadingIndicator(modifier = Modifier.padding(12.dp))
                            }
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                },
                expanded = true,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    uiState.error != null -> {
                        ErrorState(
                            message = uiState.error ?: "Search failed",
                            onRetry = { viewModel.onQueryChanged(uiState.query) }
                        )
                    }
                    uiState.showRecentSearches -> {
                        RecentSearchesContent(
                            recentSearches = uiState.recentSearches,
                            onSearchClick = viewModel::onRecentSearchClick,
                            onClearHistory = viewModel::clearSearchHistory
                        )
                    }
                    uiState.results.isEmpty() && !uiState.isLoading && uiState.query.isNotBlank() -> {
                        EmptyState(
                            title = "No results",
                            message = "No stocks found for \"${uiState.query}\"",
                            icon = Icons.Default.Search
                        )
                    }
                    else -> {
                        SearchResultsList(
                            results = uiState.results,
                            onStockClick = onStockClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesContent(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    if (recentSearches.isEmpty()) {
        EmptyState(
            title = "Search for stocks",
            message = "Enter a ticker symbol or company name",
            icon = Icons.Default.Search
        )
    } else {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onClearHistory) {
                    Text("Clear")
                }
            }

            LazyColumn {
                items(recentSearches) { query ->
                    ListItem(
                        headlineContent = { Text(query) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.clickable { onSearchClick(query) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<SearchResult>,
    onStockClick: (symbol: String, name: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(results) { result ->
            ListItem(
                headlineContent = {
                    Text(
                        text = result.symbol,
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Text(
                        text = result.name,
                        maxLines = 1
                    )
                },
                trailingContent = {
                    Text(
                        text = result.region,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable {
                    onStockClick(result.symbol, result.name)
                }
            )
            HorizontalDivider()
        }
    }
}
