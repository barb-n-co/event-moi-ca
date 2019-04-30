package com.example.event_app.ui.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.model.Event
import kotlinx.android.synthetic.main.fragment_detail_event.*

class DetailEventFragment : BaseFragment() {
    var loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc arcu orci, laoreet id nisl a, faucibus eleifend tellus. In lectus sapien, gravida commodo volutpat ut, gravida ut erat. Mauris maximus metus quis bibendum mattis. Maecenas vitae ultricies velit, quis pretium tellus. Aliquam ac augue accumsan arcu lobortis tincidunt. Morbi fringilla a nibh non dignissim. Integer faucibus tortor sed tellus vulputate vestibulum. Nunc ut erat non dolor congue commodo a venenatis dui. Maecenas non rutrum ipsum. Donec rhoncus ligula eget nulla feugiat porta. "

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mockEvent = Event(42, "MockEvent", loremIpsum, "Maintenant", "Jamais !!")

        tv_eventOrga.text = "Tritri"
        tv_eventName.text = mockEvent.name
        tv_eventDateStart.text = mockEvent.dateStart
        tv_eventDateEnd.text = mockEvent.dateEnd
        tv_eventDescription.text = mockEvent.description

    }
}
