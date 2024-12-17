package com.projektarbeit

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class MqttViewModel : ViewModel() {

    // Zugriff auf die StateFlows vom MQTTManager
    val mqttMessageState: StateFlow<String?> = MQTTManager.mqttMessageState
    val connectionState: StateFlow<String?> = MQTTManager.connectionState

    init {
        // Verbinde beim Initialisieren des ViewModels
        MQTTManager.connect()
    }

    override fun onCleared() {
        super.onCleared()
        // Trenne die Verbindung nicht hier, damit sie global bleibt
        // Wenn du die Verbindung global halten möchtest, entferne die folgende Zeile:
        // MQTTManager.disconnect()
    }
}
