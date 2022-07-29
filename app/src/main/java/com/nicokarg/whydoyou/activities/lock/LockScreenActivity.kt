package com.nicokarg.whydoyou.activities.lock

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.games.ActivitiesFragment
import com.nicokarg.whydoyou.activities.games.NotesFragment
import com.nicokarg.whydoyou.activities.games.PuzzleFragment
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityLockScreenBinding
import com.nicokarg.whydoyou.viewmodel.LockScreenActivityViewModel
import com.nicokarg.whydoyou.viewmodel.NotesViewModel
import kotlinx.android.synthetic.main.fragment_notes.*
import java.util.*

class LockScreenActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLockScreenBinding
    private var _viewModel: LockScreenActivityViewModel? = null

    private val logTag = "LockScreenActivity"
    val LOCK_SCREEN_FRAGMENT_TAG = "LOCK_SCREEN_FRAGMENT_TAG"
    var lockScreenFragment:Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _viewModel = ViewModelProvider(this)[LockScreenActivityViewModel::class.java]

        _viewModel!!.apply {

            // SQLite Database
            dbHandler = DBHandler(this@LockScreenActivity) // creating a new DBHandler class

            sharedPreferences = getSharedPreferences(
                resources.getString(R.string.MY_PREFS), MODE_PRIVATE
            )
            lockGameId = sharedPreferences!!.getInt(
                resources.getString(R.string.CHECKED_MC_CARD), -1
            ) // -1 is default
            lockedPackage = sharedPreferences!!.getString(
                resources.getString(R.string.LOCKED_PACKAGE),
                ""
            )!!
            Log.d(logTag, "this is the found package: $lockedPackage")
            findLockedAppIcon() // finds app icon drawable in db
        }

        // Adding a Fragment instance to the FragmentContainerView
        lockScreenFragment = getLockScreenFragment
        if(lockScreenFragment!=null) supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_lock_screen, lockScreenFragment!!,LOCK_SCREEN_FRAGMENT_TAG)
            .commit()
    }

    fun initIcon(tv:TextView) {
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,_viewModel!!.appIcon,null)
    }

    private val getLockScreenFragment: Fragment?
        get() = when (_viewModel!!.lockGameId) {
            R.id.game_numbers -> PuzzleFragment()
            R.id.game_notes -> NotesFragment()
            R.id.game_activities -> ActivitiesFragment()
            else -> null
        }

    // closes the application and shows the home screen of the device:
    override fun onBackPressed() {
        super.onBackPressed()
        val startHomeScreen = Intent(Intent.ACTION_MAIN)
        startHomeScreen.addCategory(Intent.CATEGORY_HOME)
        startHomeScreen.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(startHomeScreen) // show home screen
    }

    // makes sure main activity appears when app is clicked
    override fun onPause() {
        super.onPause()
        this.finish() // close lock screen activity
    }

    fun updateLastLocked() {
        _viewModel!!.apply {
            dbHandler!!.updateLastLockedOfApp(lockedPackage, Calendar.getInstance().time)
            sharedPreferences!!.edit().putString(
                resources.getString(R.string.LOCKED_PACKAGE),
                ""
            ).apply()
            Log.d(logTag, "Updated locked package preference")
        }
    }
}