package com.example.timekeeper.activities.main

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.timekeeper.R
import com.example.timekeeper.databinding.FragmentSelectGameBinding
import com.example.timekeeper.viewmodel.SelectGameViewModel
import com.google.android.material.card.MaterialCardView


class SelectGameFragment : Fragment() {

    private val CHECKED_MC_ID: String = "CHECKED_MC_ID"

    private var _binding: FragmentSelectGameBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: SelectGameViewModel? = null

    private val logTag = "SelectGameFragment"
    private var sharedPreferences: SharedPreferences? = null

    private var matCards = mutableListOf<MaterialCardView>()
    // private var checkedMCid: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*if (savedInstanceState != null) {
            checkedMCid = savedInstanceState.getInt(SAVED_STATE_CHECKED_MC_ID)
        }*/

        _viewModel = ViewModelProvider(requireActivity()).get(SelectGameViewModel::class.java)

        // MY_PREFS_NAME - a static String variable like:
//public static final String MY_PREFS_NAME = "MyPrefsFile";
        // MY_PREFS_NAME - a static String variable like:
//public static final String MY_PREFS_NAME = "MyPrefsFile";
        sharedPreferences = requireActivity().getSharedPreferences(CHECKED_MC_ID, MODE_PRIVATE)
        _viewModel!!.lockGameId = sharedPreferences!!.getInt("id", -1) // -1 is default

        binding.apply {
            gameNotes.setOnClickListener(onClickListener)
            gameNumbers.setOnClickListener(onClickListener)
            gameNull.setOnClickListener(onClickListener)

            openGame.setOnClickListener(goToNextFragment)
        }

        // check MatCard
        if (_viewModel!!.lockGameId != -1) requireActivity().findViewById<MaterialCardView>(
            _viewModel!!.lockGameId
        ).performClick()

    }

    override fun onDestroyView() {
        val editor = sharedPreferences!!.edit()
        editor.putInt("id", _viewModel!!.lockGameId)
        editor.apply()
        super.onDestroyView()
        _binding = null
    }

    private val onClickListener = View.OnClickListener {
        it as MaterialCardView
        if (!matCards.contains(it)) matCards.add(it)
        if(!it.isChecked) it.isChecked = true // cannot be unchecked by click

        val checkedMatCard = matCards.filter { mc -> mc.isChecked }
        checkedMatCard.filterNot { mc -> mc == it }
            .forEach { cMC -> cMC.isChecked = false } //uncheck other matCards
        // get id of checked mat card and set UI
        _viewModel!!.lockGameId = checkedMatCard.first { mc -> mc.isChecked }.id
        binding.openGame.isVisible = checkedMatCard.isNotEmpty()
    }

    private val goToNextFragment = View.OnClickListener {
        val action = when (_viewModel!!.lockGameId) {
            R.id.game_numbers -> R.id.action_MainFragment_to_PuzzleFragment
            R.id.game_notes -> R.id.action_MainFragment_to_notesFragment
            else -> -1
        }
        if (action != -1) findNavController().navigate(action)
    }
}