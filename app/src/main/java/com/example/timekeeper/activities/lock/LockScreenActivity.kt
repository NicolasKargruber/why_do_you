package com.example.timekeeper.activities.lock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.timekeeper.R
import com.example.timekeeper.activities.games.NotesFragment
import com.example.timekeeper.activities.games.PuzzleFragment
import com.example.timekeeper.broadcast.Restarter
import com.example.timekeeper.databinding.ActivityLockScreenBinding
import com.example.timekeeper.databinding.ActivityMainBinding

class LockScreenActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLockScreenBinding

    var sharedPreferences: SharedPreferences? = null
    var lockGameId: Int = -1
    private val CHECKED_MC_ID: String = "CHECKED_MC_ID"

    private val logTag = "LockScreenActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(CHECKED_MC_ID, MODE_PRIVATE)
        lockGameId = sharedPreferences!!.getInt("id", -1) // -1 is default

        // Adding a Fragment instance to the FragmentContainerView
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_lock_screen, lockScreenFragment)
            .commit()

    }

    fun initIconApp(): Unit {

    }

    private val lockScreenFragment: Fragment
        get() = when (lockGameId) {
                R.id.game_numbers -> PuzzleFragment()
                R.id.game_notes -> NotesFragment()
                else -> PuzzleFragment() // default fragment
            }

    override fun onBackPressed() {
        super.onBackPressed()

        // If the password is incorrect then simply use the code below
        // closes the application and shows the home screen of the device:
        val startHomeScreen = Intent(Intent.ACTION_MAIN)
        startHomeScreen.addCategory(Intent.CATEGORY_HOME)
        startHomeScreen.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(startHomeScreen)
    }
}