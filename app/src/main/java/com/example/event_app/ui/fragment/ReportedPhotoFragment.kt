package com.example.event_app.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.model.Photo
import com.example.event_app.model.ReportedPhotoList
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_reported_photo.*
import timber.log.Timber
import java.lang.ref.WeakReference

class ReportedPhotoFragment : BaseFragment() {

    lateinit var reportedPhotoList: ReportedPhotoList
    lateinit var weakContext : WeakReference<Context>
    lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportedPhotoList = ReportedPhotoFragmentArgs.fromBundle(it).list
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reported_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

        report_photo_title.text = "Photos signalées"
        weakContext = WeakReference(context!!)
        adapter = CustomAdapter(weakContext.get()!!)
        rv_reported_photos.adapter = adapter
        rv_reported_photos.layoutManager = GridLayoutManager(context, 3)

        adapter.photosClickPublisher.subscribe(
            {photoId ->
                Timber.d("photo ID : $photoId")
                val action = ReportedPhotoFragmentDirections.actionReportedPhotoFragmentToDetailPhotoFragment("eventId", photoId, "Orga")
                NavHostFragment.findNavController(this).navigate(action)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
        var list = mutableListOf<Photo>()
        reportedPhotoList.listOfphotoList.forEach {
            it.list?.forEach {
                list.add(it)
            }
        }
        list = list.toSet().toMutableList()

        adapter.submitList(list)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        weakContext.clear()
    }
}
