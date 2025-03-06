package com.example.nail

import android.graphics.Rect

// Class for holding the bounding box and the confidence for a particular prediction.
data class Prediction(val boundingBox : Rect, val confidence : Float ) {
}