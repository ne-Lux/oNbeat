package com.android.samples.oNbeat

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel
import java.text.SimpleDateFormat
/*
DialogFragment for DateTimePicker (only DatePicker is used at the moment due to requirements)
 */
class DateTimePickerFragment : DialogFragment(), OnDateSetListener{
    //Initiate the ViewModel
    private val viewModel: GalleryFragmentViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Create a new DatePickerDialog and register an onDateSetListener
        val dpd = DatePickerDialog(requireContext())
        dpd.setOnDateSetListener { view, year, month, dayOfMonth -> onDateSet(view, year, month, dayOfMonth) }
        return dpd
    }

    @SuppressLint("SimpleDateFormat")
    //OnDateSetListener
    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        //Get the date components
        val month = monthOfYear + 1
        val dayStr = dayOfMonth.toString()
        val monthStr = month.toString()
        val yearStr = year.toString()
        val dateStr = "$dayStr.$monthStr.$yearStr 12:00:00"
        val date = SimpleDateFormat("d.M.yyyy HH:mm:ss").parse(dateStr)

        //Create DateTimeFormatter
        val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val dateFormat = SimpleDateFormat("dd.MM.yy")

        //Format the Date
        val finalDateStr = dateTimeFormat.format(date!!)
        val justDateStr = dateFormat.format(date)

        //Hand the Date to the GalleryFragment
        setFragmentResult("requestKey", bundleOf("finalDate" to finalDateStr, "justDate" to justDateStr))
    }

    //On dialog cancel
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        //deactivate the date selection mode
        viewModel.setdateSelect(false)
        //Create a fragment result, so that the ic_start/stop_calendar can be restored (not clicked)
        setFragmentResult("destroyedDPD", bundleOf("tag" to false))
    }
}