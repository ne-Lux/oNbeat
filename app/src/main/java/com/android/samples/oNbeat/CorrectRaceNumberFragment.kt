package com.android.samples.oNbeat

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.android.samples.oNbeat.data.RaceResult
import com.android.samples.oNbeat.databinding.CorrectRaceNumberBinding
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel

class CorrectRaceNumberFragment: DialogFragment (R.layout.correct_race_number){
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private lateinit var binding: CorrectRaceNumberBinding

    //Initiate the binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CorrectRaceNumberBinding.inflate(inflater, container,false)
        val raceNumber = arguments?.getInt("raceNumber")
        val filteredRaceNumber = viewModel.results.value!!.indexOfFirst { it.raceNumber == raceNumber}
        if (filteredRaceNumber != -1){
            setResult(viewModel.results.value!![filteredRaceNumber])
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Set onClickListener for all buttons
        binding.button.setOnClickListener{ onApplyClick() }
    }

    //ClickHandler for imagebutton
    private fun onApplyClick(){
        //Store date selection mode
        //close this dialog
        dismiss()
    }
    private fun setResult(raceResult: RaceResult){
        binding.startImage.setImageURI(raceResult.contentUriStart)
        binding.finishImage.setImageURI(raceResult.contentUriFinish)
    }
}