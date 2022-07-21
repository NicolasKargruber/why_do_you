package com.nicokarg.whydoyou.activities.lock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.games.NotesFragment
import com.nicokarg.whydoyou.activities.games.PuzzleFragment
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityLockScreenBinding
import java.util.*

class LockScreenActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLockScreenBinding

    var sharedPreferences: SharedPreferences? = null

    //ToDo create viewModel for it
    var lockGameId: Int = -1
    var lockedPackage = ""


    private var dbHandler: DBHandler? = null

    private val logTag = "LockScreenActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SQLite Database
        dbHandler = DBHandler(this) // creating a new DBHandler class

        sharedPreferences = getSharedPreferences(
            resources.getString(R.string.MY_PREFS)
            , MODE_PRIVATE)
        lockGameId = sharedPreferences!!.getInt(
            resources.getString(R.string.CHECKED_MC_CARD)
            , -1) // -1 is default
        lockedPackage = sharedPreferences!!.getString(
            resources.getString(R.string.LOCKED_PACKAGE),
            ""
        )!!
        Log.d(logTag,"this is the found package: $lockedPackage")
        // Adding a Fragment instance to the FragmentContainerView
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_lock_screen, lockScreenFragment)
            .commit()

//        binding.fragmentContainerLockScreen.findViewById<TextView>(R.id.wdy_text_what_app).apply {
//            setCompoundDrawablesRelative(null,null,icon,null)
//        }
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

    fun updateLastLocked() {
        dbHandler!!.updateLastLockedOfApp(lockedPackage,Calendar.getInstance().time)
        sharedPreferences!!.edit().putString(
            resources.getString(R.string.LOCKED_PACKAGE),
            ""
        ).apply()
        Log.d(logTag,"Updated locked package preference")
    }
}