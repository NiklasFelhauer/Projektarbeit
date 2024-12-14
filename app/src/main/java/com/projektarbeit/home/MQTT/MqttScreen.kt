// Datei: MqttScreen.kt
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
    // Starte die Verbindung und das Abonnieren, wenn der Bildschirm angezeigt wird
    LaunchedEffect(Unit) {
        mqttViewModel.connectAndSubscribe()
    }

    // Beobachte die mqttMessageState vom ViewModel
    val message by mqttViewModel.mqttMessageState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, fontSize = 20.sp)
    }
}
