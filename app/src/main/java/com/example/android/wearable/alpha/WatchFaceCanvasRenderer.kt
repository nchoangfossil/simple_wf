package com.example.android.wearable.alpha

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.data.watchface.*
import com.example.android.wearable.alpha.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import com.example.android.wearable.alpha.factory.IWatchFaceCanvasRender
import com.example.android.wearable.alpha.factory.WatchFaceCanvasRendererFactory
import com.example.android.wearable.alpha.utils.*
import java.io.InputStream
import java.time.ZonedDateTime
import kotlinx.coroutines.*


// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

/**
 * Renders watch face via data in Room database. Also, updates watch face state based on setting
 * changes by user via [userStyleRepository.addUserStyleListener()].
 */
open class WatchFaceCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer2<WatchFaceCanvasRenderer.AnalogSharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {

    private lateinit var renderer: IWatchFaceCanvasRender

    class AnalogSharedAssets : SharedAssets {
        override fun onDestroy() {
        }
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Represents all data needed to render the watch face. All value defaults are constants. Only
    // three values are changeable by the user (color scheme, ticks being rendered, and length of
    // the minute arm). Those dynamic values are saved in the watch face APIs and we update those
    // here (in the renderer) through a Kotlin Flow.
    private var watchFaceData: WatchFaceData = WatchFaceData()

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = convertToWatchFaceColorPalette(
        context, watchFaceData.activeColorStyle, watchFaceData.ambientColorStyle
    )

    // Used to paint the main hour hand text with the hour pips, i.e., 3, 6, 9, and 12 o'clock.
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.hour_mark_size).toFloat()
    }

    init {
        scope.launch {
            currentUserStyleRepository.userStyle.collect { userStyle ->
                updateWatchFaceData(userStyle)
            }
        }
    }

    override suspend fun createSharedAssets(): AnalogSharedAssets {
        return AnalogSharedAssets()
    }

    /*
     * Triggered when the user makes changes to the watch face through the settings activity. The
     * function is called by a flow.
     */
    open fun updateWatchFaceData(userStyle: UserStyle) {
        var newWatchFaceData: WatchFaceData = watchFaceData

        // Loops through user style and applies new values to watchFaceData.
        for (options in userStyle) when (options.key.id.toString()) {
            COLOR_STYLE_SETTING -> {
                val listOption =
                    options.value as UserStyleSetting.ListUserStyleSetting.ListOption

                newWatchFaceData = newWatchFaceData.copy(
                    activeColorStyle = ColorStyleIdAndResourceIds.getColorStyleConfig(
                        listOption.id.toString()
                    )
                )
            }
            DRAW_HOUR_PIPS_STYLE_SETTING -> {
                val booleanValue =
                    options.value as UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                newWatchFaceData = newWatchFaceData.copy(
                    drawHourPips = booleanValue.value
                )
            }
            BACKGROUND_STYLE_SETTING -> {
                val listOption =
                    options.value as UserStyleSetting.ListUserStyleSetting.ListOption

                newWatchFaceData = newWatchFaceData.copy(
                    backGroundStyle = BackGroundStyle.getBackGroundStyleConfig(
                        listOption.id.toString()
                    )
                )
            }

            DIGITAL_STYLE_SETTING -> {
                val listOption =
                    options.value as UserStyleSetting.ListUserStyleSetting.ListOption
                Log.d("Id", listOption.toString())
                newWatchFaceData = newWatchFaceData.copy(
                    digitalStyle = DigitalStyle.getDigitalStyle(
                        listOption.id.toString()
                    )
                )
            }

        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            if (::renderer.isInitialized) {
                renderer.setStyle()
            }

            // Recreates Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToWatchFaceColorPalette(
                context, watchFaceData.activeColorStyle, watchFaceData.ambientColorStyle
            )

//             Applies the user chosen complication color scheme canges. ComplicationDrawables for
//             each of the styles are defined in XML so we need to replace the complication's
//             drawables.
            for ((_, complication) in complicationSlotsManager.complicationSlots) {
                ComplicationDrawable.getDrawable(
                    context, watchFaceColors.complicationStyleDrawableId
                )?.let {
                    (complication.renderer as CanvasComplicationDrawable).drawable = it
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("AnalogWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

    override fun renderHighlightLayer(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: AnalogSharedAssets
    ) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)

        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    override fun render(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: AnalogSharedAssets
    ) {
        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            watchFaceColors.ambientBackgroundColor
        } else {
            watchFaceColors.activeBackgroundColor
        }
        canvas.drawColor(backgroundColor)

        renderer = WatchFaceCanvasRendererFactory.createWatchFaceRenderer(
            type = WatchFaceMode.DIGITAL,
            context = context,
            watchFaceData = watchFaceData,
            bounds = bounds,
            canvas = canvas
        )

//        val renderer = WatchFaceCanvasRendererFactory.createWatchFaceRenderer(
//            type = WatchFaceType.ANALOG,
//            context = context,
//            watchFaceData = watchFaceData,
//            bounds = bounds,
//            canvas = canvas,
//            zonedDateTime = zonedDateTime,
//            renderParameters = renderParameters
//        )

        renderer.drawCanvas()
    }

    // ----- All drawing functions -----
    private fun drawBackGround(canvas: Canvas) {
        val stream: InputStream = context.assets.open(watchFaceData.backGroundStyle.resourcePath)
        val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
        val corX = 0.0f
        val corY = 0.0f
        val scaledBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, canvas.width, canvas.height, true)
        canvas.drawBitmap(scaledBitmap, corX, corY, null)
    }

    private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    companion object {
        private const val TAG = "WatchCanvasRenderer"
    }
}
