package com.nicokarg.whydoyou.activities.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.games.ActivitiesFragment
import com.nicokarg.whydoyou.activities.games.NotesFragment
import com.nicokarg.whydoyou.activities.games.PuzzleFragment
import com.nicokarg.whydoyou.activities.settings.SettingsFragment
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityAboutMeBinding
import com.nicokarg.whydoyou.databinding.ActivityLockScreenBinding
import com.nicokarg.whydoyou.viewmodel.LockScreenActivityViewModel
import java.util.*

class AboutMeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutMeBinding

    private val logTag = "AboutMeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutMeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            setSupportActionBar(toolbar) // to interact with icon in toolbar
            // give the back button functionality
            toolbar.setNavigationOnClickListener {
                Log.d(logTag,"Navigation icon clicked")
                onBackPressed()
            }
        }
    }
}