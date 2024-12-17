package com.projektarbeit.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projektarbeit.objectdetector.ObjectDetectorHelper
import com.projektarbeit.ui.Turquoise
import com.projektarbeit.SensorData

@Composable
fun ResultsOverlay(
    modifier: Modifier = Modifier,
    result: ObjectDetectorHelper.DetectionResult,
    sensorData: SensorData?
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
    ) {
        val detections = result.detections
        for (detection in detections) {
            val ratioBox = detection.boundingBox
            val boxWidth = ratioBox.width() * maxWidth.value
            val boxHeight = ratioBox.height() * maxHeight.value
            val boxLeftOffset = ratioBox.left * maxWidth.value
            val boxTopOffset = ratioBox.top * maxHeight.value

            // Box für die Bounding Box
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .offset(
                        x = boxLeftOffset.dp,
                        y = boxTopOffset.dp,
                    )
                    .width(boxWidth.dp)
                    .height(boxHeight.dp)
                    .border(3.dp, Turquoise), // Umrandung für die Bounding Box
                contentAlignment = Alignment.Center // Zentriert den Inhalt in der Bounding Box
            ) {
                // Text in der Mitte der Bounding Box
                Text(
                    text = "${detection.label} ${String.format("%.1f", detection.score)}",
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f)) // Transparenter Hintergrund
                        .padding(5.dp), // Polsterung um den Text
                    color = Color.White,
                    textAlign = TextAlign.Center // Zentriert den Textinhalt
                )
            }
        }

        // Anzeige der Sensor-Daten im oberen linken Bereich
        sensorData?.let {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(text = "Temp HLT: ${it.temp_hlt}°C", color = Color.White)
                Text(text = "Temp MLT Outside: ${it.temp_mlt_outside}°C", color = Color.White)
                Text(text = "Temp MLT Inside: ${it.temp_mlt_inside}°C", color = Color.White)
                // Füge weitere Felder hinzu, falls nötig
            }
        }

        // Optional: Anzeige des Verbindungsstatus im oberen rechten Bereich
        val connectionStatus by MQTTManager.connectionState.collectAsState()
        connectionStatus?.let {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        when {
                            it.contains("Fehler") || it.contains("Verbindung verloren") -> Color.Red.copy(alpha = 0.5f)
                            it.contains("Wieder verbunden") || it.contains("Verbunden") -> Color.Green.copy(alpha = 0.5f)
                            else -> Color.Gray.copy(alpha = 0.5f)
                        }
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
