package com.example.android.wearable.alpha.builder

import android.text.TextPaint

class AnalogStyleBuilder: StyleBuilder(){
    fun setArmColor(paint: TextPaint, armColor: Int) {
        paint.color = armColor
    }
}
