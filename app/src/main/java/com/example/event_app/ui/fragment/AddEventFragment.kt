package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import android.widget.TimePicker
import android.app.TimePickerDialog
import com.example.event_app.ui.activity.MainActivity
import android.widget.DatePicker
import android.app.DatePickerDialog
import android.content.Intent
import kotlinx.android.synthetic.main.fragment_add_event.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class AddEventFragment : BaseFragment() {

    var dateStart: Date? = null
    var dateEnd: Date? = null
    var startDateTimePicker = Calendar.getInstance()
    var endDateTimePicker = Calendar.getInstance()

    companion object {
        const val TAG = "ADDEVENTFRAGMENT"
        const val startDate = 1
        const val startTime = 2
        const val endDate = 3
        const val endTime = 4
        fun newInstance(): AddEventFragment = AddEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chip_date_start_add_event_fragment.setOnClickListener {
            val date = DatePickerFragment()
            date.setTargetFragment(this, startDate)
            date.show(fragmentManager, tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            startDate -> {
                val calendar = data?.extras?.get("args") as Calendar
                dateStart = calendar.time
                val date = TimePickerFragment()
                date.setTargetFragment(this, startTime)
                date.show(fragmentManager, tag)
            }
            startTime -> {
                val calendar = data?.extras?.get("args") as Calendar
                dateStart?.let {
                    startDateTimePicker.set(it.year, it.month, it.day, calendar.time.hours, calendar.time.minutes)
                    chip_date_start_add_event_fragment.text = getDateToString(startDateTimePicker.time)
                }
            }
        }
    }

    private fun getDateToString(date: Date?): String {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        return if (date != null) df.format(date) else "Erreur test"
    }

}