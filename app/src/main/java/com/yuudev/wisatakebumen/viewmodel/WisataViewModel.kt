package com.yuudev.wisatakebumen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.repository.WisataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WisataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WisataRepository(application)

    private val _wisataList = MutableStateFlow<List<Wisata>>(emptyList())
    val wisataList: StateFlow<List<Wisata>> = _wisataList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getWisataStream().collectLatest { list ->
                _wisataList.value = list
            }
        }
        refreshWisata()
    }

    fun refreshWisata(force: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshWisata(force)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = if (_wisataList.value.isNotEmpty()) {
                    "Menampilkan data terakhir yang tersimpan."
                } else {
                    e.localizedMessage ?: "Gagal mengambil data"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
