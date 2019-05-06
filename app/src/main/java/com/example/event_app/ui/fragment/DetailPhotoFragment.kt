package com.example.event_app.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
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
    private var photoAuthor: String? = null
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
                photo.url?.let {url ->
                    val storageReference = EventRepository.ref.child(url)
                    GlideApp.with(context!!).load(storageReference).override(500,500).centerInside().placeholder(R.drawable.pic1).into(iv_photo)
                }
                photo.auteur?.let {
                    photoAuthor = it
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
        val author = photoAuthor
        val menu = FabSpeedDialMenu(context!!)

        if (userId != null && author != null && (userId == author || userId == idOrganizer)) {
            menu.add(0,0,0,"Supprimer l'image").setIcon(R.drawable.ic_delete)
        }
        menu.add(0,1,1,"Sauvegarder l'image").setIcon(R.drawable.ic_file_download)
        fab_detail_photo.setMenu(menu)

        fab_detail_photo.addOnMenuItemClickListener { miniFab, label, itemId ->
            when (itemId) {
                0 -> {
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
                    Toast.makeText(context, "Sauvegarde", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context!!, "Image supprimée", Toast.LENGTH_SHORT)
                                .show()
                            activity?.onBackPressed()
                        },
                        {error ->
                            Timber.e(error)
                            Toast.makeText(
                                context!!, "Impossible de supprimer l'image $error", Toast.LENGTH_SHORT
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
