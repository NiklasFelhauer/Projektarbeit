package com.projektarbeit.home.MQTT

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projektarbeit.MqttViewModel

@Composable
fun MqttScreen(mqttViewModel: MqttViewModel = viewModel()) {
    // Beobachte die mqttMessageState und connectionState vom ViewModel
    val message by mqttViewModel.mqttMessageState.collectAsState()
    val connectionStatus by mqttViewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Optional: Anzeige des Verbindungsstatus, wenn gewünscht
        connectionStatus?.let {
            Text(text = it, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
        }

        // Anzeige der MQTT-Nachrichten
        message?.let {
            Text(text = it, fontSize = 20.sp)
        }
    }
}
