package com.example.android.wearable.alpha

import android.util.Log
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.WatchFace

class TapEvent: WatchFace.TapListener {
    override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
        Log.d("On tap event", "")
    }
}
