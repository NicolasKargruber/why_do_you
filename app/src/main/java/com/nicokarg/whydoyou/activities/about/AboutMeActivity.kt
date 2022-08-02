package com.nicokarg.whydoyou.activities.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.nicokarg.whydoyou.BuildConfig
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.databinding.ActivityAboutMeBinding
import java.util.*

class AboutMeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutMeBinding

    private val logTag = "AboutMeActivity"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutMeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            setSupportActionBar(toolbar) // to interact with icon in toolbar
            // give the back button functionality
            toolbar.setNavigationOnClickListener {
                Log.d(logTag, "Navigation icon clicked")
                onBackPressed()
            }

            aboutMeGithub.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.uri_github_nicolas_kargruber))
                    )
                )
            }
            aboutMeLinkedin.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.uri_linkedin_nicolas_kargruber))
                    )
                )
            }
            aboutMeAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        }
    }
}