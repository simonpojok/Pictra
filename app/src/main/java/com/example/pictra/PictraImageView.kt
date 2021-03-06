package com.example.pictra

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import android.view.MotionEvent
import java.io.File
import java.util.ArrayList

class PictraImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(
    context!!, attrs
) {
    private var state = 0
    private val paintPenList = ArrayList<Paint>()
    private val textPaintList = ArrayList<TextPaint>()
    private lateinit var latestPath: Path
    private lateinit var latestPaint: Paint
    private lateinit var textPaint: Paint
    private val pathPenList = ArrayList<Path>()
    private lateinit var callbackForCoordinate: GetCoordinateCallback
    private var lineWidth = 3
    private var currentColor = 0
    private  var xPosition: Float = 0f
    private  var yPosition: Float = 0f
    private fun init() {
        DEFAULT_COLOR = ContextCompat.getColor(context, R.color.colorAccent)
        currentColor = DEFAULT_COLOR
        initPaintNPen(currentColor)
    }

    private fun initPaintNPen(color: Int) {
        textPaint = getNewPaintPen(color)
        latestPaint = getNewPaintPen(color)
        latestPath = newPathPen
        paintPenList.add(latestPaint)
        pathPenList.add(latestPath)
    }

    private val newPathPen: Path
        get() = Path()

    private fun getNewPaintPen(color: Int): Paint {
        val mPaintPen = Paint()
        mPaintPen.strokeWidth = lineWidth.toFloat()
        mPaintPen.isAntiAlias = true
        mPaintPen.isDither = true
        mPaintPen.style = Paint.Style.STROKE
        mPaintPen.strokeJoin = Paint.Join.MITER
        mPaintPen.strokeCap = Paint.Cap.ROUND
        mPaintPen.color = color
        return mPaintPen
    }

    fun setThisCallback(callback: GetCoordinateCallback) {
        callbackForCoordinate = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        xPosition = x
        yPosition = y
        Log.i("CO-ordinate", event.x.toString() + " : " + event.y)
        if (event.action == MotionEvent.ACTION_DOWN) {
            callbackForCoordinate.start(x, y)
            startPath(x, y)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            callbackForCoordinate.moving(x, y)
            updatePath(x, y)
        } else if (event.action == MotionEvent.ACTION_UP) {
            callbackForCoordinate.end(x, y)
            endPath(x, y)
        }
        invalidate()
        return true
    }

    private fun startPath(x: Float, y: Float) {
        /*if(state==STATE_MOVING)
            mPath.lineTo(x,y);
        else
            mPath.moveTo(x,y);*/
        initPaintNPen(currentColor)
        latestPath.moveTo(x, y)
    }

    private fun updatePath(x: Float, y: Float) {
        state = STATE_MOVING
        latestPath.lineTo(x, y)
    }

    private fun endPath(x: Float, y: Float) {

    }
    fun setDrawColor(color: Int) {
        currentColor = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in paintPenList.indices) {
            canvas.drawPath(pathPenList[i], paintPenList[i])
        }

        for (paint in textPaintList) {
            canvas.drawText(paint.text, paint.xPoint, paint.yPoint, paint.paint)
        }

    }

    fun increaseWidth(decrease: Boolean) {
        if (decrease) {
            if (lineWidth > 5) {
                lineWidth -= 10
            }
        } else {
            if (lineWidth < 50) {
                lineWidth += 10
            }
        }
        invalidate()
    }

    fun resetView() {
        currentColor = DEFAULT_COLOR
        state = STATE_STILL
        latestPath.reset()
        latestPaint.reset()
        pathPenList.clear()
        paintPenList.clear()
        lineWidth = 5
        initPaintNPen(currentColor)
        textPaintList.clear()
        invalidate()
    }

    fun undoPath() {
        if (paintPenList.size > 1) {
            latestPaint = paintPenList[paintPenList.size - 2]
            latestPath = pathPenList[pathPenList.size - 2]
            paintPenList.removeAt(paintPenList.size - 1)
            pathPenList.removeAt(pathPenList.size - 1)
            currentColor = latestPaint.color
            lineWidth = latestPaint.strokeWidth.toInt()
        } else {
            resetView()
        }

        if (textPaintList.size > 1) {
            textPaintList.removeAt(textPaintList.size - 1)
        } else {
            textPaintList.clear()
        }
        invalidate()
    }

    fun drawUserText(text: String) {
        val paint = Paint()
        paint.color = currentColor
        paint.textSize = 30f
        textPaintList.add(TextPaint(
            paint = paint,
            text = text,
            xPoint = xPosition,
            yPoint = yPosition
        ))
    }

    fun updateImageContrastBrightness(brightness: Float, contrast: Float) {
        buildDrawingCache()
        val bitmap = drawingCache
        setImageBitmap(bitmap.setBrightnessContrast(brightness, contrast))
    }

    data class TextPaint(
        val paint: Paint,
        val text: String,
        val xPoint: Float,
        val yPoint: Float

    )

    interface GetCoordinateCallback {
        fun moving(x: Float, y: Float)
        fun start(x: Float, y: Float)
        fun end(x: Float, y: Float)
    }

    companion object {
        private const val STATE_STILL = 0
        private const val STATE_MOVING = 1
        private var DEFAULT_COLOR = 0
    }

    init {
        init()
    }

    fun Bitmap.setBrightnessContrast(
        brightness:Float = 0.0F,
        contrast:Float = 1.0F
    ):Bitmap?{
        val bitmap = copy(Bitmap.Config.ARGB_8888,true)
        val paint = Paint()

        val matrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val filter = ColorMatrixColorFilter(matrix)
        paint.colorFilter = filter

        Canvas(bitmap).drawBitmap(this,0f,0f,paint)
        return bitmap
    }
}