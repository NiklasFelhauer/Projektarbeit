package com.projektarbeit

import androidx.compose.runtime.Immutable
import com.projektarbeit.objectdetector.ObjectDetectorHelper

@Immutable
data class UiState(
    val detectionResult: ObjectDetectorHelper.DetectionResult? = null,
    val errorMessage: String? = null,
    val mqttData: Map<String, Float> = emptyMap() // Hinzugefügtes Feld
)
