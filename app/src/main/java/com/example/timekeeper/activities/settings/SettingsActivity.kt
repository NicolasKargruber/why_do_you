package com.example.timekeeper.activities.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.timekeeper.R


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // below line is to change
        // the title of our action bar.
//        supportActionBar!!.title = "Settings"

        // below line is used to check if
        // frame layout is empty or not.
        if (findViewById<View?>(R.id.idFrameLayout) != null) {
            if (savedInstanceState != null) {
                return
            }
            // below line is to inflate our fragment.
            supportFragmentManager.beginTransaction().add(R.id.idFrameLayout, SettingsFragment())
                .commit()
        }
    }
}
