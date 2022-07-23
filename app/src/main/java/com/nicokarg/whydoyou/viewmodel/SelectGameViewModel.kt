package com.nicokarg.whydoyou.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.android.material.card.MaterialCardView

class SelectGameViewModel:ViewModel() {
    var matCards = mutableListOf<MaterialCardView>()
    var sharedPreferences: SharedPreferences? = null
    var lockGameId: Int = -1
}