package com.example.android.wearable.alpha

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import com.example.android.wearable.alpha.builder.DigitalStyleBuilder
import com.example.android.wearable.alpha.data.watchface.DIGITAL_LARGE_STYLE
import com.example.android.wearable.alpha.data.watchface.DIGITAL_MEDIUM_STYLE
import com.example.android.wearable.alpha.data.watchface.DIGITAL_SMALL_STYLE
import com.example.android.wearable.alpha.data.watchface.DigitalStyle

class DigitalDirector(
    private val context: Context,
    private val builder: DigitalStyleBuilder,
    private val timePaint: TextPaint,
    private val dayPaint: TextPaint,
    private val circlePaint: Paint
) {

    fun buildSmallStyle() {
        val smallStyle = DigitalStyle.getDigitalStyle(DIGITAL_SMALL_STYLE)

        builder.setTextColor(timePaint, smallStyle.textColor)
        builder.setFontSize(context, timePaint, smallStyle.timeSize)
        builder.setTextColor(dayPaint, smallStyle.textColor)
        builder.setFontSize(context, dayPaint, smallStyle.daySize)
        builder.setSmoothEdge(timePaint, smallStyle.isAntiAlias)
        builder.setCircleColor(circlePaint, smallStyle.textColor)
    }

    fun buildMediumStyle() {
        val mediumStyle = DigitalStyle.getDigitalStyle(DIGITAL_MEDIUM_STYLE)

        builder.setTextColor(timePaint, mediumStyle.textColor)
        builder.setFontSize(context, timePaint, mediumStyle.timeSize)
        builder.setTextColor(dayPaint, mediumStyle.textColor)
        builder.setFontSize(context, dayPaint, mediumStyle.daySize)
        builder.setSmoothEdge(timePaint, mediumStyle.isAntiAlias)
        builder.setCircleColor(circlePaint, mediumStyle.textColor)
    }

    fun buildLargeStyle() {
        val largeStyle = DigitalStyle.getDigitalStyle(DIGITAL_LARGE_STYLE)

        builder.setTextColor(timePaint, largeStyle.textColor)
        builder.setFontSize(context, timePaint, largeStyle.timeSize)
        builder.setTextColor(dayPaint, largeStyle.textColor)
        builder.setFontSize(context, dayPaint, largeStyle.daySize)
        builder.setSmoothEdge(timePaint, largeStyle.isAntiAlias)
        builder.setCircleColor(circlePaint, largeStyle.textColor)
    }
}
