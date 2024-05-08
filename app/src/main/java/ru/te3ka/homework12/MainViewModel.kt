package ru.te3ka.homework12

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var searchInWork: Job? = null
    private val _searchQuery = MutableStateFlow("")
    var searchQuery: String
        get() = _searchQuery.value
        set(value) {
            _searchQuery.value = value
        }

    private val _isSearching = MutableLiveData<Boolean>()
    var isSearching: LiveData<Boolean> = _isSearching

    private val _searchResult = MutableLiveData<String?>()

    private val _noResultsText = MutableLiveData<String>()
    val noResultsText: LiveData<String>
        get() = _noResultsText

    private val _showNoResultsText = MutableLiveData<Boolean>()
    val showNoResultsText: LiveData<Boolean>
        get() = _showNoResultsText

    init {
        _isSearching.value = false
        _searchResult.value = null
        _noResultsText.value = ""
        _showNoResultsText.value = false

        _searchQuery
            .debounce(300)
            .onEach { query ->
                searchInWork?.cancel()
                searchInWork = viewModelScope.launch {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun performSearch(searchText: String) {
        if (searchText.length >= 3) {
            _isSearching.value = true
            viewModelScope.launch {
                delay(300)
                searchingProcess(searchText)
            }
        }
    }

    private fun searchingProcess(searchText: String) {
        viewModelScope.launch {
            Log.d("Searching process", "searching $searchText")
            delay(3_000)
            Log.d("Searching process", "$searchText not found")
            updateNoResultsText(searchText)
            _isSearching.value = false
            _searchResult.value = null
        }
    }

    private fun updateNoResultsText(searchText: String) {
        if (searchText.isEmpty()) {
            _noResultsText.value = ""
            _showNoResultsText.value = false
        } else {
            _noResultsText.value = "По запросу $searchText ничего не найдено"
            _showNoResultsText.value = true
        }
    }
}