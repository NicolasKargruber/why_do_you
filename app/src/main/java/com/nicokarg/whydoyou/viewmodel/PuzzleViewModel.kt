package com.nicokarg.whydoyou.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PuzzleViewModel : ViewModel() {
    var currentNum = MutableLiveData<Int>(-2)
}