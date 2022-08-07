package com.nicokarg.whydoyou.activities.games

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.lock.LockScreenActivity
import com.nicokarg.whydoyou.databinding.FragmentPuzzleBinding
import com.nicokarg.whydoyou.viewmodel.PuzzleViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PuzzleFragment() : Fragment() {

    private var _binding: FragmentPuzzleBinding? = null
    private var _viewModel: PuzzleViewModel? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel

    private val parentIsLock get() = requireActivity().javaClass == LockScreenActivity::class.java

    private val KEY_SHUFFLED = "KEY_SHUFFLED"

    private var numberedButtons = listOf<MaterialButton>()
    private var shuffled = listOf<Int>()

    private val logTag = "PuzzleFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPuzzleBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shuffled = savedInstanceState?.getIntArray(KEY_SHUFFLED)?.toList() ?: listOf()

        _viewModel = ViewModelProvider(requireActivity())[PuzzleViewModel::class.java]

        if (shuffled.isEmpty()) viewModel!!.currentNum.value = -2

        binding.apply {
            numberedButtons = listOf(
                numberButtonTopLeft,
                numberButtonTopMiddle,
                numberButtonTopRight,
                numberButtonMiddleLeft,
                numberButtonCenter,
                numberButtonMiddleRight,
                numberButtonBottomLeft,
                numberButtonBottomMiddle,
                numberButtonBottomRight,
            )
            numberedButtons.forEach {
                it.setOnClickListener(onClick)
                it.strokeColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.number_btn_stroke_color_selector
                )
            }

            // sets text color so that it adapts to "isChecked" of the button
            if (viewModel!!.currentNum.value != -2) setTextColors()

            // shuffles indices and assigns buttons
            shuffleAndAssign()

            puzzleResetButton.setOnClickListener {
                shuffleAndAssign()
                viewModel!!.currentNum.value = -2
                resetTextColors()
                it.isVisible = false

                binding.puzzleTvInstruction.text = getString(R.string.game_numbers_first_instruction)
            }

            // puzzleResetButton.isVisible = !parentIsLock

            if (parentIsLock) {
                (requireActivity() as LockScreenActivity).initIcon(wdyInclude.wdyTextWhatApp)
            }
        }


        viewModel!!.currentNum.observe(
            viewLifecycleOwner
        ) { cn ->
            Log.d(logTag, "current Number: $cn")
            when (cn) {
                -1 -> numberedButtons.forEach { nb -> nb.isChecked = false } // restart
                numberedButtons.size - 1 -> showSuccessAndQuit() // was correct and is completed
                -2 -> numberedButtons.forEach { nb -> nb.isChecked = false } // start
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_SHUFFLED, shuffled.toIntArray())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSuccessAndQuit() {
        Snackbar.make(
            requireView(),
            getString(R.string.task_completed),
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
        binding.puzzleResetButton.isVisible = !parentIsLock
        if(parentIsLock) {
            (activity as LockScreenActivity).updateLastLocked()
            Handler(Looper.getMainLooper()).postDelayed({
                (requireActivity() as LockScreenActivity).revertToMainActivity()
        }, 3000)}
    }

    private fun shuffleAndAssign() {
        if (shuffled.isEmpty()) shuffled = (numberedButtons.indices).shuffled()
        shuffled.forEachIndexed { ind, num ->
            numberedButtons[ind].setNumber(num)
        }
        numberedButtons = numberedButtons.sortedBy { it.getNumber() }
    }

    private fun MaterialButton.setNumber(n: Int) {
        text = (n.plus(1)).toString()
    }

    private fun MaterialButton.getNumber(): Int {
        return text.toString().toInt() - 1
    }

    private fun setTextColors() {
        numberedButtons.forEach {
            it.setTextColor(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.number_btn_text_color_selector
                )
            )
        }
    }

    private fun resetTextColors() {
        numberedButtons.forEach {
            it.setTextColor(
                requireContext().getColorFromAttr(com.google.android.material.R.attr.colorPrimaryVariant)
            )
        }
    }

    private val onClick = View.OnClickListener {
        (it as MaterialButton).getNumber().let { num ->
            binding.puzzleTvInstruction.text = getString(R.string.game_numbers_second_instruction)
            viewModel!!.currentNum.apply {
                //initially it is -2
                if (value!! == -2) {
                    setTextColors()
                    if (num == 0) value = 0
                }
                //if clicked in correct order this case happens
                if (num == value!! + 1) value = num
                //if clicked wrong order this case happens
                else if (num > value!! + 1) value = -1
                //if button was re-clicked this case happens
                if (num <= value!!) it.isChecked = true
            }
        }
    }

    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}