package com.example.event_app.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.viewmodel.DetailEventViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.json.JSONObject
import org.kodein.di.generic.instance
import timber.log.Timber




class DetailEventFragment: BaseFragment() {
    private var eventId: Int = -1
    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private val viewModel: DetailEventViewModel by instance(arg = this)

    var imageIdList = ArrayList<Photo>()

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        eventId = arguments?.let{
            DetailEventFragmentArgs.fromBundle(it).eventId
        }!!
        return inflater.inflate(R.layout.fragment_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var json = JSONObject()
        json.put("url","https://rickandmortyapi.com/api/character/avatar/1.jpeg")
        json.put("id",42)
        json.put("auteur", "pouet")
        imageIdList.add(Photo(json))

        Log.d("DetailEvent", "event id :"+ eventId)
        viewModel.event.subscribe(
            {
                tv_eventName.text = it.name
                tv_eventDescription.text = it.description
                tv_eventOrga.text = it.organiser
                tv_eventDateStart.text = it.dateStart
                tv_eventDateEnd.text = it.dateEnd

            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.getEventInfo(eventId)

        val adapter = CustomAdapter()
        //val mGrid = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        val mGrid = GridLayoutManager(context, 2)
        rv_listImage.layoutManager = mGrid
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false)
        adapter.photosClickPublisher.subscribe(
            {
                val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(eventId, it)
                NavHostFragment.findNavController(this).navigate(action)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        adapter.submitList(imageIdList)

    }




}
