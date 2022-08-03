package com.nicokarg.whydoyou.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.services.YourService

class FirstViewModel:ViewModel() {
    // Background Service
    var mServiceIntent: Intent? = null
    var mYourService: YourService? = null
}