package com.yuudev.wisatakebumen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yuudev.wisatakebumen.model.Review
import com.yuudev.wisatakebumen.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReviewRepository(application)
    
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentWisataId: String? = null

    fun loadReviews(wisataId: String, force: Boolean = false) {
        if (currentWisataId != wisataId) {
            currentWisataId = wisataId
            viewModelScope.launch {
                repository.getReviewsStream(wisataId).collectLatest { list ->
                    _reviews.value = list
                }
            }
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshReviews(wisataId, force)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = if (_reviews.value.isNotEmpty()) {
                    "Menampilkan ulasan terakhir yang tersimpan."
                } else {
                    e.localizedMessage ?: "Gagal mengambil ulasan"
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
