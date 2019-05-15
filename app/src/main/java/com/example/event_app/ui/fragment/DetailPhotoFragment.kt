package com.example.event_app.ui.fragment


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.adapter.CommentsAdapter
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
import com.example.event_app.utils.hideKeyboard
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_photo.*
import org.kodein.di.generic.instance
import timber.log.Timber

class DetailPhotoFragment : BaseFragment(), DetailPhotoInterface {

    private var eventId: String? = null
    private var photoId: String? = null
    private var idOrganizer: String? = null
    private var photoAuthorId: String? = null
    private var photoURL: String? = null
    private var adapter: CommentsAdapter? = null
    private val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    companion object {
        fun newInstance(): DetailPhotoFragment = DetailPhotoFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventId = arguments?.let {
            DetailPhotoFragmentArgs.fromBundle(it).eventId
        }
        photoId = arguments?.let {
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

        viewModel.userLike.subscribe(
            {liked ->
                if(liked) {
                    iv_like.setColorFilter(Color.RED)
                } else {
                    iv_like.setColorFilter(Color.WHITE)
                }
            },
            {
                Timber.e(it)
            }
        )

        viewModel.initMessageReceiving()
        UserRepository.currentUser.value?.id?.let {
            adapter = CommentsAdapter(it, idOrganizer, commentSelectedListener = {commentId ->
                photoId?.let {
                    viewModel.deleteComment(it, commentId)
                }
            })
            rv_comments.adapter = adapter
            rv_comments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        btn_validate_comment.setOnClickListener {
            photoId?.let { photoId ->
                if (et_comments.text.isNotEmpty()) {
                    val comment = et_comments.text.toString()
                    viewModel.addComment(comment, photoId)
                        .subscribe(
                            {
                                Toast.makeText(context, getString(R.string.detail_photo_fragment_comment_added), Toast.LENGTH_SHORT).show()
                                viewModel.getPhotoDetail(eventId, photoId)
                                et_comments.text.clear()
                                et_comments.hideKeyboard()
                            },
                            { error ->
                                Timber.e(error)
                            }
                        ).addTo(viewDisposable)
                } else {
                    Toast.makeText(context, getString(R.string.detail_photo_fragment_toast_commentaire_error), Toast.LENGTH_SHORT).show()
                }
            }

        }

        viewModel.photo.subscribe(
            { photo ->
                this.photo.onNext(photo)
                photo.url?.let { url ->
                    GlideApp
                        .with(context!!)
                        .load(viewModel.getStorageRef(url))
                        .centerInside()
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
                setMenu()
            },
            {
                Timber.e(it)
                setMenu()
            })
            .addTo(viewDisposable)

        viewModel.peopleWhoLike.subscribe(
            { list ->
                var number = 0
                list.forEach { item ->
                    number++
                    if (item.userId == UserRepository.currentUser.value?.id) {
                        viewModel.isPhotoAlreadyLiked = true
                        viewModel.userLike.onNext(true)
                    }
                }
                tv_like.text = number.toString()
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        iv_like.setOnClickListener {
            photoId?.let { photoId ->
                viewModel.addLikes(photoId)
            }
        }

        viewModel.getPhotoDetail(eventId, photoId)

        viewModel.commentaires.subscribe(
            { commentList ->
                Timber.tag("comments -- ").d(commentList.toString())
                adapter?.submitList(commentList)
            },
            { error ->
                Timber.e(error)
            }
        ).addTo(viewDisposable)
    }

    private fun setMenu() {
        val userId = UserRepository.currentUser.value?.id
        val authorId = photoAuthorId

        if (userId != null && (userId == authorId || userId == idOrganizer)) {
            displayDetailPhotoMenuDeletePhoto(true)
        }
        photo.value?.let {
            if (userId != null && userId == idOrganizer && it.isReported == 1) {
                displayDetailPhotoMenuActionValidatePhoto(true)
            }
        }
    }

    private fun authorizeImage() {
        eventId?.let { eventId ->
            photo.value?.let { photo ->
                if (photo.isReported == 1) {
                    reportOrValidateImage(
                        eventId,
                        photo,
                        -1,
                        getString(R.string.picture_authorized_by_admin)
                    )
                    displayDetailPhotoMenuActionValidatePhoto(false)
                }
            }
        }
    }

    private fun reportImage() {
        eventId?.let { eventId ->
            photo.value?.let { photo ->
                if (photo.isReported == 0) {
                    reportOrValidateImage(eventId, photo, 1, getString(R.string.picture_reported_to_owner))
                } else {
                    Toast.makeText(context, getString(R.string.picture_already_reported), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadImage() {
        photoURL?.let {
            viewModel.downloadImageOnPhone(it)
                .subscribe(
                    { byteArray ->
                        if (viewModel.saveImage(byteArray, eventId!!, photoId!!).isNotEmpty()) {
                            Toast.makeText(
                                context,
                                getString(R.string.picture_downloaded_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.error_on_download_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    { error ->
                        Timber.e(error)
                        Toast.makeText(
                            context,
                            getString(R.string.error_on_download_toast),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ).addTo(viewDisposable)
        }
    }

    private fun deleteImage() {
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

    private fun reportOrValidateImage(eventId: String, photo: Photo, delta: Int, message: String) {
        val reportValue = if (delta > 0) 1 else 0

        handleReportPhoto(eventId, photo, reportValue, message)

        viewModel.getEvents()
            .subscribe(
                {
                    handleEventUpdate(it, eventId, delta)
                },
                {
                    Timber.e("error getting event for update reported photo = $it")
                }
            ).addTo(viewDisposable)
    }

    private fun handleReportPhoto(eventId: String, photo: Photo, reportValue: Int, message: String) {
        viewModel.reportPhoto(eventId, photo, reportValue)
            .subscribe(
                {
                    Timber.d("photo unreported ")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (reportValue == 1) {
                        viewModel.sendReportMessageToEventOwner(idOrganizer ?: "")
                    }
                },
                {
                    Timber.e(it)
                    Toast.makeText(
                        context,
                        getString(R.string.problem_occured_during_download),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ).addTo(viewDisposable)
    }

    private fun handleEventUpdate(list: List<Event>, eventId: String, delta: Int) {
        list.forEach {
            if (it.idEvent == eventId) {
                val updateEvent = it
                updateEvent.reportedPhotoCount = it.reportedPhotoCount + delta
                viewModel.updateEventReportedPhotoCount(eventId, updateEvent)
                    .subscribe(
                        {
                            Timber.e("event updated")
                            setMenu()
                        },
                        {
                            Timber.e("error for update event reported photo = $it")
                        }
                    )
            }
        }
    }

    private fun deletePicture(eventId: String, id: String, url: String) {
        /** delete reference */
        viewModel.deleteImageOrga(eventId, id).addOnCompleteListener {
            if (it.isSuccessful) {
                /** delete file */
                deleteFireStoreReference(url)
                /** delete likes */
                deleteLikes()
                /** delete comments */
                deleteComments()
            } else {
                Toast.makeText(
                    context!!,
                    getString(R.string.unable_to_delete_picture, ""),
                    Toast.LENGTH_SHORT
                )
                    .show()
                Timber.e("an error occurred : ${it.exception}")
            }
        }
    }

    private fun deleteComments() {
        photoId?.let { photoId ->
            viewModel.deleteComments(photoId)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("comments successfully deleted")
                    } else {
                        Timber.e("a problem occurred in deleting comments for the photo: ${task.exception}")
                    }
                }
        }
    }

    private fun deleteFireStoreReference(url: String) {
        viewModel.deleteRefFromFirestore(url)
            .subscribe(
                {
                    Toast.makeText(context!!, getString(R.string.picture_deleted), Toast.LENGTH_SHORT)
                        .show()
                    activity?.onBackPressed()
                },
                { error ->
                    Timber.e(error)
                    Toast.makeText(
                        context!!,
                        String.format(getString(R.string.unable_to_delete_picture, error)),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ).addTo(viewDisposable)
    }

    private fun deleteLikes() {
        photoId?.let { pictureId ->
            viewModel.deleteLikesForPhoto(pictureId)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("photo likes deleted")
                    } else {
                        Timber.e("a problem occurred in deleting likes for the photo: ${task.exception}")
                    }
                }
        }
    }

    override fun deleteAction() {
        deleteImage()
    }

    override fun authorizeAction() {
        authorizeImage()
    }

    override fun downloadAction() {
        downloadImage()
    }

    override fun reportAction() {
        reportImage()
    }

    override fun onStart() {
        super.onStart()
        displayDetailPhotoMenuRestricted(true)
    }

    override fun onPause() {
        super.onPause()
        displayDetailPhotoMenu(false)
    }

}

interface DetailPhotoInterface {
    fun deleteAction()
    fun authorizeAction()
    fun downloadAction()
    fun reportAction()
}
