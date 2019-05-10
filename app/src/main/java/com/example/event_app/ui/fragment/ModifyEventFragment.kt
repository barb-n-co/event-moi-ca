package com.example.event_app.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.model.Event
import com.example.event_app.viewmodel.DetailEventViewModel
import com.example.event_app.viewmodel.LoginViewModel
import com.example.event_app.viewmodel.ModifyEventViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_modify_event.*
import org.kodein.di.generic.instance
import java.lang.ref.WeakReference

class ModifyEventFragment : BaseFragment(){
    private val viewModel: ModifyEventViewModel by instance(arg = this)

    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private var eventId: String? = null
    private lateinit var adapter : CustomAdapter
    private lateinit var weakContext: WeakReference<Context>

    companion object {
        const val TAG = "MODIFYFRAGMENT"
        fun newInstance(): ModifyEventFragment = ModifyEventFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_modify_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        setVisibilityNavBar(false)
        setTitleToolbar("Modifier un evenement")
        weakContext = WeakReference<Context>(context)
        adapter = CustomAdapter(weakContext.get()!!)

        eventId = arguments?.let {
            ModifyEventFragmentArgs.fromBundle(it).eventId

        }

        eventId?.let { notNullId ->
            viewModel.getEventInfo(notNullId)
        }

        Log.d("DetailEvent", "event id :" + eventId)
        viewModel.event.subscribe(

            {
                et_organizer_modify_event_fragment.setText(it.nameOrganizer)
                et_name_modify_event_fragment.setText(it.nameEvent)
                et_description_add_event_fragment.setText(it.description)
                chip_place_modify_event_fragment.setText(it.place)
                chip_date_start_modify_event_fragment.setText(it.dateStart)
                chip_date_end_modify_event_fragment.setText(it.dateEnd)


            }

        ).addTo(viewDisposable)







    }
}