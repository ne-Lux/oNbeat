package com.android.samples.oNbeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
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
        val adapter = ArrayAdapter(requireContext(), R.layout.result_file, fileList)
        val listView = binding.listView
        listView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set onClickListener for all buttons
        binding.button.setOnClickListener{ onApplyClick() }
        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            chosenFile = binding.listView.adapter.getItem(position).toString()
            binding.listView.setItemChecked(position, true)
        }

    }

    //ClickHandler for Button
    private fun onApplyClick(){
        if(chosenFile!= "") viewModel.chooseFile(chosenFile)
        dismiss()
    }

}