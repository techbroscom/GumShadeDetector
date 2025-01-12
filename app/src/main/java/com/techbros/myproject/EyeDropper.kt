package com.techbros.myproject
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.DRAWING_CACHE_QUALITY_LOW
import android.widget.ImageView
import androidx.annotation.ColorInt

private const val NO_COLOR = Color.TRANSPARENT
private val INVERT_MATRIX = Matrix()

class EyeDropper(private val view: View, private val listener: ColorSelectionListener) {

    /**
     * Register a callback to be invoked when the color selection begins or ends.
     */
    var selectionListener: SelectionListener? = null

    init {
        if (view.shouldDrawingCacheBeEnabled()) {
            view.isDrawingCacheEnabled = true
            view.drawingCacheQuality = DRAWING_CACHE_QUALITY_LOW
        }
        setTouchListener()
    }

    private fun setTouchListener() {
        view.setOnTouchListener { _, event ->
            if (event.down()) selectionListener?.onSelectionStart(event)
            notifyColorSelection(event.x.toInt(), event.y.toInt())
            if (event.up()) selectionListener?.onSelectionEnd(event)
            true
        }
    }

    private fun getColorAtPoint(x: Int, y: Int): Int {
        return when (view) {
            is ImageView -> handleIfImageView(view, x, y)
            else -> getPixelAtPoint(view.drawingCache, x, y)
        }
    }

    private fun handleIfImageView(view: ImageView, x: Int, y: Int): Int {
        return when (val drawable = view.drawable) {
            is BitmapDrawable -> {
                view.imageMatrix.invert(INVERT_MATRIX)
                val mappedPoints = floatArrayOf(x.toFloat(), y.toFloat())
                INVERT_MATRIX.mapPoints(mappedPoints)
                getPixelAtPoint(drawable.bitmap, mappedPoints[0].toInt(), mappedPoints[1].toInt())
            }
            else -> NO_COLOR
        }
    }

    fun getPixelAtPoint(bitmap: Bitmap, x: Int, y: Int): Int {
        if (bitmap.isValidCoordinate(x, y)) {
            return bitmap.getPixel(x, y)
        }
        return NO_COLOR
    }

    fun notifyColorSelection(x: Int, y: Int) {
        val colorAtPoint = getColorAtPoint(x, y)
        listener.onColorSelected(colorAtPoint)
    }


    /**
     * Optional listener to listen to before and after selection events
     */
    interface SelectionListener {
        /**
         * Invoked when the user touches the view to select a color. This corresponds to the [ ][MotionEvent.ACTION_DOWN] event.
         *
         * @param event the down motion event
         */
        fun onSelectionStart(event: MotionEvent)

        /**
         * Invoked when the color selection is finished. This corresponds to the [MotionEvent.ACTION_UP]
         * event.
         *
         * @param event the up motion event
         */
        fun onSelectionEnd(event: MotionEvent)
    }

    interface ColorSelectionListener {
        /**
         * Invoked when a color is selected from the given view

         * @param color the selected color
         */
        fun onColorSelected(@ColorInt color: Int)
    }
}

fun Bitmap.isValidCoordinate(x: Int, y: Int): Boolean {
    return x in 1 until width && y in 1 until height
}

fun View.shouldDrawingCacheBeEnabled(): Boolean = (this !is ImageView) && !isDrawingCacheEnabled

fun MotionEvent.down() = (actionMasked == ACTION_DOWN)
fun MotionEvent.up() = (actionMasked == ACTION_UP)