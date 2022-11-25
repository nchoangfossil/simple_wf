package com.example.android.wearable.alpha

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.android.wearable.alpha.data.watchface.WatchFaceColorPalette
import com.example.android.wearable.alpha.data.watchface.WatchFaceData
import com.example.android.wearable.alpha.factory.IWatchFaceCanvasRender
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin


class AnalogWatchFaceCanvasRenderer(
    private val context: Context,
    private val watchFaceData: WatchFaceData,
    private val bounds: Rect,
    private val canvas: Canvas,
    private val renderParameters: RenderParameters,
    private val zonedDateTime: ZonedDateTime
) : IWatchFaceCanvasRender() {

    /** Initializes paint object for painting the clock hands with default values.*/
    private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

    /** Changed when setting changes cause a change in the minute hand arm (triggered by user in
    updateUserStyle() via userStyleRepository.addUserStyleListener()). */
    private var armLengthChangedRecalculateClockHands: Boolean = false

    /**Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    valid dimensions from the system. */
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = WatchFaceColorPalette.convertToWatchFaceColorPalette(
        context, watchFaceData.activeColorStyle, watchFaceData.ambientColorStyle
    )


    private val outerElementPaint = Paint().apply {
        isAntiAlias = true
    }

    // Used to paint the main hour hand text with the hour pips, i.e., 3, 6, 9, and 12 o'clock.
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.hour_mark_size).toFloat()
    }

    private lateinit var hourHandFill: Path
    private lateinit var hourHandBorder: Path
    private lateinit var minuteHandFill: Path
    private lateinit var minuteHandBorder: Path
    private lateinit var secondHand: Path

    override fun drawCanvas() {
        super.drawCanvas()
        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            drawClockHands(zonedDateTime)
        }
        if (renderParameters.drawMode == DrawMode.INTERACTIVE && renderParameters.watchFaceLayers.contains(
                WatchFaceLayer.BASE
            ) && watchFaceData.drawHourPips
        ) {
            drawNumberStyleOuterElement(
                watchFaceData.numberRadiusFraction,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceColors.activeOuterElementColor,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceData.gapBetweenOuterCircleAndBorderFraction
            )
        }
    }

    private fun drawNumberStyleOuterElement(
        numberRadiusFraction: Float,
        outerCircleStokeWidthFraction: Float,
        outerElementColor: Int,
        numberStyleOuterCircleRadiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        // Draws text hour indicators (12, 3, 6, and 9).
        val textBounds = Rect()
        textPaint.color = outerElementColor
        for (i in 0 until 4) {
            val rotation = 0.5f * (i + 1).toFloat() * Math.PI
            val dx = sin(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            val dy = -cos(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            textPaint.getTextBounds(HOUR_MARKS[i], 0, HOUR_MARKS[i].length, textBounds)
            canvas.drawText(
                HOUR_MARKS[i],
                bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
                bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
                textPaint
            )
        }

        // Draws dots for the remain hour indicators between the numbers above.
        outerElementPaint.strokeWidth = outerCircleStokeWidthFraction * bounds.width()
        outerElementPaint.color = outerElementColor
        canvas.save()
        for (i in 0 until 12) {
            if (i % 3 != 0) {
                drawTopMiddleCircle(
                    numberStyleOuterCircleRadiusFraction,
                    gapBetweenOuterCircleAndBorderFraction
                )
            }
            canvas.rotate(360.0f / 12.0f, bounds.exactCenterX(), bounds.exactCenterY())
        }
        canvas.restore()
    }

    /** Draws the outer circle on the top middle of the given bounds. */
    private fun drawTopMiddleCircle(
        radiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        outerElementPaint.style = Paint.Style.FILL_AND_STROKE

        // X and Y coordinates of the center of the circle.
        val centerX = 0.5f * bounds.width().toFloat()
        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)

        canvas.drawCircle(
            centerX, centerY, radiusFraction * bounds.width(), outerElementPaint
        )
    }


    /**
     * Returns a round rect clock hand if {@code rx} and {@code ry} equals to 0, otherwise return a
     * rect clock hand.
     *
     * @param bounds The bounds use to determine the coordinate of the clock hand.
     * @param length Clock hand's length, in fraction of {@code bounds.width()}.
     * @param thickness Clock hand's thickness, in fraction of {@code bounds.width()}.
     * @param gapBetweenHandAndCenter Gap between inner side of arm and center.
     * @param roundedCornerXRadius The x-radius of the rounded corners on the round-rectangle.
     * @param roundedCornerYRadius The y-radius of the rounded corners on the round-rectangle.
     */
    private fun createClockHand(
        length: Float,
        thickness: Float,
        gapBetweenHandAndCenter: Float,
        roundedCornerXRadius: Float,
        roundedCornerYRadius: Float
    ): Path {
        val width = bounds.width()
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val left = centerX - thickness / 2 * width
        val top = centerY - (gapBetweenHandAndCenter + length) * width
        val right = centerX + thickness / 2 * width
        val bottom = centerY - gapBetweenHandAndCenter * width
        val path = Path()

        if (roundedCornerXRadius != 0.0f || roundedCornerYRadius != 0.0f) {
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                roundedCornerXRadius,
                roundedCornerYRadius,
                Path.Direction.CW
            )
        } else {
            path.addRect(
                left, top, right, bottom, Path.Direction.CW
            )
        }
        return path
    }


    private fun drawClockHands(
        zonedDateTime: ZonedDateTime
    ) {
        // Only recalculate bounds (watch face size/surface) has changed or the arm of one of the
        // clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time (when the size of the device is
        // initially broadcasted).
        if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
            armLengthChangedRecalculateClockHands = false
            currentWatchFaceSize = bounds
            recalculateClockHands()
        }

        // Retrieve current time to calculate location/rotation of watch arms.
        val secondOfDay = zonedDateTime.toLocalTime().toSecondOfDay()

        // Determine the rotation of the hour and minute hand.

        // Determine how many seconds it takes to make a complete rotation for each hand
        // It takes the hour hand 12 hours to make a complete rotation
        val secondsPerHourHandRotation = Duration.ofHours(12).seconds
        // It takes the minute hand 1 hour to make a complete rotation
        val secondsPerMinuteHandRotation = Duration.ofHours(1).seconds

        // Determine the angle to draw each hand expressed as an angle in degrees from 0 to 360
        // Since each hand does more than one cycle a day, we are only interested in the remainder
        // of the secondOfDay modulo the hand interval
        val hourRotation =
            secondOfDay.rem(secondsPerHourHandRotation) * 360.0f / secondsPerHourHandRotation
        val minuteRotation =
            secondOfDay.rem(secondsPerMinuteHandRotation) * 360.0f / secondsPerMinuteHandRotation

        canvas.withScale(
            x = WATCH_HAND_SCALE,
            y = WATCH_HAND_SCALE,
            pivotX = bounds.exactCenterX(),
            pivotY = bounds.exactCenterY()
        ) {
            val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT

            clockHandPaint.style = if (drawAmbient) Paint.Style.STROKE else Paint.Style.FILL
            clockHandPaint.color = if (drawAmbient) {
                watchFaceColors.ambientPrimaryColor
            } else {
                watchFaceColors.activePrimaryColor
            }

            // Draw hour hand.
            withRotation(hourRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                drawPath(hourHandBorder, clockHandPaint)
            }

            // Draw minute hand.
            withRotation(minuteRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                drawPath(minuteHandBorder, clockHandPaint)
            }

            // Draw second hand if not in ambient mode
            if (!drawAmbient) {
                clockHandPaint.color = watchFaceColors.activeSecondaryColor

                // Second hand has a different color style (secondary color) and is only drawn in
                // active mode, so we calculate it here (not above with others).
                val secondsPerSecondHandRotation = Duration.ofMinutes(1).seconds
                val secondsRotation =
                    secondOfDay.rem(secondsPerSecondHandRotation) * 360.0f / secondsPerSecondHandRotation
                clockHandPaint.color = watchFaceColors.activeSecondaryColor

                withRotation(secondsRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                    drawPath(secondHand, clockHandPaint)
                }
            }
        }
    }

    /*
     * Rarely called (only when watch face surface changes; usually only once) from the
     * drawClockHands() method.
    */
    private fun recalculateClockHands() {
        Log.d(TAG, "recalculateClockHands()")
        hourHandBorder = createClockHand(
            watchFaceData.hourHandDimensions.lengthFraction,
            watchFaceData.hourHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.hourHandDimensions.xRadiusRoundedCorners,
            watchFaceData.hourHandDimensions.yRadiusRoundedCorners
        )
        hourHandFill = hourHandBorder

        minuteHandBorder = createClockHand(
            watchFaceData.minuteHandDimensions.lengthFraction,
            watchFaceData.minuteHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.minuteHandDimensions.xRadiusRoundedCorners,
            watchFaceData.minuteHandDimensions.yRadiusRoundedCorners
        )
        minuteHandFill = minuteHandBorder

        secondHand = createClockHand(
            watchFaceData.secondHandDimensions.lengthFraction,
            watchFaceData.secondHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.secondHandDimensions.xRadiusRoundedCorners,
            watchFaceData.secondHandDimensions.yRadiusRoundedCorners
        )
    }

    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f


    }
}
