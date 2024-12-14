package com.projektarbeit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttViewModel : ViewModel() {
    // MQTT Broker URL und Topic
    private val brokerUrl = "tcp://192.168.188.26:1883" // Beispiel: lokaler Broker
    private val topic = "brauanlage/data"

    // MQTT Client ID
    private val clientId = "AndroidClient_" + System.currentTimeMillis()

    private var mqttClient: MqttClient? = null

    private val _mqttMessageState = MutableStateFlow("Noch keine Daten empfangen")
    val mqttMessageState: StateFlow<String> = _mqttMessageState

    fun connectAndSubscribe() {
        viewModelScope.launch {
            try {
                // MQTT Client initialisieren
                mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = true
                    connectionTimeout = 10
                    // Falls Username/Password nötig sind, hier:
                    // userName = "..."
                    // password = "....".toCharArray()
                }

                // Callback setzen, um Nachrichten zu empfangen:
                mqttClient?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        if (reconnect) {
                            updateMessage("Wieder verbunden mit: $serverURI")
                        } else {
                            updateMessage("Verbunden mit: $serverURI")
                        }
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        message?.let {
                            val payload = String(it.payload)
                            updateMessage("Nachricht empfangen: $payload")
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        updateMessage("Verbindung verloren: ${cause?.message}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Wird aufgerufen wenn eine gesendete Nachricht bestätigt wird
                    }
                })

                // Verbinden
                mqttClient?.connect(options)
                if (mqttClient?.isConnected == true) {
                    updateMessage("Erfolgreich mit Broker verbunden. Topic wird abonniert...")
                    mqttClient?.subscribe(topic, 1) // QoS = 1
                    updateMessage("Abonniert: $topic")
                } else {
                    updateMessage("Konnte nicht verbinden.")
                }

            } catch (e: Exception) {
                updateMessage("Fehler beim Verbinden: ${e.message}")
            }
        }
    }

    private fun updateMessage(newMessage: String) {
        viewModelScope.launch {
            _mqttMessageState.value = newMessage
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {
            // Ignorieren oder loggen
        }
    }
}
