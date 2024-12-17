package com.projektarbeit.home.MQTT

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projektarbeit.MqttViewModel

@Composable
fun MqttScreen(mqttViewModel: MqttViewModel = viewModel()) {
    // Beobachte die mqttMessageState, sensorDataState und connectionState vom ViewModel
    val message by mqttViewModel.mqttMessageState.collectAsState()
    val connectionStatus by mqttViewModel.connectionState.collectAsState()
    val sensorData by mqttViewModel.sensorDataState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Anzeige des Verbindungsstatus (optional)
        connectionStatus?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = if (it.contains("Fehler") || it.contains("Verbindung verloren")) Color.Red else Color.Gray
            )
        }

        // Anzeige der MQTT-Nachrichten
        message?.let {
            Text(
                text = it,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Anzeige der Sensor-Daten
        sensorData?.let {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Temp HLT: ${it.temp_hlt}°C", fontSize = 16.sp, color = Color.White)
                Text(text = "Temp MLT Outside: ${it.temp_mlt_outside}°C", fontSize = 16.sp, color = Color.White)
                Text(text = "Temp MLT Inside: ${it.temp_mlt_inside}°C", fontSize = 16.sp, color = Color.White)
                // Füge weitere Felder hinzu, falls nötig
            }
        }
    }
}
