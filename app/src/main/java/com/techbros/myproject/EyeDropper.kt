import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

class EyeDropper(private val imageView: ImageView, private val listener: ColorSelectionListener) {
    interface ColorSelectionListener {
        fun onColorSelected(color: Int)
    }

    fun notifyColorSelection(x: Int, y: Int) {
        val drawable = imageView.drawable ?: return
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> return
        }

        // Convert view coordinates to bitmap coordinates
        val imageMatrix = imageView.imageMatrix
        val matrixValues = FloatArray(9)
        imageMatrix.getValues(matrixValues)

        // Scaling factors
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]

        // Translation points
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        // Calculate bitmap coordinates
        val bitmapX = ((x - transX) / scaleX).toInt().coerceIn(0, bitmap.width - 1)
        val bitmapY = ((y - transY) / scaleY).toInt().coerceIn(0, bitmap.height - 1)

        // Get pixel color
        try {
            val color = bitmap.getPixel(bitmapX, bitmapY)
            listener.onColorSelected(color)
        } catch (e: Exception) {
            // Handle out of bounds or other exceptions
        }
    }
}