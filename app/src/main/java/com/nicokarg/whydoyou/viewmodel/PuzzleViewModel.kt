package com.nicokarg.whydoyou.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton

class PuzzleViewModel : ViewModel() {
    var currentNum = MutableLiveData<Int>(-2)
}