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

    val activityIsSetUp: Boolean get() = viewModel.selectedActivity != null && viewModel.getTimeString() != null

    var activitiesList: List<ListActivity>? = null

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

        initActivities()

        binding.apply {
            activitiesMcActivity.setOnClickListener {
                SelectActivityAD(requireContext(), activitiesList!!) { p -> onClick(p) }.show()
            }
            activityTvTimer.setOnClickListener {
                activitiesTvTimerInstruction.isInvisible = true

                val tp = viewModel.getTime()
                val mTimePicker = TimePickerDialog(
                    requireContext(),
                    { timePicker, minute, seconds ->
                        viewModel.setTime(minute, seconds)
                        activityTvTimer.text = viewModel.getTimeString() ?: "-:--"
                        activityBtnStartTimer.isEnabled = activityIsSetUp
                    },
                    tp.first, // set minutes
                    tp.second, // set seconds
                    true
                ) //Yes 24 hour time
                mTimePicker.setTitle("Select Minutes : Seconds")
                mTimePicker.show()
            }
            activityBtnStartTimer.setOnClickListener {
                activitiesMcActivity.isClickable = false
                activityTvTimer.isClickable = false
                activityBtnStartTimer.isEnabled = false

                val tim = viewModel.getTimeInMillis()
                object : CountDownTimer(tim.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        activityTvTimer.text = viewModel.oneSecPassed() ?: "-:--"
                    }

                    override fun onFinish() {
                        showSuccessAndQuit()
                    }
                }.start()
            }
            if (parentIsLock) {
                (requireActivity() as LockScreenActivity).initIcon(wdyInclude.wdyTextWhatApp)
            }
        }
    }

    fun onClick(pos: Int) {
        if (activitiesList == null) return
        viewModel.selectedActivity = activitiesList!![pos]
        updateActivityMatCard()
    }

    private fun updateActivityMatCard() {
        binding.let { b ->
            viewModel.apply {
                b.activitiesTvActivityInstruction.isInvisible = selectedActivity != null
                b.activitiesIcon.setImageDrawable(selectedActivity!!.icon)
                b.activitiesTvDescription.text = selectedActivity!!.text
                b.activityBtnStartTimer.isVisible = activityIsSetUp
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }

    private fun showSuccessAndQuit() {
        Snackbar.make(
            requireView(),
            "Task is completed",
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
        if (parentIsLock) {
//            binding.fabAddNote.isEnabled = false
            (activity as LockScreenActivity).updateLastLocked()
            Handler(Looper.getMainLooper()).postDelayed({
                requireActivity().finishAffinity()
            }, 3000)
        }
    }

    private fun initActivities() {
        activitiesList = listOf(
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_directions_run_fill0_wght700_grad0_opsz48
                )!!, "Go for a run"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_laundry_fill0_wght700_grad0_opsz48
                )!!, "Do laundry"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_auto_stories_fill0_wght700_grad0_opsz48
                )!!, "Read book"
            ),
            ListActivity(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_dishwasher_gen_fill0_wght700_grad0_opsz48
                )!!, "Empty dish washer"
            ),
        )
    }
}