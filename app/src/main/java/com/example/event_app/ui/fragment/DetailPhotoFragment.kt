package com.example.event_app.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.github.kobakei.materialfabspeeddial.FabSpeedDialMenu
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_photo.*
import org.kodein.di.generic.instance
import timber.log.Timber

class DetailPhotoFragment : BaseFragment() {

    private var eventId: String? = null
    private var photoId: String? = null
    private var idOrganizer: String? = null
    private var photoAuthorId: String? = null
    private var photoURL: String? = null
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    companion object {
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
        idOrganizer = arguments?.let {
            DetailPhotoFragmentArgs.fromBundle(it).idOrganizer
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.photo.subscribe(
            {photo ->
                this.photo.onNext(photo)
                photo.url?.let {url ->
                    GlideApp
                        .with(context!!)
                        .load(viewModel.getStorageRef(url))
                        .fitCenter()
                        .into(iv_photo)
                }
                photo.auteurId?.let {
                    photoAuthorId = it
                }
                photo.url?.let {
                    photoURL = it
                }
                photo.like?.let {
                    tv_like.text = it.toString()
                }
                photo.authorName?.let {
                    tv_auteur.text = it
                }
                setFab()
            },
            {
                Timber.e(it)
                setFab()
            })
            .addTo(viewDisposable)

        viewModel.getPhotoDetail(eventId, photoId)
    }

    private fun setFab() {
        val userId = UserRepository.currentUser.value?.id
        val authorId = photoAuthorId
        val menu = FabSpeedDialMenu(context!!)

        if (userId != null && (userId == authorId || userId == idOrganizer)) {
            menu.add(0,0,0,getString(R.string.delete_picture_fab_title)).setIcon(R.drawable.ic_delete)
        }
        menu.add(0,1,1,getString(R.string.save_image_fab_title)).setIcon(R.drawable.ic_file_download)
        menu.add(0,2,2,"signaler la photo").setIcon(R.drawable.ic_report_problem)
        fab_detail_photo.setMenu(menu)

        fab_detail_photo.addOnMenuItemClickListener { miniFab, label, itemId ->
            when (itemId) {
                0 -> {
                    //delete picture
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
                1 -> {
                    //save image
                    photoURL?.let {
                        viewModel.downloadImageOnPhone(it)
                            .subscribe(
                                {byteArray ->
                                    if (viewModel.saveImage(byteArray, eventId!!, photoId!!).isNotEmpty()) {
                                        Toast.makeText(context, getString(R.string.picture_downloaded_toast), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, getString(R.string.error_on_download_toast), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                {error ->
                                    Timber.e(error)
                                    Toast.makeText(context, getString(R.string.error_on_download_toast), Toast.LENGTH_SHORT).show()
                                }
                            )

                    }

                }
                2 -> {
                    //report image
                    photo.value?.let {
                        if (it.isReported == 0) {
                            viewModel.reportPhoto(eventId!!,it)
                                .subscribe(
                                    {
                                        Timber.d("photo reported ")
                                        Toast.makeText(context, getString(R.string.picture_reported_to_owner), Toast.LENGTH_SHORT).show()
                                    },
                                    {
                                        Timber.e(it)
                                        Toast.makeText(context, "Un problème a eut lieu lors du signalement. Merci de réessayer.", Toast.LENGTH_SHORT).show()
                                    }
                                ).addTo(viewDisposable)
                            viewModel.getEvents()
                                .subscribe(
                                    {
                                        it.forEach {
                                            if (it.idEvent == eventId) {
                                                val updateEvent = it
                                                updateEvent.reportedPhotoCount = it.reportedPhotoCount + 1
                                                viewModel.updateEventReportedPhotoCount(eventId!!, updateEvent)
                                                    .subscribe(
                                                        {
                                                            Timber.e("event updated")
                                                        },
                                                        {
                                                            Timber.e("error for update event reported photo = $it")
                                                        }
                                                    )
                                            }
                                        }

                                    },
                                    {
                                        Timber.e("error getting event for update reported photo = $it")
                                    }
                                )
                        } else {
                            Toast.makeText(context, "photo déjà siganlée",Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }


    private fun deletePicture(eventId: String, id: String, url: String) {
        viewModel.deleteImageOrga(eventId, id).addOnCompleteListener {
            if (it.isSuccessful) {
                viewModel.deleteRefFromFirestore(url)
                    .subscribe(
                        {
                            Toast.makeText(context!!, getString(R.string.picture_deleted), Toast.LENGTH_SHORT)
                                .show()
                            activity?.onBackPressed()
                        },
                        {error ->
                            Timber.e(error)
                            Toast.makeText(
                                context!!, String.format(getString(R.string.unable_to_delete_picture, error)), Toast.LENGTH_SHORT
                            ).show()
                        }
                    ).addTo(viewDisposable)
            } else {
                Toast.makeText(context!!, String.format(getString(R.string.unable_to_delete_picture, "")), Toast.LENGTH_SHORT)
                    .show()
                Timber.e("an error occurred : ${it.exception}")
            }
        }
    }

}
