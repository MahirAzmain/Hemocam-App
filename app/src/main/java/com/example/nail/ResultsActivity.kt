package com.example.nail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.widget.Toast

class ResultsActivity : AppCompatActivity() {

    private val listBitmap = mutableListOf<Bitmap>()
    private val computationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val hbLevelText = findViewById<TextView>(R.id.hbLevelText)
        val avgHbLevelText = findViewById<TextView>(R.id.avgHbLevelText)
        val backButton = findViewById<Button>(R.id.backButton)
        val loader = findViewById<ProgressBar>(R.id.loader)

        loader.visibility = View.VISIBLE
        hbLevelText.visibility = View.GONE
        avgHbLevelText.visibility = View.GONE

        // Get the image path and segmented images from the intent
        val imagePath = intent.getStringExtra("path")
        val segmentedImages = intent.getParcelableArrayListExtra<BoundingBox>("segmentedImages")

        val bitmapMain = BitmapFactory.decodeFile(imagePath)

        segmentedImages?.forEachIndexed { index, box ->
            val bitmap = Bitmap.createBitmap(bitmapMain, box.left, box.top, box.width, box.height)
            listBitmap.add(bitmap)
            findViewById<ImageView>(resources.getIdentifier("segmentedImage${index + 1}", "id", packageName)).setImageBitmap(bitmap)
        }

        // Back button functionality
        backButton.setOnClickListener {
            finish()
        }

        computationScope.launch {
            val imagePath = intent.getStringExtra("path") ?: return@launch

            val isHand = detectHand(imagePath)

            withContext(Dispatchers.Main) {
                if (isHand == "error") {
                    Toast.makeText(this@ResultsActivity, "Hand detection failed! Try another image.", Toast.LENGTH_LONG).show()
                    finish()
                    return@withContext
                } else if (isHand == "non-hand") {
                    Toast.makeText(this@ResultsActivity, "No hand detected! Please retake the photo.", Toast.LENGTH_LONG).show()
                    finish()
                    return@withContext
                }
            }

            // Proceed with Hb Level Calculation if it's a hand
            val result = try {
                getHbLevels(
                    listBitmap[6],
                    listOf(listBitmap[0], listBitmap[1], listBitmap[2]),
                    listOf(listBitmap[3], listBitmap[4], listBitmap[5])
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (result == null) {
                withContext(Dispatchers.Main) {
                    hbLevelText.text = "Some Error Occurred"
                    avgHbLevelText.text = "Please try with another image"
                    loader.visibility = View.GONE
                    avgHbLevelText.visibility = View.VISIBLE
                    hbLevelText.visibility = View.VISIBLE
                }
                return@launch
            }

            val avg = (result[0] + result[1] + result[2]) / 3.0f

            // Update UI
            withContext(Dispatchers.Main) {
                hbLevelText.text = "Hb Levels : $result g/L"
                avgHbLevelText.text = "Average Hb Level: $avg g/L"
                loader.visibility = View.GONE
                avgHbLevelText.visibility = View.VISIBLE
                hbLevelText.visibility = View.VISIBLE
            }
        }
    }

    private fun bitmapToPng(bitmap: Bitmap, id: Int): String {
        try {
            val img: File = createImageFile(id)
            val stream = FileOutputStream(img)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            println("PNG SAVED : ${img.absolutePath}")
            return img.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun detectHand(imagePath: String): String {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val pyObj = py.getModule("hand_detection") // Ensure the Python file is named hand_detection.py

        // Get the model file path
        val modelPath = "${filesDir.absolutePath}/model.tflite"

        // Copy model if not present
        copyModelIfNeeded()

        return try {
            pyObj.callAttr("predict", imagePath, modelPath).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "error"
        }
    }


    // Helper method to copy the model file from assets to internal storage
    private fun copyModelIfNeeded() {
        try {
            val modelName = "model.tflite"
            val modelFile = File("${filesDir.absolutePath}/$modelName")

            // Only copy if the file doesn't exist yet
            if (!modelFile.exists()) {
                val inputStream = assets.open(modelName)
                val outputStream = FileOutputStream(modelFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                println("Model file copied successfully")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error copying model file: ${e.message}")
        }
    }

    private fun getHbLevels(whiteImg: Bitmap, skinImgs: List<Bitmap>, nailImgs: List<Bitmap>): List<Float> {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val pyObj = py.getModule("run")

        // Convert images to byte arrays
        val whiteImgBytes = bitmapToPng(whiteImg, 0)
        var c = 1
        val skinImgBytes = skinImgs.map { bitmapToPng(it, c++) }.toTypedArray()
        val nailImgBytes = nailImgs.map { bitmapToPng(it, c++) }.toTypedArray()

        // Call Python function
        val jsonResult = pyObj.callAttr("main", whiteImgBytes, skinImgBytes, nailImgBytes).toString()

        // Convert JSON string to List<Float>
        return jsonResult.removeSurrounding("[", "]").split(",").map { it.trim().toFloat() }
    }

    @Throws(IOException::class)
    private fun createImageFile(segmentId: Int): File {
        val storageDir: File = cacheDir // Use app's cache directory
        return File.createTempFile(
            "Segmented_${Int}",
            ".png",
            storageDir
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel background computations if activity is destroyed
        computationScope.cancel()
    }
}

