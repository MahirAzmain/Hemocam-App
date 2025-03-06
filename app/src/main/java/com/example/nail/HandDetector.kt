package com.example.nail

import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect

class HandDetector(context: Context) {
    private val handDetectionModel = HandDetectionModel(context)

    // Predicts if the image contains a hand
    fun predict(image: Bitmap): Boolean {
        return try {
            val predictions = handDetectionModel.flow(image)
            predictions.isNotEmpty()
        } catch (e: Exception) {
            // Handle any errors that occur during prediction (e.g., model loading issues)
            e.printStackTrace()
            false
        }
    }

    // Returns the bounding boxes of detected hands in the image
    fun getBoundingBoxes(image: Bitmap): List<Rect> {
        return try {
            val predictions = handDetectionModel.flow(image)
            predictions.map { it.boundingBox }
        } catch (e: Exception) {
            // Handle any errors that occur during prediction
            e.printStackTrace()
            emptyList() // Return an empty list if there's an error
        }
    }

    // Resets the model, for example, clearing any cached data
    fun reset() {
        try {
            handDetectionModel.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
