package com.minimal.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Zeichnet einen Balken aus kleinen Quadraten ("Halbton"-Look):
// links dicht/hell = bereits verbrauchter Akku, rechts gedaempft = Rest.
class DottedBatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dotPaintFilled = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
    }
    private val dotPaintDim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x40FFFFFF
    }

    private val dotSizePx = dpToPx(3f)
    private val gapPx = dpToPx(2.5f)

    var progressPercent: Int = 100
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val step = dotSizePx + gapPx
        val cols = (width / step).toInt()
        val rows = (height / step).toInt().coerceAtLeast(1)
        if (cols <= 0) return

        val filledCols = (cols * progressPercent) / 100

        for (row in 0 until rows) {
            val top = row * step
            for (col in 0 until cols) {
                val left = col * step
                val paint = if (col < filledCols) dotPaintFilled else dotPaintDim
                canvas.drawRect(left, top, left + dotSizePx, top + dotSizePx, paint)
            }
        }
    }
}
