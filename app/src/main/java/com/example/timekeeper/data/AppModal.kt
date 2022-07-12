package com.example.timekeeper.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppModal(
    val name: String,
    val icon: Drawable,
    val packageName: String,
    var isLocked: Boolean
) {
    var id = 0
}