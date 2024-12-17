package com.projektarbeit

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialisiere den MQTTManager mit dem Application Context
        MQTTManager.initialize(this)
        // Initialisiere die MQTT-Verbindung
        MQTTManager.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Trenne die Verbindung, wenn die App beendet wird
        MQTTManager.disconnect()
    }
}
