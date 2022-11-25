package com.example.android.wearable.alpha.factory

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.example.android.wearable.alpha.*
import com.example.android.wearable.alpha.data.watchface.WatchFaceData
import com.example.android.wearable.alpha.data.watchface.WatchFaceMode
import java.time.ZonedDateTime

class WatchFaceCanvasRendererFactory() {
    companion object {
        fun createWatchFaceRenderer(
            type: WatchFaceMode,
            context: Context,
            watchFaceData: WatchFaceData,
            bounds: Rect,
            canvas: Canvas,
            renderParameters: RenderParameters? = null,
            zonedDateTime: ZonedDateTime? = null
        ): IWatchFaceCanvasRender {
            val renderer = when (type) {
                WatchFaceMode.DIGITAL -> DigitalWatchFaceCanvasRenderer(
                    context = context,
                    watchFaceData = watchFaceData,
                    bounds = bounds,
                    canvas = canvas
                )
                else -> AnalogWatchFaceCanvasRenderer(
                    context = context,
                    watchFaceData = watchFaceData,
                    bounds = bounds,
                    canvas = canvas,
                    zonedDateTime = zonedDateTime!!,
                    renderParameters = renderParameters!!
                )
            }
            return renderer
        }
    }
}
