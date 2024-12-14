package com.projektarbeit

import androidx.compose.runtime.Immutable
import com.projektarbeit.objectdetector.ObjectDetectorHelper

@Immutable
class UiState(
    val detectionResult: ObjectDetectorHelper.DetectionResult? = null,
    val errorMessage: String? = null
)
