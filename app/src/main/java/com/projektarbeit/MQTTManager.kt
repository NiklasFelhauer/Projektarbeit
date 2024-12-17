package com.projektarbeit

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

@SuppressLint("StaticFieldLeak")
object MQTTManager {
    private const val TAG = "MQTTManager"
    private const val brokerUrl = "tcp://192.168.188.26:1883"
    private const val topic = "brauanlage/data"
    private const val PREFS_NAME = "mqtt_prefs"
    private const val PREF_CLIENT_ID = "client_id"

    private lateinit var context: Context

    // Initialisierungsmethode, die von der Application-Klasse aufgerufen wird
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    // Verwende eine konsistente clientId, die in SharedPreferences gespeichert wird
    private val clientId: String by lazy {
        getClientId()
    }

    private var mqttClient: MqttClient? = null

    // Separate StateFlows für Nachrichten, SensorData und Verbindungsstatus
    private val _mqttMessageState = MutableStateFlow<String?>(null)
    val mqttMessageState: StateFlow<String?> = _mqttMessageState

    private val _sensorDataState = MutableStateFlow<SensorData?>(null)
    val sensorDataState: StateFlow<SensorData?> = _sensorDataState

    private val _connectionState = MutableStateFlow<String?>(null)
    val connectionState: StateFlow<String?> = _connectionState

    private val gson = Gson()

    // Initialisierung der Verbindung
    fun connect() {
        try {
            if (mqttClient == null) {
                mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
                mqttClient?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        if (reconnect) {
                            Log.d(TAG, "Wieder verbunden mit: $serverURI")
                            _connectionState.value = "Wieder verbunden mit: $serverURI"
                            subscribeToTopic()
                        } else {
                            Log.d(TAG, "Verbunden mit: $serverURI")
                            _connectionState.value = "Verbunden mit: $serverURI"
                            subscribeToTopic()
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(TAG, "Verbindung verloren: ${cause?.message}")
                        _connectionState.value = "Verbindung verloren: ${cause?.message}"
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        message?.let {
                            val payload = String(it.payload)
                            Log.d(TAG, "Nachricht empfangen: $payload")
                            _mqttMessageState.value = payload
                            parseSensorData(payload)
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Optional: Aktionen bei abgeschlossener Lieferung
                    }
                })
            }

            if (!(mqttClient!!.isConnected)) {
                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = false
                    connectionTimeout = 10
                    keepAliveInterval = 60
                    // Falls Username/Password nötig sind:
                    // userName = "..."
                    // password = "....".toCharArray()
                }

                mqttClient?.connect(options)
                Log.d(TAG, "Verbindungsversuch mit Broker: $brokerUrl")
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Fehler beim Verbinden: ${e.message}", e)
            _connectionState.value = "Fehler beim Verbinden: ${e.message}"
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttClient?.subscribe(topic, 1)
            Log.d(TAG, "Abonniert: $topic")
            _connectionState.value = "Abonniert: $topic"
        } catch (e: MqttException) {
            Log.e(TAG, "Fehler beim Abonnieren: ${e.message}", e)
            _connectionState.value = "Fehler beim Abonnieren: ${e.message}"
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            mqttClient = null
            Log.d(TAG, "Verbindung getrennt")
            _connectionState.value = "Verbindung getrennt"
        } catch (e: MqttException) {
            Log.e(TAG, "Fehler beim Trennen der Verbindung: ${e.message}", e)
            _connectionState.value = "Fehler beim Trennen der Verbindung: ${e.message}"
        }
    }

    private fun getClientId(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var uniqueId = sharedPreferences.getString(PREF_CLIENT_ID, null)
        if (uniqueId == null) {
            uniqueId = "AndroidClient_${UUID.randomUUID()}"
            sharedPreferences.edit().putString(PREF_CLIENT_ID, uniqueId).apply()
        }
        return uniqueId
    }

    private fun parseSensorData(json: String) {
        try {
            val data = gson.fromJson(json, SensorData::class.java)
            _sensorDataState.value = data
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Fehler beim Parsen der MQTT-Daten: ${e.message}")
        }
    }
}
