package com.nicokarg.whydoyou.activities.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.databinding.ActivityMainBinding
import com.nicokarg.whydoyou.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private val logTag: String = "SettingsActivity"
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // below line is used to check if
            // frame layout is empty or not.
            if (savedInstanceState == null)
            // below line is to inflate our fragment.
            supportFragmentManager.beginTransaction()
                .add(R.id.idFrameLayout, SettingsFragment())
                .commit()

            setSupportActionBar(toolbar) // to interact with icon in toolbar
            // give the back button functionality
            toolbar.setNavigationOnClickListener {
                Log.d(logTag,"Navigation icon clicked")
                onBackPressed()
            }
        }

    }
}
