package com.example.event_app.ui.fragment


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.event_app.R
import com.example.event_app.adapter.CommentsAdapter
import com.example.event_app.model.CommentChoice
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

private const val COLOR_PRIMARY = "#00e68a"

class DetailPhotoFragment : BaseFragment(), DetailPhotoInterface {

    private var eventId: String? = null
    private var photoId: String? = null
    private var idOrganizer: String? = null
    private var photoAuthorId: String? = null
    private var photoURL: String = ""
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
            DetailPhotoFragmentArgs.fromBundle(it).photoId
        }
        idOrganizer = arguments?.let {
            DetailPhotoFragmentArgs.fromBundle(it).idOrganizer
        }
        return inflater.inflate(R.layout.fragment_detail_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(view)
        setActions()

        viewModel.onBackPressedTrigger.subscribe(
            {
                if (it) {
                    activity?.onBackPressed()
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.menuListener.subscribe(
            {
                if (it) {
                    setMenu()
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.numberOfLikes.subscribe(
            {
                tv_like.text = it
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.messageDispatcher.subscribe(
            {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.userLike.subscribe(
            { liked ->
                if (liked) iv_like.setColorFilter(Color.parseColor(COLOR_PRIMARY))
                else iv_like.setColorFilter(Color.WHITE)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.photo.subscribe(
            { photo ->
                this.photo.onNext(photo)
                photoURL = photo.url
                photoAuthorId = photo.auteurId

                GlideApp
                    .with(context!!)
                    .load(viewModel.getStorageRef(photoURL))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerInside()
                    .into(iv_photo)

                tv_like.text = photo.like.toString()

                tv_auteur.text = photo.authorName
                setMenu()

                viewModel.getPhotographProfilePicture(photo.auteurId)

            },
            {
                Timber.e(it)
            }, {
                setMenu()
            })
            .addTo(viewDisposable)

        viewModel.photoTaker.subscribe(
            {
                GlideApp
                    .with(context!!)
                    .load(viewModel.getStorageRef(it))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(iv_icon_author_detail_photo_fragment)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.peopleWhoLike.subscribe(
            { list ->
                viewModel.getNumberOfLike(list)
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
                adapter?.submitList(commentList)
                if (commentList.size > 0) {
                    rv_comments.visibility = View.VISIBLE
                } else rv_comments.visibility = View.GONE

            },
            { error ->
                Timber.e(error)
            }
        ).addTo(viewDisposable)


    }

    private fun setActions() {
        btn_validate_comment.setOnClickListener {
            photoId?.let { photoId ->
                if (et_comments.text.isNotEmpty()) {
                    val comment = et_comments.text.toString()
                    viewModel.addComment(comment, photoId)
                        .subscribe(
                            {
                                viewModel.getPhotoDetail(eventId, photoId)
                                et_comments.text.clear()
                                et_comments.hideKeyboard()
                            },
                            { error ->
                                Timber.e(error)
                            }
                        ).addTo(viewDisposable)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.detail_photo_fragment_toast_commentaire_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun initAdapter(view: View) {
        UserRepository.currentUser.value?.id?.let { userId ->
            adapter = CommentsAdapter(
                viewModel,
                requireFragmentManager(),
                userId,
                idOrganizer,
                commentSelectedListener = { comment, commentChoice, likeId ->
                    when (commentChoice) {
                        CommentChoice.DELETE -> {
                            photoId?.let {
                                viewModel.deleteComment(it, comment.commentId)
                            }
                        }
                        CommentChoice.REPORT -> {
                            viewModel.reportComment(comment, getString(R.string.detail_photo_fragment_comment_reported))
                        }
                        CommentChoice.LIKE -> {
                            photoId?.let {
                                viewModel.addCommentLike(userId, comment.commentId, it)
                            }
                        }
                        CommentChoice.DISLIKE -> {
                            photoId?.let {
                                likeId?.let { id ->
                                    viewModel.removeCommentLike(id, it)
                                }
                            }
                        }
                        else -> {
                        }
                    }
                },
                editCommentListener = {
                    viewModel.editComment(it)
                    view.hideKeyboard()
                })
            rv_comments.itemAnimator = DefaultItemAnimator()
            rv_comments.adapter = adapter
            rv_comments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun setMenu() {
        val userId = UserRepository.currentUser.value?.id
        val authorId = photoAuthorId
        if (userId != null && (userId == idOrganizer)) {
            displayDetailPhotoMenuDeletePhoto(true)
        }
        if (userId != null && (userId == authorId)) {
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
                    viewModel.reportOrValidateImage(
                        eventId,
                        photo,
                        -1,
                        getString(R.string.picture_authorized_by_admin),
                        getString(R.string.problem_occured_during_download)
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
                    viewModel.reportOrValidateImage(
                        eventId, photo, 1,
                        getString(R.string.picture_reported_to_owner),
                        getString(R.string.problem_occured_during_download)
                    )
                } else {
                    Toast.makeText(context, getString(R.string.picture_already_reported), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadImage() {
        viewModel.downloadImageOnPhone(
            photoURL, eventId!!, photoId!!,
            getString(R.string.picture_downloaded_toast),
            getString(R.string.error_on_download_toast)
        )
    }

    private fun deleteImage() {
        eventId?.let { eventId ->
            viewModel.photo.value?.let { photo ->
                photoId?.let { id ->
                    viewModel.deleteImageOrga(
                        eventId, id, photoURL, photo.isReported,
                        getString(R.string.picture_deleted),
                        getString(R.string.unable_to_delete_picture)
                    )
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
