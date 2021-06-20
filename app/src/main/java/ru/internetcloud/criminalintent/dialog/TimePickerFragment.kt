package ru.internetcloud.criminalintent.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class TimePickerFragment: DialogFragment() {

    // интерфейс
    interface Callbacks {
        fun onTimeSelected(date: Date)
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_DATE) as Date

        val calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]

        val timeListener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker,
                                                                newHour: Int,
                                                                newMinute: Int ->

            calendar.set(Calendar.HOUR_OF_DAY, newHour)
            calendar.set(Calendar.MINUTE, newMinute)
            calendar.set(Calendar.SECOND, 0)

            val resultDate: Date = calendar.getTime()
            targetFragment?.let { fragment ->
                (fragment as TimePickerFragment.Callbacks).onTimeSelected(resultDate)
            }
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            hour,
            minute,
            true
        )
    }
}