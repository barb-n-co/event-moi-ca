package com.example.event_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.viewmodel.AddEventFragmentViewModel
import kotlinx.android.synthetic.main.fragment_add_event.*
import org.kodein.di.generic.instance
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class AddEventFragment : BaseFragment() {

    var dateStart: Date? = null
    var dateEnd: Date? = null
    var startDateTimePicker = Calendar.getInstance()
    var endDateTimePicker = Calendar.getInstance()
    private val viewModel: AddEventFragmentViewModel by instance(arg = this)

    companion object {
        const val TAG = "ADDEVENTFRAGMENT"
        const val startDateCode = 1
        const val startTimeCode = 2
        const val endDateCode = 3
        const val endTimeCode = 4
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
            date.setTargetFragment(this, startDateCode)
            date.show(fragmentManager!!, tag)
        }

        chip_date_end_add_event_fragment.setOnClickListener {
            val date = DatePickerFragment()
            date.setTargetFragment(this, endDateCode)
            date.show(fragmentManager!!, tag)
        }

        b_validate_add_event_fragment.setOnClickListener {
            val id = java.util.UUID.randomUUID().toString()
            val organizer = et_organizer_add_event_fragment.text.toString()
            val name = et_name_add_event_fragment.text.toString()
            val description = et_description_add_event_fragment.text.toString()
            val startDateString = getDateToString(dateStart)
            val endDateString = getDateToString(dateEnd)

            if(dateEnd != null && dateStart != null && organizer.isNotEmpty() && name.isNotEmpty()){
                viewModel.addEventFragment(id, organizer, name, description, startDateString, endDateString)
                fragmentManager?.popBackStack()
            } else {
                Toast.makeText(context, getString(R.string.error_empty_field_add_event_fragment), Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            startDateCode -> {
                startDateTimePicker = data?.extras?.get("args") as Calendar
                val date = TimePickerFragment()
                date.setTargetFragment(this, startTimeCode)
                date.show(fragmentManager!!, tag)
            }
            startTimeCode -> {
                val calendar = data?.extras?.get("args") as Calendar
                startDateTimePicker.set(Calendar.HOUR, calendar.time.hours)
                startDateTimePicker.set(Calendar.MINUTE, calendar.time.minutes)
                dateStart = startDateTimePicker.time
                chip_date_start_add_event_fragment.text = getDateToString(startDateTimePicker.time)
            }
            endDateCode -> {
                endDateTimePicker = data?.extras?.get("args") as Calendar
                val date = TimePickerFragment()
                date.setTargetFragment(this, endTimeCode)
                date.show(fragmentManager!!, tag)
            }
            endTimeCode -> {
                val calendar = data?.extras?.get("args") as Calendar
                endDateTimePicker.set(Calendar.HOUR, calendar.time.hours)
                endDateTimePicker.set(Calendar.MINUTE, calendar.time.minutes)
                dateEnd = endDateTimePicker.time
                chip_date_end_add_event_fragment.text = getDateToString(endDateTimePicker.time)
            }
        }
    }

    private fun getDateToString(date: Date?): String {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        return if (date != null) df.format(date) else "Erreur test"
    }

}