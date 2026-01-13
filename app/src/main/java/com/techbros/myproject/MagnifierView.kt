package com.techbros.myproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

class MagnifierView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var bitmapX = 0
    private var bitmapY = 0
    private var touchX = 0f
    private var touchY = 0f
    private var isVisible = false // Track if magnifier should be visible
    private val magnification = 3f
    private val radius = 150f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 2f
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = 60
        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }

    fun updateMagnifier(bitmap: Bitmap, x: Float, y: Float) {
        this.bitmap = bitmap
        this.bitmapX = x.toInt()
        this.bitmapY = y.toInt()

        this.touchX = x
        this.touchY = y
        this.isVisible = true // Show magnifier

        invalidate()
    }

    fun hideMagnifier() {
        isVisible = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isVisible) return

        bitmap?.let { bmp ->
            try {
                val halfSrcSize = (radius / magnification).toInt()

                val srcX = (bitmapX - halfSrcSize).coerceIn(0, bmp.width - 1)
                val srcY = (bitmapY - halfSrcSize).coerceIn(0, bmp.height - 1)

                val srcWidth = (2 * halfSrcSize).coerceAtMost(bmp.width - srcX)
                val srcHeight = (2 * halfSrcSize).coerceAtMost(bmp.height - srcY)

                // Adjust position to avoid cutting off at edges
                val magnifierX = touchX.coerceIn(radius, width - radius)
                val magnifierY = touchY.coerceIn(radius, height - radius)

                // Draw shadow effect
                canvas.drawCircle(magnifierX, magnifierY, radius + 5, shadowPaint)

                // Define source and destination rectangles
                val srcRect = Rect(srcX, srcY, srcX + srcWidth, srcY + srcHeight)
                val dstRect = Rect(
                    (magnifierX - radius).toInt(),
                    (magnifierY - radius).toInt(),
                    (magnifierX + radius).toInt(),
                    (magnifierY + radius).toInt()
                )

                // Create circular clipping path
                val path = Path().apply {
                    addCircle(magnifierX, magnifierY, radius, Path.Direction.CW)
                }

                // Clip to circular magnifier and draw bitmap
                canvas.save()
                canvas.clipPath(path)
                canvas.drawBitmap(bmp, srcRect, dstRect, paint)
                canvas.restore()

                // Draw border and crosshair
                canvas.drawCircle(magnifierX, magnifierY, radius, borderPaint)
                canvas.drawLine(magnifierX - 15, magnifierY, magnifierX + 15, magnifierY, crosshairPaint)
                canvas.drawLine(magnifierX, magnifierY - 15, magnifierX, magnifierY + 15, crosshairPaint)

            } catch (e: Exception) {
                Log.e("MagnifierView", "Error drawing magnifier: ${e.message}")
            }
        }
    }
}
