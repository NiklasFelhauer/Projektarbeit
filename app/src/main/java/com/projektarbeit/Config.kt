package com.projektarbeit.config

import com.projektarbeit.objectdetector.ObjectDetectorHelper

// Globale Konstanten für die Konfiguration
const val THRESHOLD = 0.6f
const val MAX_RESULTS = 3
val MODEL = ObjectDetectorHelper.Model.EfficientDetLite2
val DELEGATE = ObjectDetectorHelper.Delegate.CPU
