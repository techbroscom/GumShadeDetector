package com.techbros.myproject

import EyeDropper
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.techbros.myproject.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var capturedBitmap: Bitmap? = null
    private var isFlashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkCameraHardware()
        requestPermissionsIfNeeded()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        binding.apply {
            cameraCaptureButton.setOnClickListener { takePhoto() }
            retakeButton.setOnClickListener { retakePhoto() }
            flashButton.setOnClickListener { toggleFlash() }
            switchCameraButton.setOnClickListener { switchCamera() }
            uploadButton.setOnClickListener { openFileChooser() }


            // Initialize scale gesture detector for zoom functionality
            scaleGestureDetector = ScaleGestureDetector(this@MainActivity,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        camera?.let { camera ->
                            val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                            val delta = detector.scaleFactor
                            camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                        }
                        return true
                    }
                }
            )

            // Set up touch listener for zoom
            binding.viewFinder.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                true
            }

            // Set up EyeDropper for detecting color from the captured image
            val eyeDropper = object {
                fun notifyColorSelection(view: ImageView, event: MotionEvent) {
                    val bitmap = capturedBitmap ?: return

                    // Get image matrix values for coordinate transformation
                    val matrix = FloatArray(9)
                    view.imageMatrix.getValues(matrix)

                    // Calculate actual pixel coordinates in the bitmap
                    val scaleX = matrix[Matrix.MSCALE_X]
                    val scaleY = matrix[Matrix.MSCALE_Y]
                    val transX = matrix[Matrix.MTRANS_X]
                    val transY = matrix[Matrix.MTRANS_Y]

                    // Convert touch point to bitmap coordinates
                    val bitmapX = ((event.x - transX) / scaleX).toInt().coerceIn(0, bitmap.width - 1)
                    val bitmapY = ((event.y - transY) / scaleY).toInt().coerceIn(0, bitmap.height - 1)

                    try {
                        val pixelColor = bitmap.getPixel(bitmapX, bitmapY)
                        binding.colorPreview.setBackgroundColor(pixelColor)

                        val r = Color.red(pixelColor)
                        val g = Color.green(pixelColor)
                        val b = Color.blue(pixelColor)
                        val (l, a, bLab) = rgbToLab(r, g, b)
                        val labValues = "L: ${"%.2f".format(l)}, A: ${"%.2f".format(a)}, B: ${"%.2f".format(bLab)}"
                        binding.colorDetailsText.text = labValues
                        binding.colorNameText.text = "Selected Color"

                        // Update magnifier with the same coordinates
                        binding.magnifierView.updateMagnifier(bitmap, bitmapX.toFloat(), bitmapY.toFloat())
                    } catch (e: Exception) {
                        // Handle exceptions (e.g., out of bounds)
                        Log.e("MainActivity", "Error selecting color: ${e.message}")
                    }
                }
            }

            // Update touch listener
            binding.capturedImage.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    binding.magnifierView.visibility = View.VISIBLE
                    eyeDropper.notifyColorSelection(binding.capturedImage, event)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    binding.magnifierView.visibility = View.GONE
                }
                true
            }

            binding.proceedButton.setOnClickListener {
                capturedBitmap?.let { bitmap ->
                    val selectedColor = (binding.colorPreview.background as? ColorDrawable)?.color
                        ?: Color.WHITE // Default to white if no color is selected
                    val rgbValues = "R: ${Color.red(selectedColor)}, G: ${Color.green(selectedColor)}, B: ${Color.blue(selectedColor)}"
                    val r = Color.red(selectedColor)
                    val g = Color.green(selectedColor)
                    val b = Color.blue(selectedColor)
                    val (l, a, bLab) = rgbToLab(r, g, b)
                    val labValues = "L: ${"%.2f".format(l)}, A: ${"%.2f".format(a)}, B: ${"%.2f".format(bLab)}"
                    // Extract extras from the current intent
                    val extras = intent.extras

                    // Create the intent for ResultActivity
                    val resultIntent = Intent(this@MainActivity, ResultActivity::class.java).apply {
                        putExtra("SELECTED_COLOR", selectedColor)
                        putExtra("RGB_VALUES", rgbValues)
                        putExtra("LAB_VALUES", labValues)

                        // Pass previous activity's extras
                        extras?.let {
                            for (key in it.keySet()) {
                                val value = it.get(key)
                                when (value) {
                                    is String -> putExtra(key, value)
                                    else -> {
                                        // Handle other types if needed
                                    }
                                }
                            }
                        }
                    }
                    startActivity(resultIntent)
                } ?: Toast.makeText(this@MainActivity, "No color selected!", Toast.LENGTH_SHORT).show()
            }



        }
    }

    private fun checkCameraHardware() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "This device has no camera.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            withContext(Dispatchers.Main) {
                                handleCapturedImage(bitmap)
                            }
                        } catch (e: OutOfMemoryError) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    baseContext,
                                    "Failed to process image: insufficient memory",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        baseContext,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun handleCapturedImage(bitmap: Bitmap) {
        capturedBitmap = bitmap
        binding.apply {
            capturedImage.setImageBitmap(bitmap)
            viewFinder.visibility = View.GONE
            capturedImage.visibility = View.VISIBLE
            cameraCaptureButton.visibility = View.GONE
            retakeButton.visibility = View.VISIBLE
            flashButton.visibility = View.GONE
            switchCameraButton.visibility = View.GONE
            colorInfoLayout.visibility = View.VISIBLE
        }
        cameraProvider?.unbindAll()
    }

    private fun retakePhoto() {
        capturedBitmap = null
        binding.apply {
            viewFinder.visibility = View.VISIBLE
            capturedImage.visibility = View.GONE
            cameraCaptureButton.visibility = View.VISIBLE
            retakeButton.visibility = View.GONE
            flashButton.visibility = View.VISIBLE
            switchCameraButton.visibility = View.VISIBLE
            colorInfoLayout.visibility = View.GONE
        }
        startCamera()
    }

    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
        camera?.cameraControl?.enableTorch(isFlashEnabled)
        binding.flashButton.setImageResource(
            if (isFlashEnabled) R.drawable.baseline_flash_on_24 else R.drawable.baseline_flash_off_24
        )
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // Initialize camera controls
                camera?.cameraControl?.enableTorch(isFlashEnabled)

            } catch (exc: Exception) {
                Toast.makeText(
                    this,
                    "Failed to start camera: ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                lifecycleScope.launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }
                    bitmap?.let {
                        handleCapturedImage(it)
                    } ?: Toast.makeText(this@MainActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_CODE_PICK_IMAGE = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    fun rgbToLab(r: Int, g: Int, b: Int): Triple<Double, Double, Double> {
        // Normalize RGB values (0 - 1)
        val rNorm = r / 255.0
        val gNorm = g / 255.0
        val bNorm = b / 255.0

        // Convert RGB to XYZ
        val x = (rNorm * 0.4124564 + gNorm * 0.3575761 + bNorm * 0.1804375) / 0.95047
        val y = (rNorm * 0.2126729 + gNorm * 0.7151522 + bNorm * 0.0721750) / 1.00000
        val z = (rNorm * 0.0193339 + gNorm * 0.1191920 + bNorm * 0.9503041) / 1.08883

        // Convert XYZ to Lab
        fun f(t: Double): Double {
            return if (t > 0.008856) t.pow(1.0 / 3.0) else (7.787 * t) + (16.0 / 116.0)
        }

        val l = (116 * f(y)) - 16
        val a = 500 * (f(x) - f(y))
        val b = 200 * (f(y) - f(z))

        return Triple(l, a, b)
    }
}