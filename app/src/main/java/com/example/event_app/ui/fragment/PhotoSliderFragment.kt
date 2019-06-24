package com.example.event_app.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.event_app.R
import com.example.event_app.adapter.FolderChooserDialog
import com.example.event_app.adapter.PicturesViewPagerAdapter
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.AnimationZoomOutPageTransformer
import com.example.event_app.utils.toast
import com.example.event_app.viewmodel.DetailPhotoViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_photo_slider.*
import org.kodein.di.generic.instance
import timber.log.Timber

class PhotoSliderFragment : BaseFragment() {

    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    private var eventId: String? = null
    private var photoId: String? = null
    private var idOrganizer: String? = null

    private var photoAuthorId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        eventId = arguments?.let {
            PhotoSliderFragmentArgs.fromBundle(it).eventId
        }
        photoId = arguments?.let {
            PhotoSliderFragmentArgs.fromBundle(it).photoId
        }
        idOrganizer = arguments?.let {
            PhotoSliderFragmentArgs.fromBundle(it).idOrganizer
        }

        return inflater.inflate(R.layout.fragment_photo_slider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.pictures.subscribe(
            {
                eventId?.let { eventId ->
                    idOrganizer?.let { idOrganizer ->
                        val adapter = PicturesViewPagerAdapter(
                            requireFragmentManager(),
                            it.reversed(),
                            eventId,
                            idOrganizer
                        )
                        view_pager_photo_slider_fragment.adapter = adapter
                        tl_photo_slider_fragment.setupWithViewPager(view_pager_photo_slider_fragment, true)
                        view_pager_photo_slider_fragment.currentItem = it.reversed().indexOf(photoId)
                        view_pager_photo_slider_fragment.setPageTransformer(true, AnimationZoomOutPageTransformer())
                        adapter.idItemPosition.subscribe(
                            {
                                viewModel.getPhotoDetail(eventId, it)
                            },
                            {
                                Timber.e(it)
                            }
                        ).addTo(viewDisposable)
                    }
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.photo.subscribe(
            {
                photoAuthorId = it.auteurId
                activity?.invalidateOptionsMenu()
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.messageDispatcher.subscribe(
            {
                context!!.toast(it, Toast.LENGTH_SHORT)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail_photo_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        UserRepository.currentUser.value?.id?.let { userId ->
            if (userId == idOrganizer || userId == photoAuthorId) {
                menu.findItem(R.id.action_delete_photo).isVisible = true
                if (userId == photoAuthorId) {
                    menu.findItem(R.id.action_report_photo).isVisible = false
                }
            }
            viewModel.photo.value?.let {
                if (userId == idOrganizer && it.isReported == 1) {
                    menu.findItem(R.id.action_validate_photo).isVisible = true
                }
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    private fun authorizeImage() {
        eventId?.let { eventId ->
            viewModel.photo.value?.let { photo ->
                if (photo.isReported == 1) {
                    viewModel.reportOrValidateImage(
                        eventId,
                        photo,
                        -1
                    )
                    activity?.invalidateOptionsMenu()
                }
            }
        }
    }

    private fun reportImage() {
        eventId?.let { eventId ->
            viewModel.photo.value?.let { photo ->
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

    private fun chooseFolder() {
        val folderChooserDialog = FolderChooserDialog(context!!)
        folderChooserDialog
            .getDialog()
            .withChosenListener { chosenFolder, pathFile ->

                viewModel.photo.value?.url?.let {
                    viewModel.downloadImageOnPhone(it, null, chosenFolder, photoId!!)
                }

            }
            .build()
            .show()
    }

    private fun downloadImage() {

        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle("Où enregistrer ?")
            .setMessage("Voulez-vous enregistrer dans le répertoire par défaut ou choisir un emplacement ?")
            .setNegativeButton("Défault") { _, _ ->

                viewModel.photo.value?.url?.let {
                    viewModel.downloadImageOnPhone(it, eventId!!, null, photoId!!)
                }
            }
            .setPositiveButton("Choisir") { _, _ ->
                chooseFolder()
            }
            .setNeutralButton("Annuler") { _, _ -> }
            .show()
    }

    private fun deleteImage() {
        eventId?.let { eventId ->
            viewModel.photo.value?.let { photo ->
                photoId?.let { id ->
                    viewModel.photo.value?.url?.let { photoURL ->
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

    override fun onResume() {
        super.onResume()

        eventId?.let {
            viewModel.getPicturesEvent(it)
            photoId?.let { photoId ->
                viewModel.getPhotoDetail(it, photoId)
            }
        }
    }

}
