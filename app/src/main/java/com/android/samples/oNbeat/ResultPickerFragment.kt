package com.android.samples.oNbeat

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.android.samples.oNbeat.data.RaceResult
import com.android.samples.oNbeat.databinding.CorrectRaceNumberBinding
import com.android.samples.oNbeat.databinding.ResultPickerBinding
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel

class ResultPickerFragment: DialogFragment (R.layout.result_picker){
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private lateinit var binding: ResultPickerBinding
    private lateinit var fileList: ArrayList<String>
    private var chosenFile: String = ""

    //Initiate the binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ResultPickerBinding.inflate(inflater, container,false)
        fileList = arguments?.getStringArrayList("fileList")!!
        val adapter = ArrayAdapter<String>(requireContext(), R.layout.result_file, fileList)
        val listView = binding.listView
        listView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set onClickListener for all buttons
        binding.button.setOnClickListener{ onApplyClick() }
        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { lparent, lview, lposition, lid ->
            chosenFile = binding.listView.adapter.getItem(lposition).toString()
            binding.listView.setItemChecked(lposition, true);
        }

    }

    //ClickHandler for imagebutton
    private fun onApplyClick(){
        if(chosenFile!= "") viewModel.chooseFile(chosenFile)
        dismiss()
    }

}