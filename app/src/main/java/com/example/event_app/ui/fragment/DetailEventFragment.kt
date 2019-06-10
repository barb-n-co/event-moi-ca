package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.GenericTransitionOptions
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.manager.PermissionManager.Companion.CAPTURE_PHOTO
import com.example.event_app.manager.PermissionManager.Companion.IMAGE_PICK_CODE
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_ALL
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_IMPORT
import com.example.event_app.model.Event
import com.example.event_app.model.User
import com.example.event_app.model.spannable
import com.example.event_app.model.url
import com.example.event_app.repository.UserRepository
import com.example.event_app.ui.activity.GenerationQrCodeActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.DetailEventViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.kobakei.materialfabspeeddial.OnMenuItemClick
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


class DetailEventFragment : BaseFragment(), DetailEventInterface {

    private lateinit var weakContext: WeakReference<Context>
    private lateinit var adapter: CustomAdapter
    private lateinit var participants: List<User>
    private val viewModel: DetailEventViewModel by instance(arg = this)
    private var eventId: String? = null
    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private var popupWindow: PopupWindow? = null
    var idOrganizer = ""

    companion object {
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        setVisibilityNavBar(false)

        weakContext = WeakReference<Context>(context)

        eventId = arguments?.let {
            DetailEventFragmentArgs.fromBundle(it).eventId
        }

        adapter = CustomAdapter()
        val mGrid = GridLayoutManager(context, 3)
        rv_listImage.layoutManager = mGrid
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false)

        eventId?.let {

            //viewModel.getEventInfo(it)
            //viewModel.getParticipant(it)

            adapter.photosClickPublisher.subscribe(
                { photoId ->
                    val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(
                        it,
                        photoId,
                        idOrganizer
                    )
                    NavHostFragment.findNavController(this).navigate(action)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)

            viewModel.initPhotoEventListener(it).subscribe(
                { photoList ->
                    adapter.submitList(photoList)
                    adapter.notifyDataSetChanged()
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)
        }

        viewModel.event.subscribe(
            {
                tv_event_name_detail_fragment.text = it.nameEvent
                tv_description_detail_fragment.text = it.description
                tv_organizer_detail_fragment.text = it.nameOrganizer

                tv_address_detail_fragment.text = spannable { url("", it.place) }
                tv_start_event_detail_fragment.text = it.dateStart
                tv_finish_event_detail_fragment.text = it.dateEnd
                idOrganizer = it.idOrganizer

                GlideApp
                    .with(context!!)
                    .load(it.organizerPhotoReference)
                    .transition(GenericTransitionOptions.with(R.anim.fade_in))
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(iv_organizer_detail_fragment)

                root_layout.visibility = VISIBLE

                if (it.organizer != 1) {
                    switch_activate_detail_event_fragment.visibility = GONE
                    if (it.accepted != 1) {
                        fabmenu_detail_event.visibility = GONE
                        iv_alert_not_accepted_event.visibility = VISIBLE
                        not_already_accepted_alert.visibility = VISIBLE

                    } else {
                        fabmenu_detail_event.visibility = VISIBLE
                        displayQuitEventMenu(true)
                        setFab(it.activate != 0)
                        rv_listImage.visibility = VISIBLE
                    }

                } else {
                    setFab(it.activate != 0)
                    displayDeleteEventMenu(true)
                    displayEditEventMenu(true)
                    displayQrCodeMenu(true)
                    if (it.activate == 0) {
                        switch_activate_detail_event_fragment.isChecked = false
                        switch_activate_detail_event_fragment.text =
                            getString(R.string.switch_desactivate_detail_event_fragment)
                    } else {
                        switch_activate_detail_event_fragment.isChecked = true
                        switch_activate_detail_event_fragment.text =
                            getString(R.string.switch_activate_detail_event_fragment)
                    }
                    switch_activate_detail_event_fragment.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            switch_activate_detail_event_fragment.text =
                                getString(R.string.switch_activate_detail_event_fragment)
                            viewModel.changeActivationEvent(true)
                            setFab(true)
                        } else {
                            switch_activate_detail_event_fragment.text =
                                getString(R.string.switch_desactivate_detail_event_fragment)
                            viewModel.changeActivationEvent(false)
                            setFab(false)
                        }
                    }
                    switch_activate_detail_event_fragment.visibility = VISIBLE
                    fabmenu_detail_event.visibility = VISIBLE
                    rv_listImage.visibility = VISIBLE
                }
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        tv_listParticipant.setOnClickListener { openPopUp() }

        viewModel.participants.subscribe({
            tv_listParticipant.text = resources.getQuantityString(R.plurals.participants, it.size, it.size)
            participants = it
        }, {
            Timber.e(it)
        }).addTo(viewDisposable)


        tv_address_detail_fragment.setOnClickListener {
            val query = tv_address_detail_fragment.text.toString()
            val address = getString(R.string.map_query, query)
            if (query.isNotEmpty()) {
                startActivity(viewModel.createMapIntent(address))
            }

        }

        tv_listParticipant.setOnClickListener { openPopUp() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        when (requestCode) {
            CAPTURE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    val capturedBitmap = viewModel.getBitmapWithPath()
                    eventId?.let { eventId ->
                        viewModel.putImageWithBitmap(capturedBitmap, eventId, false)
                    }
                }
            }

            IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    returnIntent?.extras
                    val galleryUri = returnIntent?.data!!
                    val galeryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    eventId?.let { eventId ->
                        viewModel.putImageWithBitmap(galeryBitmap, eventId, true)
                    }
                }
            }
        }
    }

    private fun actionDeleteEvent(eventId: String) {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(getString(R.string.tv_delete_event_detail_event_fragment))
            .setMessage(getString(R.string.tv_delete_event_message_detail_event_fragment))
            .setNegativeButton(getString(R.string.tv_delete_event_cancel_detail_event_fragment)) { _, _ -> }
            .setPositiveButton(getString(R.string.tv_delete_event_valider_detail_event_fragment)) { _, _ ->
                viewModel.deleteEvent(eventId).addOnCompleteListener {
                    fragmentManager?.popBackStack()
                    Toast.makeText(
                        activity!!,
                        getString(R.string.tv_delete_event_toast_detail_event_fragment),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.show()
    }

    private fun actionExitEvent(eventId: String) {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(getString(R.string.tv_dialogTitle_detail_event_fragment))
            .setMessage(getString(R.string.tv_dialogMessage_detail_event_fragment))
            .setNegativeButton(getString(R.string.tv_dialogCancel_detail_event_fragment)) { _, _ -> }
            .setPositiveButton(getString(R.string.tv_dialogValidate_detail_event_fragment)) { _, _ ->
                    viewModel.exitEvent(eventId)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModel.exitMyEvent(eventId)?.addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    fragmentManager?.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.error_occured_leaving_event),
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    Timber.e(task2.exception?.localizedMessage)
                                }
                            }

                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.error_occured_leaving_event),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Timber.e(task.exception?.localizedMessage)
                        }

                    }
                    Toast.makeText(
                        activity!!,
                        getString(R.string.exit_event_toast_detail_event_fragment),
                        Toast.LENGTH_SHORT
                    ).show()
            }.show()
    }

    private fun openPopUp() {
        eventId?.let {
            viewModel.getParticipant(it)
        }
        val popup = ListParticipantDialogFragment(
            deleteSelectedListener = {
                removeUser(it)
            },
            idOrganizer = idOrganizer,
            isNotAnOrganizer = idOrganizer == UserRepository.currentUser.value?.id,
            participants = participants
        )
        popup.show(requireFragmentManager(), "listParticipant")
    }

    private fun removeUser(userId: String) {
        viewModel.removeParticipant(eventId!!, userId)
        Toast.makeText(context, getString(R.string.ToastRemoveParticipant), Toast.LENGTH_LONG).show()
    }

    private fun setFab(state: Boolean) {

        if (state) {
            fabmenu_detail_event.getMiniFab(0).show()
            fabmenu_detail_event.getMiniFabTextView(0).visibility = VISIBLE
            fabmenu_detail_event.getMiniFab(1).show()
            fabmenu_detail_event.getMiniFabTextView(1).visibility = VISIBLE
        } else {
            fabmenu_detail_event.getMiniFab(0).hide()
            fabmenu_detail_event.getMiniFabTextView(0).visibility = GONE
            fabmenu_detail_event.getMiniFab(1).hide()
            fabmenu_detail_event.getMiniFabTextView(1).visibility = GONE
        }

        fabmenu_detail_event.addOnMenuItemClickListener(object : OnMenuItemClick {
            override fun invoke(miniFab: FloatingActionButton, label: TextView?, itemId: Int) {
                when (itemId) {
                    R.id.action_camera -> {
                        if (permissionManager.checkPermissions(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        ) {
                            takePhotoByCamera()
                        } else {
                            requestPermissions()
                        }
                    }
                    R.id.action_gallery -> {
                        if (permissionManager.checkPermissions(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        ) {
                            takePhotoByGallery()
                        } else {
                            requestPermissions()
                        }
                    }
                    R.id.action_download_every_photos -> {
                        eventId?.let { id ->
                            val eventName = tv_event_name_detail_fragment.text.toString()
                            viewModel.getAllPictures(id, weakContext.get()!!, eventName)
                        }
                    }
                }
            }
        })


    }

    private fun takePhotoByCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                viewModel.createImageFile(context!!)
            } catch (ex: IOException) {
                Toast.makeText(
                    context,
                    getString(R.string.error_occured_downloading_photo),
                    Toast.LENGTH_SHORT
                )
                    .show()
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context!!,
                    getString(R.string.fileProvider),
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAPTURE_PHOTO)
            }
        }
    }

    private fun takePhotoByGallery() {
        viewModel.pickImageFromGallery().also { galleryIntent ->
            val chooser =
                Intent.createChooser(galleryIntent, "My Gallery")
            startActivityForResult(chooser, IMAGE_PICK_CODE)
        }
    }

    override fun loadQrCode() {
        eventId?.let {
            GenerationQrCodeActivity.start(activity as MainActivity, it)
        }
    }

    override fun editEvent() {
        eventId?.let {
            val action =
                DetailEventFragmentDirections.actionDetailEventFragmentToEditDetailEventFragment(it)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun deleteEvent() {
        eventId?.let {
            actionDeleteEvent(it)
        }
    }

    override fun quitEvent() {
        eventId?.let {
            actionExitEvent(it)
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissionManager.requestPermissions(permissions, PERMISSION_ALL, activity as MainActivity)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL && grantResults.size == 2) {
            takePhotoByCamera()
        }

        if (requestCode == PERMISSION_IMPORT && grantResults.size == 2) {
            takePhotoByGallery()
        }
    }

    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_detail_event))
        eventId?.let {
            viewModel.getEventInfo(it)
            viewModel.getParticipant(it)
        }
    }

    override fun onStop() {
        super.onStop()
        displayQrCodeMenu(false)
        displayEditEventMenu(false)
        displayDeleteEventMenu(false)
        displayQuitEventMenu(false)
    }

    override fun onDestroyView() {
        weakContext.clear()
        popupWindow?.dismiss()
        super.onDestroyView()
    }
}

interface DetailEventInterface {
    fun loadQrCode()
    fun editEvent()
    fun deleteEvent()
    fun quitEvent()
}
