package com.example.event_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.model.Event
import com.example.event_app.viewmodel.AddEventFragmentViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_edit_detail_event.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE



class EditDetailEventFragment : BaseFragment() {


    var dateStart: Date? = null
    var dateEnd: Date? = null
    var startDateTimePicker: Calendar = Calendar.getInstance()
    var endDateTimePicker: Calendar = Calendar.getInstance()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private val viewModel: AddEventFragmentViewModel by instance(arg = this)

    companion object {
        const val ADDRESS_TAG = "AddressTAG"
        const val LAT_TAG = "LatitudeTAG"
        const val LONG_TAG = "longitudeTAG"
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

        return inflater.inflate(R.layout.fragment_edit_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        setVisibilityNavBar(false)
        setTitleToolbar(getString(R.string.toolbar_add_event_fragment_add_event))

        val eventId = arguments?.let {
            EditDetailEventFragmentArgs.fromBundle(it).eventId
        }

        /*chip_place_edit_event_fragment.setOnClickListener {
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
            fragmentMapIsOpen(true)
        }*/
        chip_date_start_edit_event_fragment.setOnClickListener {
            val date = DatePickerFragment()
            date.setTargetFragment(this, startDateCode)
            date.show(fragmentManager!!, tag)
        }

        chip_date_end_edit_event_fragment.setOnClickListener {
            val date = DatePickerFragment()
            date.setTargetFragment(this, endDateCode)
            date.show(fragmentManager!!, tag)
        }

        eventId?.let {
            viewModel.getEventInfo(it).subscribe(
                {
                    initEditorPage(it)
                    initActions(it)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)
        }
    }

    private fun initEditorPage(event: Event) {
        et_name_edit_event_fragment.setText(event.name)
        et_description_edit_event_fragment.setText(event.description)
        chip_place_edit_event_fragment.text = event.place
        chip_date_start_edit_event_fragment.text = event.dateStart
        chip_date_end_edit_event_fragment.text = event.dateEnd
        dateStart = getDateOfString(event.dateStart)
        dateEnd = getDateOfString(event.dateEnd)
        latitude = event.latitude
        longitude = event.longitude
    }

    private fun initActions(event: Event) {
        b_validate_edit_event_fragment.setOnClickListener {
            val name = et_name_edit_event_fragment.text.toString()
            val place = chip_place_edit_event_fragment.text.toString()
            val description = et_description_edit_event_fragment.text.toString()
            val startDateString = getDateToString(dateStart)
            val endDateString = getDateToString(dateEnd)

            if (dateEnd != null && dateStart != null
                && name.isNotEmpty()
                && place.isNotEmpty() && place != getString(R.string.chip_adresse))
            {
                if(dateEnd!!.time <= dateStart!!.time){
                    Toast.makeText(context, getString(R.string.error_date_add_event_fragment), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.addEventFragment(
                        event.idEvent,
                        event.organizer,
                        name,
                        place,
                        description,
                        startDateString,
                        endDateString,
                        latitude,
                        longitude
                    )
                    fragmentManager?.popBackStack()
                }
            } else {
                Toast.makeText(context, getString(R.string.error_empty_field_add_event_fragment), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
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
                chip_date_start_edit_event_fragment.text = getDateToString(startDateTimePicker.time)
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
                chip_date_end_edit_event_fragment.text = getDateToString(endDateTimePicker.time)
            }
            MapsFragment.requestCodeMapFragment -> {
                data?.getStringExtra(ADDRESS_TAG)?.let {
                    chip_place_edit_event_fragment.text = it
                }
                data?.getDoubleExtra(LAT_TAG, 0.0)?.let {
                    latitude = it
                }
                data?.getDoubleExtra(LONG_TAG, 0.0)?.let {
                    longitude = it
                }
            }
        }
    }

    private fun getDateToString(date: Date?): String {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        return if (date != null) df.format(date) else "Erreur test"
    }

    private fun getDateOfString(date: String): Date {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm")
        return dateFormat.parse(date)
    }


}
