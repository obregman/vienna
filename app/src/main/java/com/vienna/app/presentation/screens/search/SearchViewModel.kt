package com.vienna.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vienna.app.domain.model.SearchResult
import com.vienna.app.domain.repository.StockRepository
import com.vienna.app.domain.usecase.SearchStocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showRecentSearches: Boolean = true
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchStocksUseCase: SearchStocksUseCase,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        loadRecentSearches()
        setupSearchDebounce()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            stockRepository.getRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    private fun setupSearchDebounce() {
        _queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                showRecentSearches = query.isBlank(),
                error = null
            )
        }
        _queryFlow.value = query

        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchStocksUseCase(query)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Search failed"
                        )
                    }
                }
        }
    }

    fun onRecentSearchClick(query: String) {
        onQueryChanged(query)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            stockRepository.clearSearchHistory()
        }
    }

    fun clearQuery() {
        onQueryChanged("")
    }
}
