package com.example.hits_android

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.*

class SplineActivity : AppCompatActivity() {

    private lateinit var myCustomView: MyCustomView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spline)
        myCustomView = findViewById(R.id.my_custom_view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val homePage: Button = findViewById(R.id.nazad)
        homePage.setOnClickListener {
            val intent = Intent(this@SplineActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val replacing: Button = findViewById(R.id.replacePoint)
        replacing.setOnClickListener {
            myCustomView.setAddButton(false)
            myCustomView.setDeleteButton(false)
            myCustomView.setReplaceButton(true)
        }

        val deleting: Button = findViewById(R.id.deletePoint)
        deleting.setOnClickListener {
            myCustomView.setAddButton(false)
            myCustomView.setDeleteButton(true)
            myCustomView.setReplaceButton(false)
        }

        val added: Button = findViewById(R.id.addPoint)
        added.setOnClickListener {
            myCustomView.setAddButton(true)
            myCustomView.setDeleteButton(false)
            myCustomView.setReplaceButton(false)
        }

        val splining: Button = findViewById(R.id.spline)
        splining.setOnClickListener {
            myCustomView.setAddButton(false)
            myCustomView.setSplineButton(true)
            myCustomView.setDeleteButton(false)
            myCustomView.setReplaceButton(false)
        }
    }
}

class MyCustomView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    private var points = mutableListOf<PointF>()
    private var flag = true
    private var flagForSpline = false
    private var index = -1
    private var replaceButtonClicked = false
    private var deleteButtonClicked = false
    private var addButtonClicked = false
    private var splineButtonClicked = false
    private var splines = mutableListOf<PointF>()
    private var segments = mutableListOf<MutableList<PointF>>()

    fun setReplaceButton(clicked: Boolean) {
        replaceButtonClicked = clicked
    }

    fun setDeleteButton(clicked: Boolean) {
        deleteButtonClicked = clicked
    }

    fun setAddButton(clicked: Boolean) {
        addButtonClicked = clicked
    }

    fun setSplineButton(clicked: Boolean) {
        splineButtonClicked = clicked
        flagForSpline = true
        convertingToSpline()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!splineButtonClicked) {
            for (point in points) {
                canvas.drawCircle(point.x, point.y, 20f, paint)
            }
            for (i in 0 until points.size - 1) {
                canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paint)
                if (!flag) {
                    canvas.drawLine(points.last().x, points.last().y, points.first().x, points.first().y, paint)
                }
            }
        }
        else {
            for (point in points) {
                canvas.drawCircle(point.x, point.y, 20f, paint)
            }
            if (points.size > 2) {
                for (i in 0 until splines.size - 1) {
                    canvas.drawLine(splines[i].x, splines[i].y, splines[i + 1].x, splines[i + 1].y, paint
                    )
                }
                canvas.drawLine(splines.last().x, splines.last().y, splines.first().x, splines.first().y, paint
                )
            }
            else if (points.size == 2){
                canvas.drawLine(points.first().x, points.first().y, points.last().x, points.last().y, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentPoint = PointF(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (addButtonClicked) {
                    if (flag && points.size > 0 && isNearFirstPoint(currentPoint)) {
                        flag = false
                    }
                    if (flag) {
                        points.add(currentPoint)
                    }
                    if (!flag && !isNearFirstPoint(currentPoint)) {
                        var firstMinimumDistance = 100000F
                        var firstIndex = 0
                        var secondMinimumDistance = 100000F
                        var secondIndex = 0
                        for (i in 0 until points.size) {
                            val firstCurrentDistance = calculateDistance(currentPoint, points[i])
                            if (firstCurrentDistance < firstMinimumDistance) {
                                firstMinimumDistance = firstCurrentDistance
                                firstIndex = i
                            }
                        }
                        for (j in 0 until points.size) {
                            val secondCurrentDistance = calculateDistance(currentPoint, points[j])
                            if (secondCurrentDistance < secondMinimumDistance && firstIndex != j) {
                                secondMinimumDistance = secondCurrentDistance
                                secondIndex = j
                            }
                        }
                        val newIndex = min(firstIndex, secondIndex)
                        if (newIndex == 0 && max(firstIndex, secondIndex) != 1) {
                            points.add(currentPoint)
                        }
                        else {
                            points.add(newIndex + 1, currentPoint)
                        }
                        convertingToSpline()
                    }
                }
                if (replaceButtonClicked) {
                    index = getNearPointIndex(currentPoint)
                }
                if (deleteButtonClicked) {
                    index = getNearPointIndex(currentPoint)
                    if (index != -1) {
                        points.removeAt(index)
                    }
                    if (flagForSpline) {
                        convertingToSpline()
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (index != -1 && replaceButtonClicked) {
                    val newPoint = PointF(event.x, event.y)
                    points[index] = newPoint
                    invalidate()
                    if (flagForSpline) {
                        convertingToSpline()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (index != -1 && replaceButtonClicked) {
                    val newPoint = PointF(event.x, event.y)
                    points[index] = newPoint
                    if (flagForSpline) {
                        convertingToSpline()
                    }
                    invalidate()
                    index = -1
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isNearFirstPoint(point: PointF): Boolean {
        return (points[0].x <= point.x + 50 && points[0].x >= point.x - 50) && (points[0].y <= point.y + 50 && points[0].y >= point.y - 50)
    }

    private fun getNearPointIndex(point: PointF): Int {
        for (i in 0 until points.size) {
            if ((points[i].x <= point.x + 50 && points[i].x >= point.x - 50) && (points[i].y <= point.y + 50 && points[i].y >= point.y - 50)) {
                return i
            }
        }
        return -1
    }

    private fun calculateDistance(newPoint: PointF, currentPoint: PointF): Float {
        return sqrt((newPoint.x - currentPoint.x) * (newPoint.x - currentPoint.x) + (newPoint.y - currentPoint.y) * (newPoint.y - currentPoint.y))
    }

    private fun constructionSegments() {
        splines.clear()
        segments.clear()

        for (i in 0 until points.size - 1) {
            segments.add(mutableListOf(points[i], points[i + 1]))
        }
        segments.add(mutableListOf(points.last(), points.first()))
    }

    private fun convertingToSpline() {
        if (points.size > 2) {
            constructionSegments()

            var xS: Double
            var yS: Double
            var m0X: Double
            var m0Y: Double
            var m1X: Double
            var m1Y: Double

            for (i in 0 until segments.size) {
                var t = 0.0
                if (i == 0) {
                    m0X = (segments[0][1].x - segments[segments.size - 1][0].x) / 2.0
                    m0Y = (segments[0][1].y - segments[segments.size - 1][0].y) / 2.0
                    m1X = (segments[1][1].x - segments[i][0].x) / 2.0
                    m1Y = (segments[1][1].y - segments[i][0].y) / 2.0
                } else if (i == segments.size - 1) {
                    m0X = (segments[i][1].x - segments[i - 1][0].x) / 2.0
                    m0Y = (segments[i][1].y - segments[i - 1][0].y) / 2.0
                    m1X = (segments[0][1].x - segments[i][0].x) / 2.0
                    m1Y = (segments[0][1].y - segments[i][0].y) / 2.0
                } else {
                    m0X = (segments[i][1].x - segments[i - 1][0].x) / 2.0
                    m0Y = (segments[i][1].y - segments[i - 1][0].y) / 2.0
                    m1X = (segments[i + 1][1].x - segments[i][0].x) / 2.0
                    m1Y = (segments[i + 1][1].y - segments[i][0].y) / 2.0
                }

                while (t <= 1.0) {
                    xS = (2.0 * t.pow(3.0) - 3.0 * t.pow(2.0) + 1) * segments[i][0].x +
                            (t.pow(3.0) - 2.0 * t.pow(2.0) + t) * m0X +
                            (3.0 * t.pow(2.0) - 2.0 * t.pow(3.0)) * segments[i][1].x +
                            (t.pow(3.0) - t.pow(2.0)) * m1X
                    yS = (2.0 * t.pow(3.0) - 3.0 * t.pow(2.0) + 1) * segments[i][0].y +
                            (t.pow(3.0) - 2.0 * t.pow(2.0) + t) * m0Y +
                            (3.0 * t.pow(2.0) - 2.0 * t.pow(3.0)) * segments[i][1].y +
                            (t.pow(3.0) - t.pow(2.0)) * m1Y
                    splines.add(PointF(xS.toFloat(), yS.toFloat()))
                    t += 0.01
                }
            }
        }
    }
}