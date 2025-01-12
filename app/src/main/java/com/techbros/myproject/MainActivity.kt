package com.techbros.myproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
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
            val eyeDropper = EyeDropper(capturedImage, object : EyeDropper.ColorSelectionListener {
                override fun onColorSelected(color: Int) {
                    colorPreview.setBackgroundColor(color)
                    val rgbValues = "R: ${Color.red(color)}, G: ${Color.green(color)}, B: ${Color.blue(color)}"
                    colorDetailsText.text = rgbValues
                    colorNameText.text = "Selected Color"
                }
            })

            capturedImage.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    eyeDropper.notifyColorSelection(event.x.toInt(), event.y.toInt())
                }
                true
            }

            binding.proceedButton.setOnClickListener {
                capturedBitmap?.let { bitmap ->
                    val selectedColor = (binding.colorPreview.background as? ColorDrawable)?.color
                        ?: Color.WHITE // Default to white if no color is selected
                    val rgbValues = "R: ${Color.red(selectedColor)}, G: ${Color.green(selectedColor)}, B: ${Color.blue(selectedColor)}"

                    // Extract extras from the current intent
                    val extras = intent.extras

                    // Create the intent for ResultActivity
                    val resultIntent = Intent(this@MainActivity, ResultActivity::class.java).apply {
                        putExtra("SELECTED_COLOR", selectedColor)
                        putExtra("RGB_VALUES", rgbValues)

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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}