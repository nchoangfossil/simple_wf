package com.example.android.wearable.alpha.data.watchface

import android.content.Context
import androidx.annotation.StringRes
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import com.example.android.wearable.alpha.R
import java.io.IOException
import java.util.*


const val PIC_ONE_ID = "pic_one_id"
const val PIC_TWO_ID = "pic_two_id"
const val PIC_THREE_ID = "pic_three_id"
const val PIC_FOUR_ID = "pic_four_id"
const val PIC_FIVE_ID = "pic_five_id"

enum class BackGroundStyle(
    val id: String,
    @StringRes val nameResourceId: Int,
    val resourcePath: String,
) {
    PIC1(
        id = PIC_ONE_ID,
        nameResourceId = R.drawable.wfbg,
        resourcePath = "wfbg.jpeg"
    ),
    PIC2(
        id = PIC_THREE_ID,
        nameResourceId = R.drawable.wfbg3,
        resourcePath = "wfbg3.png"
    ),
    PIC3(
        id = PIC_FIVE_ID,
        nameResourceId = R.drawable.wfbgg,
        resourcePath = "wfbgg.png"
    );

    companion object {
        fun getBackGroundStyleConfig(id: String): BackGroundStyle {
            return when (id) {
                PIC1.id -> PIC1
                PIC2.id -> PIC2
                else -> PIC3
            }
        }

        fun toOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            val backGroundStyleList = enumValues<BackGroundStyle>()

            return backGroundStyleList.map { bg ->
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(bg.id),
                    context.resources,
                    bg.nameResourceId,
                    null
                )
            }
        }
    }
}
