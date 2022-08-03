package com.nicokarg.whydoyou.activities.games

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.lock.LockScreenActivity
import com.nicokarg.whydoyou.alertdialog.SelectActivityAD
import com.nicokarg.whydoyou.databinding.FragmentActivitiesBinding
import com.nicokarg.whydoyou.model.ListActivity
import com.nicokarg.whydoyou.viewmodel.ActivitiesViewModel
import kotlin.Long
import kotlin.Unit
import kotlin.apply


class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private var _viewModel: ActivitiesViewModel? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    private val parentIsLock get() = requireActivity().javaClass == LockScreenActivity::class.java

    private val logTag = "ActivitiesFragment"

    private val activityIsSetUp: Boolean get() = viewModel.selectedActivity != null && viewModel.getTimeString() != null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity())[ActivitiesViewModel::class.java]

        // shared preferences
        viewModel.sharedPreferences = requireActivity().getSharedPreferences(
            resources.getString(R.string.MY_PREFS), AppCompatActivity.MODE_PRIVATE
        )

        initActivities() // load activities icons into viewModel

        binding.apply {
            // display custom alert dialog on click
            activitiesMcActivity.setOnClickListener {
                SelectActivityAD(
                    requireContext(),
                    viewModel.activitiesList!!
                ) { p -> onClick(p) }.show()
            }
            // display time picker on click
            activityTvTimer.setOnClickListener {
                activitiesTvTimerInstruction.isInvisible = true
                val tp = viewModel.getTime()
                createTimerDialog(tp) // displays the time picker
            }
            activityBtnStartTimer.setOnClickListener { startTimeClicked() }

            if (parentIsLock) {
                (requireActivity() as LockScreenActivity).initIcon(wdyInclude.wdyTextWhatApp)
                // set up timer and
                viewModel.apply { // reset timer to what it was
                    val prevTimer = sharedPreferences!!.getInt(
                        resources.getString(R.string.ACTIVITY_TIMER),
                        0 // 0 millis is default
                    )
                    Log.d(logTag,"received time: ${getTimeString()}")
                    setTimeInMillis(prevTimer)
                    val pos = sharedPreferences!!.getInt(
                        resources.getString(R.string.ACTIVITY_POS),
                        -1 // -1 is default position
                    )
                    Log.d(logTag,"received position: $pos")
                    selectedActivityPosition = pos
                    if (prevTimer > 0 && pos > -1) {
                        onClick(pos)
                        startTimeClicked() // activityTvTimer.text gets set and updated here
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (parentIsLock) viewModel.apply { // save current timer in LockScreenActivity
            sharedPreferences!!.edit().putInt(
                resources.getString(R.string.ACTIVITY_TIMER),
                getTimeInMillis()
            ).apply() // edit time
            Log.d(logTag,"Save time: ${getTimeString()}")
            sharedPreferences!!.edit().putInt(
                resources.getString(R.string.ACTIVITY_POS),
                selectedActivityPosition
            ).apply() // edit activity position
            Log.d(logTag,"Save position: $selectedActivityPosition")
        }
        Log.d(logTag, "Fragment destroyed")
    }

    private fun onClick(pos: Int) {
        viewModel.apply {
//            if (activitiesList == null) return
            selectedActivityPosition = pos
            updateActivityMatCard()
        }
    }

    private fun startTimeClicked() {
        disableViews() // timer starts and should not be changeable
        val tim = viewModel.getTimeInMillis()
        runCountdown(tim) // runs countdown
    }

    private fun updateActivityMatCard() {
        binding.apply {
            viewModel.apply {
                activitiesTvActivityInstruction.isInvisible = selectedActivity != null
                activitiesActivityIcon.setImageDrawable(selectedActivity!!.icon)
                activitiesActivityMessage.text = selectedActivity!!.text
                activityBtnStartTimer.isEnabled = activityIsSetUp
            }
        }
    }

    private fun disableViews() {
        binding.apply {
            activitiesMcActivity.isClickable = false
            activityTvTimer.isClickable = false
            activityBtnStartTimer.isEnabled = false
        }
    }

    private fun runCountdown(tim: Int) {
        object : CountDownTimer(tim.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.activityTvTimer.text = viewModel.oneSecPassed() ?: "00:00" //
            }
            override fun onFinish() {
                showSuccessAndQuit()
            }
        }.start()
    }

    private fun createTimerDialog(tp: Pair<Int, Int>): Unit {
        binding.apply {
            val mTimePicker = TimePickerDialog(
                requireContext(),
                { _, minute, seconds ->
                    viewModel.setTime(minute, seconds)
                    activityTvTimer.text = viewModel.getTimeString() ?: "00:00"
                    activityBtnStartTimer.isEnabled = activityIsSetUp
                },
                tp.first, // set minutes
                tp.second, // set seconds
                true
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Minutes : Seconds")
            mTimePicker.show()
        }
    }

    private fun showSuccessAndQuit() {
        Snackbar.make(
            requireView(),
            getString(R.string.task_completed),
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
        viewModel.apply { // edit activity and time
            sharedPreferences!!.edit().putInt(
                resources.getString(R.string.ACTIVITY_TIMER),
                0
            ).apply()// edit time
            sharedPreferences!!.edit().putInt(
                resources.getString(R.string.ACTIVITY_POS),
                -1
            ).apply() // edit activity position
        }
        if (parentIsLock) {
//            binding.fabAddNote.isEnabled = false
            (activity as LockScreenActivity).updateLastLocked()
            Handler(Looper.getMainLooper()).postDelayed({
                requireActivity().finishAffinity()
            }, 3000)
        }
    }

    private fun initActivities() {
        viewModel.activitiesList = listOf(
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_directions_run
                )!!, "Go for a run"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_laundry
                )!!, "Do laundry"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_auto_stories
                )!!, "Read book"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_dishwasher_gen
                )!!, "Empty dish washer"
            ),
        )
    }
}