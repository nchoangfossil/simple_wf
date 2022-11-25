package com.example.android.wearable.alpha

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import com.example.android.wearable.alpha.builder.DigitalStyleBuilder
import com.example.android.wearable.alpha.data.watchface.DIGITAL_MEDIUM_STYLE
import com.example.android.wearable.alpha.data.watchface.DIGITAL_SMALL_STYLE
import com.example.android.wearable.alpha.data.watchface.DigitalStyle
import com.example.android.wearable.alpha.data.watchface.WatchFaceData
import com.example.android.wearable.alpha.factory.IWatchFaceCanvasRender
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.abs


class DigitalWatchFaceCanvasRenderer(
    private val context: Context,
    private val watchFaceData: WatchFaceData,
    private val bounds: Rect,
    private val canvas: Canvas
) :IWatchFaceCanvasRender(){

    private val dayFormat: SimpleDateFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var timePaint: TextPaint = TextPaint()
    private var dayPaint: TextPaint = TextPaint()
    private var circlePaint = Paint()

    private lateinit var director: DigitalDirector
    private lateinit var digitalBuilder: DigitalStyleBuilder

    override fun drawCanvas() {
        super.drawCanvas()
        val time = System.currentTimeMillis()
        val timeText = timeFormat.format(time)
        val dayText = dayFormat.format(time)

        setStyle()
        drawTimeText(timeText)
        drawDayText(dayText)
    }

    override fun setStyle() {
        digitalBuilder = DigitalStyleBuilder()
        director = DigitalDirector(
            context = context,
            builder = digitalBuilder,
            timePaint = timePaint,
            dayPaint = dayPaint,
            circlePaint = circlePaint
        )

        when (watchFaceData.digitalStyle.id) {
            DIGITAL_SMALL_STYLE -> director.buildSmallStyle()
            DIGITAL_MEDIUM_STYLE -> director.buildMediumStyle()
            else -> director.buildLargeStyle()
        }

    }

    private fun drawTimeText(timeText: String) {
        val timeBounds = Rect()
        val x: Int = abs(bounds.centerX() - timeBounds.centerX())
        val y: Int = bounds.centerY()

        timePaint.getTextBounds(timeText, 0, timeText.length, timeBounds)
        canvas.drawText(
            timeText,
            x.toFloat() - timeBounds.centerX(),
            y.toFloat() - timeBounds.centerY(),
            timePaint
        )
    }

    private fun drawWeekDayText(
        corX: Int, corY: Int, weekDayText: String, canvas: Canvas
    ) {
        canvas.drawText(weekDayText, corX.toFloat(), corY.toFloat(), dayPaint)
    }

    private fun drawDayText(
        dayText: String
    ) {
        val dayBounds = Rect()
        val circleX: Int = bounds.centerX() / 2
        val circleY: Int = bounds.centerY() * 4 / 3

        dayPaint.getTextBounds(dayText, 0, dayText.length, dayBounds)

        val dayX = (circleX - dayBounds.centerX()).toFloat()
        val dayY = (circleY - dayBounds.centerY()).toFloat()

        canvas.drawCircle(circleX.toFloat(), circleY.toFloat(), 40f, circlePaint)
        canvas.drawText(dayText, dayX, dayY, dayPaint)
    }
}
