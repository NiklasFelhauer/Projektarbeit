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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MainViewModel(private val objectDetectorHelper: ObjectDetectorHelper) : ViewModel() {
    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    val objectDetectorHelper = ObjectDetectorHelper(
                        context = context,
                        threshold = THRESHOLD,
                        maxResults = MAX_RESULTS,
                        delegate = DELEGATE,
                        model = MODEL
                    )
                    return MainViewModel(objectDetectorHelper) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private var detectJob: Job? = null

    private val detectionResult =
        MutableStateFlow<ObjectDetectorHelper.DetectionResult?>(null).also { flow ->
            viewModelScope.launch {
                objectDetectorHelper.detectionResult.collect { flow.value = it }
            }
        }

    private val errorMessage = MutableStateFlow<Throwable?>(null).also { flow ->
        viewModelScope.launch {
            objectDetectorHelper.error.collect { flow.value = it }
        }
    }

    // Neuer StateFlow für MQTT-Daten
    private val _mqttData = MutableStateFlow<Map<String, Float>>(emptyMap())
    val mqttData: StateFlow<Map<String, Float>> = _mqttData

    init {
        // Initialisieren Sie das ObjectDetectorHelper
        viewModelScope.launch {
            objectDetectorHelper.setupObjectDetector()
        }

        // MQTT-Daten sammeln und parsen
        viewModelScope.launch {
            MQTTManager.mqttMessageState.collect { message ->
                message?.let {
                    try {
                        val json = JSONObject(it)
                        val map = mutableMapOf<String, Float>()
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = json.getDouble(key).toFloat()
                            map[key] = value
                        }
                        _mqttData.value = map
                    } catch (e: JSONException) {
                        errorMessage.emit(e) // Korrigiert von _error.emit(e) zu errorMessage.emit(e)
                    }
                }
            }
        }
    }

    // Kombinierter UiState, der auch MQTT-Daten enthält
    val uiState: StateFlow<UiState> = combine(
        detectionResult,
        errorMessage,
        _mqttData
    ) { result, error, mqttData ->
        UiState(
            detectionResult = result,
            errorMessage = error?.message,
            mqttData = mqttData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

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

    fun stopDetect() {
        viewModelScope.launch {
            detectionResult.emit(null)
            detectJob?.cancel()
        }
    }

    fun errorMessageShown() {
        errorMessage.update { null }
    }
}
