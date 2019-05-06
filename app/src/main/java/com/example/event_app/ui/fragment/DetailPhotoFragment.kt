package com.example.event_app.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_photo.*
import org.kodein.di.generic.instance
import timber.log.Timber

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailPhotoFragment : BaseFragment() {

    private var eventId: String? = null
    private var photoId: String? = null
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
            DetailPhotoFragmentArgs.fromBundle(it).photoURL
        }!!
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PhotoDetail", "event : ${eventId}")

        setFab()

        viewModel.photo.subscribe(
            {photo ->
                Log.d("PhotoDetail", photo.toString())
                photo.url?.let {url ->
                    val storageReference = EventRepository.ref.child(url)
                    GlideApp.with(context!!).load(storageReference).override(500,500).centerInside().placeholder(R.drawable.pic1).into(iv_photo)
                }
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.getPhotoDetail(eventId, photoId)

    }

    private fun setFab() {
        fab_detail_photo.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean =
                this@DetailPhotoFragment.onOptionsItemSelected(menuItem)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_delete_picture -> {
                eventId?.let { eventId ->
                    viewModel.photo.value?.let { photo ->
                        photo.id?.let { id ->
                            photo.url?.let { url ->
                                deletePicture(eventId, id, url)
                            }
                        }
                    }
                }

            }
            R.id.action_save_picture -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deletePicture(eventId: String, id: String, url: String) {
        viewModel.deleteImageOrga(eventId, id).addOnCompleteListener {
            if (it.isSuccessful) {
                viewModel.deleteRefFromFirestore(url)
                    .subscribe(
                        {
                            Toast.makeText(context!!, "Image supprimée", Toast.LENGTH_SHORT)
                                .show()
                            activity?.onBackPressed()
                        },
                        {error ->
                            Timber.e(error)
                            Toast.makeText(
                                context!!,
                                "Impossible de supprimer l'image $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ).addTo(viewDisposable)
            } else {
                Toast.makeText(context!!, "Impossible de supprimer l'image", Toast.LENGTH_SHORT)
                    .show()
                Timber.e("an error occurred : ${it.exception}")
            }
        }
    }

}
