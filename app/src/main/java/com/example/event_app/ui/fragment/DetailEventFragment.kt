package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.model.Event
import kotlinx.android.synthetic.main.fragment_detail_event.*


class DetailEventFragment : BaseFragment() {
    var imageIdList = arrayOf<Int>(
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3
    )
    var loremIpsum =
        "Lorem ipvitae ultricies velit, quis pretium tellus. Aliquam ac augue accumsan arcu lobortis tincidunt. Morbi fringilla a nibh non dignissim. Integer faucibus tortor sed tellus vulputate vestibulum. Nunc ut erat non dolor congue commodo a venenatis dui. Maecenas non rutrum ipsum. Donec rhoncus ligula eget nulla feugiat porta. "

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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

        val adapter = CustomAdapter(imageIdList)
        val mGrid = GridLayoutManager(context,3)
        rv_listImage.layoutManager = mGrid
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false);
        adapter.notifyDataSetChanged()
    }

}
