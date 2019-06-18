package com.example.event_app.ui.fragment


import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
    private var photoURL: String? = null
    private var adapter: CommentsAdapter? = null
    private var photo: Photo? = null
    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    companion object {
        fun newInstance(idPhoto: String, idEvent: String, idOrganizer: String): DetailPhotoFragment {
                val bundle = bundleOf(
                    "idPhoto" to idPhoto,
                    "idEvent" to idEvent,
                    "idOrganizer" to idOrganizer
                )
                val fragment = DetailPhotoFragment()
                fragment.arguments = bundle
                return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventId = arguments?.getString("idEvent")

        photoId = arguments?.getString("idPhoto")

        idOrganizer = arguments?.getString("idOrganizer")

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
                //val action = DetailPhotoFragmentDirections.actionDetailPhotoFragmentToPhotoFullscreenFragment(it)
                //NavHostFragment.findNavController(this).navigate(action)
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

    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_detail_photo))
        photoId?.let {photoId ->
            viewModel.fetchComments(photoId)
            eventId?.let {eventId ->
                viewModel.getPhotoDetail(eventId, photoId)
            }
        }
    }

}

