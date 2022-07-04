package com.example.timekeeper.data

import android.graphics.drawable.Drawable

data class App(
    val name: String,
    val icon: Drawable,
    val packageName: String,
    val isLocked: Boolean
)