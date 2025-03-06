
package com.example.nail

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import com.example.nail.HandDetector
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt

class ImageSelectionActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var openGalleryButton: Button
    private lateinit var goNextButton: Button
    private lateinit var boundingBoxView: BoundingBoxView
    private lateinit var sizeSlider: SeekBar
    private lateinit var resetButton: ImageButton
    private var selectedImageWidth: Int = -1
    private var selectedImageHeight: Int = -1


    private val cameraPermission = Manifest.permission.CAMERA
    private val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE

    private var currentPhotoPath: String? = null
    private var selectedImage: Bitmap? = null
    private lateinit var handDetector: HandDetector // Added Hand Detector
    private lateinit var detectedBoundingBoxes: List<Rect>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)

        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        openGalleryButton = findViewById(R.id.openGalleryButton)
        boundingBoxView = findViewById(R.id.boundingBoxView)
        sizeSlider = findViewById(R.id.sizeSlider)
        resetButton = findViewById(R.id.resetButton)
        goNextButton = findViewById(R.id.goNext)

        handDetector = HandDetector(this) // Initialize Hand Detector

        sizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                boundingBoxView.setBoxSize(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        resetButton.setOnClickListener {
            selectedImage?.let { image ->
                imageView.setImageBitmap(image)
            }
            sizeSlider.progress = 100
        }

        goNextButton.setOnClickListener {
            onClickGo()
        }

        takePhotoButton.setOnClickListener {
            if (checkPermission(cameraPermission)) {
                openCamera()
            } else {
                requestPermission(cameraPermission)
            }
        }


        openGalleryButton.setOnClickListener {
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }
    }
    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                100
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE ||
                    permissions[0] == Manifest.permission.READ_MEDIA_IMAGES
                ) {
                    openGallery()
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    Toast.makeText(
                        this,
                        "Gallery permission is required to select images.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Permission denied. Enable it in app settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
                imageBitmap?.let {
                    processSelectedImage(it)
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val inputStream = contentResolver.openInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    imageBitmap?.let { bmp ->
                        currentPhotoPath = getRealPathFromURI(it)
                        processSelectedImage(bmp)
                    }
                }
            }
        }


    private fun processSelectedImage(imageBitmap: Bitmap) {
        selectedImage = imageBitmap
        imageView.setImageBitmap(imageBitmap)

        // Check if the image is a hand
        val isHand = handDetector.predict(imageBitmap)
        if (!isHand) {
            Toast.makeText(
                this,
                "No hand detected! Please select another image.",
                Toast.LENGTH_SHORT
            ).show()
            imageView.setImageResource(R.drawable.placeholder) // Reset image
            selectedImage = null
            goNextButton.isEnabled = false
            return
        }

        // Fetch bounding boxes
        detectedBoundingBoxes = handDetector.getBoundingBoxes(imageBitmap)

        // Get the selected image width and height
        selectedImageWidth = imageBitmap.width
        selectedImageHeight = imageBitmap.height

        // ImageView width and height
        val imageViewWidth = imageView.width
        val imageViewHeight = imageView.height

        // Calculate the scaling factor and offset
        val scale: Float
        val offsetX: Float
        val offsetY: Float

        val imageAspectRatio = selectedImageWidth.toFloat() / selectedImageHeight.toFloat()
        val viewAspectRatio = imageViewWidth.toFloat() / imageViewHeight.toFloat()

        if (imageAspectRatio > viewAspectRatio) {
            // Image is wider than the view, so it's scaled to match the width
            scale = imageViewWidth.toFloat() / selectedImageWidth.toFloat()
            offsetX = 0f
            offsetY = (imageViewHeight - selectedImageHeight * scale) / 2f
        } else {
            // Image is taller than the view, so it's scaled to match the height
            scale = imageViewHeight.toFloat() / selectedImageHeight.toFloat()
            offsetX = (imageViewWidth - selectedImageWidth * scale) / 2f
            offsetY = 0f
        }

        // Update the BoundingBoxView
        boundingBoxView.resetPosition(
            offsetX,
            offsetY,
            selectedImageWidth * scale,
            selectedImageHeight * scale
        )

        // Adjust bounding boxes for scaling and offset
        val scaledBoundingBoxes = detectedBoundingBoxes.map { boundingBox ->
            val left = (boundingBox.left * scale + offsetX).toInt()
            val top = (boundingBox.top * scale + offsetY).toInt()
            val right = (boundingBox.right * scale + offsetX).toInt()
            val bottom = (boundingBox.bottom * scale + offsetY).toInt()

            Rect(left, top, right, bottom)
        }

        // Set the adjusted bounding boxes in the BoundingBoxView
        //boundingBoxView.setBoundingBoxes(scaledBoundingBoxes)

        // Enable the "Go" button
        goNextButton.isEnabled = true
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            currentPhotoPath = it.absolutePath
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            cameraLauncher.launch(cameraIntent)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = cacheDir
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }


    private fun onClickGo() {
        // Get the bounding boxes from the BoundingBoxView
        val boxes = boundingBoxView.getBoundingBoxes()

        // ImageView dimensions
        val imageViewWidth = imageView.width
        val imageViewHeight = imageView.height

        // Selected image dimensions
        val imageWidth = selectedImageWidth
        val imageHeight = selectedImageHeight

        // Calculate the scaling factor and offset (same as in onSelectImage)
        val scale: Float
        val offsetX: Float
        val offsetY: Float

        val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val viewAspectRatio = imageViewWidth.toFloat() / imageViewHeight.toFloat()

        if (imageAspectRatio > viewAspectRatio) {
            // Image is wider than the view, so it's scaled to match the width
            scale = imageViewWidth.toFloat() / imageWidth.toFloat()
            offsetX = 0f
            offsetY = (imageViewHeight - imageHeight * scale) / 2f
        } else {
            // Image is taller than the view, so it's scaled to match the height
            scale = imageViewHeight.toFloat() / imageHeight.toFloat()
            offsetX = (imageViewWidth - imageWidth * scale) / 2f
            offsetY = 0f
        }

        val segmentedImages = mutableListOf<BoundingBox>()

        // Adjust bounding boxes and check for validity
        for (box in boxes) {
            val left = (box.left - offsetX) / scale
            val top = (box.top - offsetY) / scale
            val right = (box.right - offsetX) / scale
            val bottom = (box.bottom - offsetY) / scale

            // Check if the bounding box is inside the image boundaries
            if (left < 0 || top < 0 || right > imageWidth || bottom > imageHeight) {
                Toast.makeText(this, "Bounding box is outside the image area!", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            // Check for invalid values (NaN or Infinity)
            if (left.isNaN() || top.isNaN() || right.isNaN() || bottom.isNaN() ||
                left.isInfinite() || top.isInfinite() || right.isInfinite() || bottom.isInfinite()
            ) {
                Toast.makeText(this, "Invalid bounding box coordinates!", Toast.LENGTH_SHORT).show()
                return
            }

            // Ensure valid box dimensions (left < right, top < bottom)
            if (left >= right || top >= bottom) {
                Toast.makeText(this, "Invalid bounding box dimensions!", Toast.LENGTH_SHORT).show()
                return
            }

            // Add the valid bounding box to the list
            segmentedImages.add(
                BoundingBox(
                    left.roundToInt(),
                    top.roundToInt(),
                    (right - left).roundToInt(),
                    (bottom - top).roundToInt()
                )
            )
        }

        // Log the bounding box dimensions for debugging
        for (box in segmentedImages) {
            println("BOX ${box.left} ${box.top} ${box.width} ${box.height}")
        }
        println("Image dimensions: $selectedImageWidth $selectedImageHeight")

        // Proceed to ResultsActivity with the segmented images
        val intent = Intent(this, ResultsActivity::class.java).apply {
            putParcelableArrayListExtra("segmentedImages", ArrayList(segmentedImages))
            putExtra("path", currentPhotoPath)
        }
        startActivity(intent)
    }
}
