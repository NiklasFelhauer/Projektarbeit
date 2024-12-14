package com.projektarbeit

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.projektarbeit.config.DELEGATE
import com.projektarbeit.config.MAX_RESULTS
import com.projektarbeit.config.MODEL
import com.projektarbeit.config.THRESHOLD
import com.projektarbeit.objectdetector.ObjectDetectorHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(private val objectDetectorHelper: ObjectDetectorHelper) : ViewModel() {
    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Initialisiere ObjectDetectorHelper mit den Konstanten aus Config.kt
                val objectDetectorHelper = ObjectDetectorHelper(
                    context = context,
                    threshold = THRESHOLD,
                    maxResults = MAX_RESULTS,
                    delegate = DELEGATE,
                    model = MODEL
                ).apply {
                    // Stelle den Object Detector mit den statischen Einstellungen ein
                    runBlocking {
                        setupObjectDetector()
                    }
                }
                return MainViewModel(objectDetectorHelper) as T
            }
        }
    }

    private var detectJob: Job? = null

    private val detectionResult =
        MutableStateFlow<ObjectDetectorHelper.DetectionResult?>(null).also {
            viewModelScope.launch {
                objectDetectorHelper.detectionResult.collect(it)
            }
        }

    private val errorMessage = MutableStateFlow<Throwable?>(null).also {
        viewModelScope.launch {
            objectDetectorHelper.error.collect(it)
        }
    }

    val uiState: StateFlow<UiState> = combine(
        detectionResult,
        errorMessage
    ) { result, error ->
        UiState(
            detectionResult = result,
            errorMessage = error?.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    /**
     *  Start detect object from an image.
     *  @param bitmap Tries to make a new bitmap based on the dimensions of this bitmap,
     *  @param rotationDegrees to correct the rotationDegrees during segmentation
     */
    fun detectImageObject(bitmap: Bitmap, rotationDegrees: Int) {
        detectJob = viewModelScope.launch {
            objectDetectorHelper.detect(bitmap, rotationDegrees)
        }
    }

    fun detectImageObject(imageProxy: ImageProxy) {
        detectJob = viewModelScope.launch {
            objectDetectorHelper.detect(imageProxy)
            imageProxy.close()
        }
    }

    /** Stop current detection */
    fun stopDetect() {
        viewModelScope.launch {
            detectionResult.emit(null)
            detectJob?.cancel()
        }
    }

    /** Clear error message after it has been consumed*/
    fun errorMessageShown() {
        errorMessage.update { null }
    }
}
