package com.example.event_app.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import kotlinx.android.synthetic.main.fragment_detail_photo.*

import org.kodein.di.generic.instance
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailPhotoFragment : BaseFragment() {

    private var eventId: String? = null
    private var photoId: Int = -1
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    companion object {
        const val TAG = "DETAIL_PHOTO_FRAGMENT"
        fun newInstance(): DetailPhotoFragment = DetailPhotoFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventId = arguments?.let{
            DetailPhotoFragmentArgs.fromBundle(it).eventId
        }
        photoId = arguments?.let{
            DetailPhotoFragmentArgs.fromBundle(it).photoId
        }!!
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.photo.subscribe(
            {
                Log.d("PhotoDetail", it.toString())
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.getPhotoDetail(eventId, photoId)

    }


}
