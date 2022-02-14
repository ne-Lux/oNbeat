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

class DateDialogFragment: DialogFragment(R.layout.datedialog_fragment) {

    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private val dateTimePicker = DateTimePickerFragment()
    private lateinit var binding: DatedialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DatedialogFragmentBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sByDate.setOnClickListener{ onCalendarClick() }
        binding.sByImage.setOnClickListener { onImageClick() }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.setdateSelect(false)
        setFragmentResult("destroyedDD", bundleOf("tag" to false))
    }

    private fun onImageClick(){
        viewModel.setbyImage(true)
        dismiss()
    }

    private fun onCalendarClick(){
        dateTimePicker.show(parentFragmentManager,"DateTimePicker_tag")
        dismiss()
    }
}

