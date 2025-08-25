package com.sthao.quickform.ui.stations

import android.graphics.Bitmap
import android.net.Uri

// Represents an item section for the Stations screen UI.
data class StationsItemSection(
    val id: Int = 0,
    val sectionRunNumber: String = "",
    val totes: String = "",
    val addOns: String = "",
    val extra: String = "",
    val printName: String = "",
    val signature: Bitmap? = null,
    val images: List<Uri> = emptyList()
)