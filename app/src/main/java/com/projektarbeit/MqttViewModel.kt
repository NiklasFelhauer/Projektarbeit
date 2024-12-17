package com.projektarbeit

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class MqttViewModel : ViewModel() {

    // Zugriff auf die StateFlows vom MQTTManager
    val mqttMessageState: StateFlow<String?> = MQTTManager.mqttMessageState
    val sensorDataState: StateFlow<SensorData?> = MQTTManager.sensorDataState
    val connectionState: StateFlow<String?> = MQTTManager.connectionState

    override fun onCleared() {
        super.onCleared()
        // Trenne die Verbindung nicht hier, damit sie global bleibt
    }
}
