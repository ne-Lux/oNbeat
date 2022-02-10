package com.android.samples.photic

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.android.samples.photic.viewmodels.GalleryFragmentViewModel
import java.text.SimpleDateFormat

class DateTimePickerFragment : DialogFragment(), OnDateSetListener{
    private val viewModel: GalleryFragmentViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dpd = DatePickerDialog(requireContext())
        dpd.setOnDateSetListener { view, year, month, dayOfMonth -> onDateSet(view, year, month, dayOfMonth) }
        return dpd
    }

    @SuppressLint("SimpleDateFormat")
    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {

        val month = monthOfYear + 1
        val dayStr = dayOfMonth.toString()
        val monthStr = month.toString()
        val yearStr = year.toString()
        val dateStr = "$dayStr.$monthStr.$yearStr 12:00:00"
        val date = SimpleDateFormat("d.M.yyyy HH:mm:ss").parse(dateStr)

        val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val dateFormat = SimpleDateFormat("dd.MM.yy")

        var finalDateStr = ""
        var justDateStr = ""
        if (date != null){
            finalDateStr = dateTimeFormat.format(date)
            justDateStr = dateFormat.format(date)
        }
        setFragmentResult("requestKey", bundleOf("finalDate" to finalDateStr, "justDate" to justDateStr))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setdateSelect(false)
        viewModel.setbyCalendar(tag = false)
        setFragmentResult("destroyedDPD", bundleOf("tag" to false))

    }
}