package com.android.samples.photic

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult

import com.android.samples.photic.databinding.DatedialogFragmentBinding

import com.android.samples.photic.viewmodels.GalleryFragmentViewModel
/*
DialogFragment to choose the date selection mode
 */
class DateDialogFragment: DialogFragment(R.layout.datedialog_fragment) {
    //Initiate ViewModel, binding and other variables
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private val dateTimePicker = DateTimePickerFragment()
    private lateinit var binding: DatedialogFragmentBinding

    //Initiate the binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DatedialogFragmentBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Set onClickListener for all buttons
        binding.sByDate.setOnClickListener{ onCalendarClick() }
        binding.sByImage.setOnClickListener { onImageClick() }
    }

    //On dialog cancel
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        //deactivate the date selection mode
        viewModel.setdateSelect(false)
        //Create a fragment result, so that the ic_start/stop_calendar can be restored (not clicked)
        setFragmentResult("destroyedDD", bundleOf("tag" to false))
    }

    //ClickHandler for imagebutton
    private fun onImageClick(){
        //Store date selection mode
        viewModel.setbyImage(true)
        //close this dialog
        dismiss()
    }

    //ClickHandler for calendarbutton
    private fun onCalendarClick(){
        //Show the DateTimePickerDialog
        dateTimePicker.show(parentFragmentManager,"DateTimePicker_tag")
        //close this dialog
        dismiss()
    }
}

