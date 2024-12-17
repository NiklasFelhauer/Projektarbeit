package com.projektarbeit

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialisiere die MQTT-Verbindung beim Start der App
        MQTTManager.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Trenne die Verbindung, wenn die App beendet wird
        MQTTManager.disconnect()
    }
}
