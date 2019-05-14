package com.example.event_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.viewmodel.AddEventFragmentViewModel
import kotlinx.android.synthetic.main.fragment_add_event.*
import org.kodein.di.generic.instance
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE


class AddEventFragment : BaseFragment() {


    var dateStart: Date? = null
    var dateEnd: Date? = null
    var startDateTimePicker: Calendar = Calendar.getInstance()
    var endDateTimePicker: Calendar = Calendar.getInstance()
    private val viewModel: AddEventFragmentViewModel by instance(arg = this)

    companion object {
        const val TAG = "ADDEVENTFRAGMENT"
        public var lieu : String ?=""
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
        setDisplayHomeAsUpEnabled(true)
        setVisibilityNavBar(false)
        setTitleToolbar(getString(R.string.toolbar_add_event_fragment_add_event))

        chip_place_add_event_fragment.setOnClickListener {
            val fragment = MapsFragment.newInstance()
            fragment.setTargetFragment(this, MapsFragment.requestCodeMapFragment)
            fragmentManager?.beginTransaction()
                ?.setCustomAnimations(
                    R.anim.transition_bottom_to_top,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.transition_top_to_bottom_exit
                )
                ?.add(R.id.content_home, fragment, fragment::class.java.name)?.addToBackStack(null)?.commit()
        }
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
            val place = chip_place_add_event_fragment.text.toString()
            val description = et_description_add_event_fragment.text.toString()
            val startDateString = getDateToString(dateStart)
            val endDateString = getDateToString(dateEnd)

            if(dateEnd != null && dateStart != null && organizer.isNotEmpty() && name.isNotEmpty()){
                viewModel.addEventFragment(id, organizer, name, place, description, startDateString, endDateString)
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
                startDateTimePicker.set(HOUR_OF_DAY, calendar.get(HOUR_OF_DAY))
                startDateTimePicker.set(Calendar.MINUTE, calendar.get(MINUTE))
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
                endDateTimePicker.set(HOUR_OF_DAY, calendar.get(HOUR_OF_DAY))
                endDateTimePicker.set(MINUTE, calendar.get(MINUTE))
                dateEnd = endDateTimePicker.time
                chip_date_end_add_event_fragment.text = getDateToString(endDateTimePicker.time)
            }
            MapsFragment.requestCodeMapFragment -> {
                data?.getStringExtra(TAG)?.let {
                    chip_place_add_event_fragment.text = it
                }
            }
        }
    }

    private fun getDateToString(date: Date?): String {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        return if (date != null) df.format(date) else "Erreur test"
    }


}
