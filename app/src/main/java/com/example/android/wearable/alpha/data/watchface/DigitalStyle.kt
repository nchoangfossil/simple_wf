package com.example.android.wearable.alpha.data.watchface

import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.wear.watchface.style.UserStyleSetting
import com.example.android.wearable.alpha.R


private const val SMALL_SIZE = 10.0f
private const val MEDIUM_SIZE = 15.0f
private const val LARGE_SIZE = 20.0f
private const val EXTRA_LARGE_SIZE = 30.0f

const val DIGITAL_SMALL_STYLE = "digital_small_style"
const val DIGITAL_MEDIUM_STYLE = "digital_medium_style"
const val DIGITAL_LARGE_STYLE = "digital_large_style"

private const val DIGITAL_SMALL_STYLE_ID = R.string.small_style
private const val DIGITAL_MEDIUM_STYLE_ID = R.string.medium_style
private const val DIGITAL_LARGE_STYLE_ID = R.string.large_style


enum class DigitalStyle(
    val id: String,
    @StringRes val nameID: Int,
    val textColor: Int,
    val backGroundColor: Int,
    val timeSize: Float,
    val daySize: Float,
    val isAntiAlias: Boolean,
) {
    SMALL(
        id = DIGITAL_SMALL_STYLE,
        nameID = DIGITAL_SMALL_STYLE_ID,
        textColor = Color.RED,
        backGroundColor = Color.GRAY,
        timeSize = MEDIUM_SIZE,
        daySize = SMALL_SIZE,
        isAntiAlias = true,
    ),

    MEDIUM(
        id = DIGITAL_MEDIUM_STYLE,
        nameID = DIGITAL_MEDIUM_STYLE_ID,
        textColor = Color.BLUE,
        backGroundColor = Color.GRAY,
        timeSize = LARGE_SIZE,
        daySize = MEDIUM_SIZE,
        isAntiAlias = true,
    ),

    LARGE(
        id = DIGITAL_LARGE_STYLE,
        nameID = DIGITAL_LARGE_STYLE_ID,
        textColor = Color.GREEN,
        backGroundColor = Color.GRAY,
        timeSize = EXTRA_LARGE_SIZE,
        daySize = LARGE_SIZE,
        isAntiAlias = true,
    );

    companion object {
        fun getDigitalStyle(id: String): DigitalStyle {
            return when (id) {
                SMALL.id -> SMALL
                MEDIUM.id -> MEDIUM
                else -> LARGE
            }
        }

        fun toOptionList(context: Context): List<UserStyleSetting.ListUserStyleSetting.ListOption> {
            val backGroundStyleList = enumValues<DigitalStyle>()

            return backGroundStyleList.map { style ->
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(style.id),
                    context.resources,
                    style.nameID,
                    null
                )
            }
        }
    }

}
