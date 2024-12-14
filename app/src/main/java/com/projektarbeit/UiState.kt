package com.projektarbeit

import androidx.compose.runtime.Immutable
import com.projektarbeit.objectdetector.ObjectDetectorHelper

@Immutable
class UiState(
    val detectionResult: ObjectDetectorHelper.DetectionResult? = null,
    val setting: Setting = Setting(),
    val errorMessage: String? = null,
)

@Immutable
data class Setting(
    val model: ObjectDetectorHelper.Model = ObjectDetectorHelper.MODEL_DEFAULT,
    val delegate: ObjectDetectorHelper.Delegate = ObjectDetectorHelper.Delegate.CPU,
    val resultCount: Int = ObjectDetectorHelper.MAX_RESULTS_DEFAULT,
    val threshold: Float = ObjectDetectorHelper.THRESHOLD_DEFAULT,
)

