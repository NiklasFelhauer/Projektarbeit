package com.projektarbeit.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projektarbeit.objectdetector.ObjectDetectorHelper
import com.projektarbeit.ui.Turquoise

@Composable
fun ResultsOverlay(
    modifier: Modifier = Modifier,
    result: ObjectDetectorHelper.DetectionResult,
    mqttData: Map<String, Float> // Hinzugefügtes Parameter
) {
    // Mapping von Objekten zu MQTT-Daten
    val labelToMqttKey = mapOf(
        "laptop" to "temp_hlt",
        "phone" to "temp_mlt_inside",
        "tablet" to "temp_mlt_outside",
        // Weitere Mappings hinzufügen...
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val detections = result.detections
        for (detection in detections) {
            val ratioBox = detection.boundingBox
            val boxWidth = ratioBox.width() * maxWidth.value
            val boxHeight = ratioBox.height() * maxHeight.value
            val boxLeftOffset = ratioBox.left * maxWidth.value
            val boxTopOffset = ratioBox.top * maxHeight.value

            // MQTT-Wert basierend auf dem erkannten Objekt
            val mqttKey = labelToMqttKey[detection.label]
            val mqttValue = mqttKey?.let { key -> mqttData[key] }

            Box(
                modifier = Modifier
                    .offset(x = boxLeftOffset.dp, y = boxTopOffset.dp)
                    .width(boxWidth.dp)
                    .height(boxHeight.dp)
                    .border(3.dp, Turquoise),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildString {
                        append("${detection.label} ")
                        append(String.format("%.1f", detection.score * 100))
                        mqttValue?.let {
                            append(" $mqttKey: $it")
                        }
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(5.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
