package com.example.event_app.ui.fragment


import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.adapter.CommentsAdapter
import com.example.event_app.model.CommentChoice
import com.example.event_app.model.Photo
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
import com.example.event_app.utils.hideKeyboard
import com.example.event_app.utils.toast
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.reactivex.rxkotlin.addTo
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_detail_photo.*
import org.kodein.di.generic.instance
import timber.log.Timber


class DetailPhotoFragment : BaseFragment() {

    private var eventId: String? = null
    private var photoId: String? = null
    private var idOrganizer: String? = null
    private var photoAuthorId: String? = null
    private var photoURL: String? = null
    private var adapter: CommentsAdapter? = null
    private var photo: Photo? = null
    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    private var isValidatePhoto: Boolean? = null

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
        setHasOptionsMenu(true)
        initAdapter(view)
        setActions()

        viewModel.commentaires.subscribe(
            { commentList ->
                adapter?.submitList(commentList)
            },
            { error ->
                Timber.e(error)
            }
        ).addTo(viewDisposable)

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
                context?.toast(it, Toast.LENGTH_SHORT)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.userLike.subscribe(
            { liked ->
                if (liked) iv_like.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_orange))
                else iv_like.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_grey))
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.photo.subscribe(
            { photo ->
                this.photo = photo
                photoURL = photo.url
                photoAuthorId = photo.auteurId

                tv_like.text = photo.like.toString()

                tv_auteur.text = photo.authorName
                setMenu()

                viewModel.getAuthorPicture(photo.auteurId)

                GlideApp
                    .with(context!!)
                    .load(viewModel.getStorageRef(photo.url))
                    .centerInside()
                    .into(iv_photo)
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.authorPicture.subscribe(
            {
                GlideApp
                    .with(context!!)
                    .load(viewModel.getStorageRef(it))
                    .circleCrop()
                    .into(iv_icon_author_detail_photo_fragment)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail_photo_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        UserRepository.currentUser.value?.id?.let {userId ->
            if (userId == idOrganizer || userId == photoAuthorId) {
                menu.findItem(R.id.action_delete_photo).isVisible = true
                if(userId != photoAuthorId){
                    menu.findItem(R.id.action_report_photo).isVisible = true
                }
            }
            photo?.let {
                if (userId == idOrganizer && it.isReported == 1) {
                    menu.findItem(R.id.action_validate_photo).isVisible = true
                }
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_report_photo -> {
                reportImage()
                true
            }
            R.id.action_delete_photo -> {
                deleteAction()
                true
            }
            R.id.action_download_photo -> {
                permissionManager.executeFunctionWithPermissionNeeded(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    { downloadImage() }
                )
                true
            }
            R.id.action_validate_photo -> {
                authorizeImage()
                true
            }
            else -> false
        }
    }

    private fun setActions() {
        btn_validate_comment.setOnClickListener {
            photoId?.let { photoId ->
                if (et_comments.text.isNotEmpty()) {
                    val comment = et_comments.text.toString()
                    viewModel.addComment(comment, photoId)
                        .subscribe(
                            {
                                et_comments.text.clear()
                                et_comments.hideKeyboard()
                            },
                            { error ->
                                Timber.e(error)
                            }
                        ).addTo(viewDisposable)
                } else {
                    context?.toast(R.string.detail_photo_fragment_toast_commentaire_error, Toast.LENGTH_SHORT)
                }
            }
        }

        iv_photo.setOnClickListener {
            photoURL?.let {
                val action = DetailPhotoFragmentDirections.actionDetailPhotoFragmentToPhotoFullscreenFragment(it)
                NavHostFragment.findNavController(this).navigate(action)
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
                            viewModel.reportComment(comment)
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
            rv_comments.itemAnimator = SlideInUpAnimator()
            rv_comments.adapter = adapter
            rv_comments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun setMenu() {
        activity?.invalidateOptionsMenu()
    }

    private fun authorizeImage() {
        eventId?.let { eventId ->
            photo?.let { photo ->
                if (photo.isReported == 1) {
                    viewModel.reportOrValidateImage(
                        eventId,
                        photo,
                        -1
                    )
                    isValidatePhoto = false
                    activity?.invalidateOptionsMenu()
                }
            }
        }
    }

    private fun reportImage() {
        eventId?.let { eventId ->
            photo?.let { photo ->
                if (photo.isReported == 0) {
                    viewModel.reportOrValidateImage(
                        eventId, photo, 1
                    )
                } else {
                    context?.toast(R.string.picture_already_reported, Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun downloadImage() {
        photoURL?.let {
            viewModel.downloadImageOnPhone(it, eventId!!, photoId!!)
        }
    }

    private fun deleteImage() {
        eventId?.let { eventId ->
            viewModel.photo.value?.let { photo ->
                photoId?.let { id ->
                    photoURL?.let {photoURL ->
                        viewModel.deleteImageOrga(
                            eventId, id, photoURL, photo.isReported
                        )
                    }
                }
            }
        }
    }

    private fun deleteAction() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(getString(R.string.delete_photo_alert_title_detail_photo_fragment))
            .setMessage(getString(R.string.delete_photo_alert_message_detail_photo_fragment))
            .setNegativeButton(getString(R.string.tv_delete_event_cancel_detail_event_fragment)) { _, _ -> }
            .setPositiveButton(getString(R.string.tv_delete_event_valider_detail_event_fragment)) { _, _ ->
                deleteImage()
            }.show()

    }

    override fun onStart() {
        super.onStart()
        setTitleToolbar(getString(R.string.title_detail_photo))
        photoId?.let {photoId ->
            viewModel.fetchComments(photoId)
            eventId?.let {eventId ->
                viewModel.getPhotoDetail(eventId, photoId)
            }
        }
    }

}

