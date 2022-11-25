package com.example.android.wearable.alpha.builder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.TypedValue

class DigitalStyleBuilder : StyleBuilder() {

    fun setBackGroundColor(canvas: Canvas, backgroundColor: Int) {
        canvas.drawColor(backgroundColor)
    }

    fun setTextColor(paint: TextPaint, textColor: Int) {
        paint.color = textColor
    }

    fun setFontSize(context: Context, paint: TextPaint, fontSize: Float) {
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, fontSize, context.resources.displayMetrics
        )
    }

    fun setSmoothEdge(paint: TextPaint, isAntiAlias: Boolean) {
        paint.isAntiAlias = isAntiAlias
    }

    fun setCircleColor(paint: Paint, cirCleColor: Int){
        paint.style = Paint.Style.STROKE
        paint.color = cirCleColor
    }
}
