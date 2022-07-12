package com.example.timekeeper.activities.main

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
import com.example.timekeeper.viewmodel.MainViewModel
import com.google.android.material.card.MaterialCardView

class SelectGameFragment : Fragment() {

    private val SAVED_STATE_CHECKED_MC_ID: String = "SAVED_STATE_CHECKED_MC_ID"

    private var _binding: FragmentSelectGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val logTag = "SelectGameFragment"
    private var _viewModel: MainViewModel? = null
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

        _viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding.apply {
            gameNotes.setOnClickListener(onClickListener)
            gameNumbers.setOnClickListener(onClickListener)
            gameNull.setOnClickListener(onClickListener)

            openGame.setOnClickListener(goToNextFragment)
        }

        // check MatCard
        if (_viewModel!!.lockGameId!=-1) requireActivity().findViewById<MaterialCardView>(_viewModel!!.lockGameId).performClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val onClickListener = View.OnClickListener {
        it as MaterialCardView
        if (!matCards.contains(it)) matCards.add(it)
        it.isChecked = !it.isChecked

        //uncheck other matCards
        val checkedMatCard = matCards.filter { mc -> mc.isChecked }
        checkedMatCard.filterNot { mc -> mc == it }.forEach { cMC -> cMC.isChecked = false }
        // get id of checked mat card and set UI
        _viewModel!!.lockGameId = checkedMatCard.first {mc -> mc.isChecked }.id
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

   /* override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_STATE_CHECKED_MC_ID, checkedMCid)
    }*/
}