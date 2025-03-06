package com.example.nail

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Custom view to draw bounding boxes on the screen
class BoundingBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val boxes = mutableListOf<Rect>()
    private val boxColors = listOf(
        Color.GREEN, Color.GREEN, Color.GREEN, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.RED
    )
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    private var selectedBox: Rect? = null
    private var boxSize = 100

    // Method to reset the bounding boxes to predefined positions
    fun resetPosition(xOffset: Float, yOffset: Float, wFactor: Float, hFactor: Float) {
        boxes.clear()

        val positionsList: List<Pair<Double, Double>> = listOf(
            Pair(0.35, 0.3),
            Pair(0.3, 0.4),
            Pair(0.35, 0.5),
            Pair(0.65, 0.3),
            Pair(0.6, 0.4),
            Pair(0.65, 0.5),
            Pair(0.05, 0.03)
        )

        positionsList.forEachIndexed { index, position ->
            val left = (xOffset + position.first * wFactor).toInt()
            val top = (yOffset + position.second * hFactor).toInt()
            boxes.add(Rect(left, top, left + boxSize, top + boxSize))
        }
        invalidate()
    }

    // Set new bounding boxes dynamically from outside
    fun setBoundingBoxes(boundingBoxes: List<Rect>) {
        boxes.clear()
        boxes.addAll(boundingBoxes)
        invalidate()
    }

    // Draw the bounding boxes and numbers
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boxes.forEachIndexed { index, box ->
            paint.color = boxColors[index % boxColors.size]
            canvas.drawRect(box, paint)
            val numberX = box.centerX().toFloat()
            val numberY = box.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText((index + 1).toString(), numberX, numberY, textPaint)
        }
    }

    // Handle touch events to move the selected box
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedBox = boxes.find { it.contains(event.x.toInt(), event.y.toInt()) }
            }
            MotionEvent.ACTION_MOVE -> {
                selectedBox?.let { box ->
                    box.offsetTo(event.x.toInt() - box.width() / 2, event.y.toInt() - box.height() / 2)
                    invalidate()
                }
            }
        }
        return true
    }

    // Set the size of all bounding boxes
    fun setBoxSize(size: Int) {
        boxSize = size
        boxes.forEachIndexed { index, box ->
            val left = box.left
            val top = box.top
            box.set(left, top, left + size, top + size)
        }
        invalidate()
    }

    // Get the current list of bounding boxes
    fun getBoundingBoxes(): List<Rect> = boxes
}


