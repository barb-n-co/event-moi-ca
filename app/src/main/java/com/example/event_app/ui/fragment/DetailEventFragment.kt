package com.example.event_app.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.View.*
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.GenericTransitionOptions
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.manager.CAPTURE_PHOTO
import com.example.event_app.manager.IMAGE_PICK_CODE
import com.example.event_app.model.*
import com.example.event_app.repository.UserRepository
import com.example.event_app.ui.activity.GenerationQrCodeActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.utils.GlideApp
import com.example.event_app.utils.toast
import com.example.event_app.viewmodel.DetailEventViewModel
import com.obsez.android.lib.filechooser.ChooserDialog
import com.obsez.android.lib.filechooser.tool.RootFile
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.file_chooser_item.view.*
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class DetailEventFragment : BaseFragment() {

    private lateinit var weakContext: WeakReference<Context>
    private lateinit var adapter: CustomAdapter
    private lateinit var participants: List<User>
    private val viewModel: DetailEventViewModel by instance(arg = this)
    private var eventId: String? = null
    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private var popupWindow: PopupWindow? = null
    var idOrganizer = ""

    private var isOrganizerMenu: Boolean? = null

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
        setHasOptionsMenu(true)
        setVisibilityNavBar(false)

        weakContext = WeakReference<Context>(context)

        eventId = arguments?.let {
            DetailEventFragmentArgs.fromBundle(it).eventId
        }

        adapter = CustomAdapter()
        val mGrid = GridLayoutManager(context, 3)
        rv_listImage.layoutManager = mGrid
        rv_listImage.itemAnimator = SlideInUpAnimator()
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false)

        eventId?.let {

            viewModel.getParticipant(it)

            adapter.photosClickPublisher.subscribe(
                { photoId ->
                    val action = DetailEventFragmentDirections.actionDetailEventFragmentToPhotoSliderFragment(
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

            viewModel.pictures.subscribe(
                { pictures ->
                    if(pictures.isEmpty()){
                        group_no_pictures_event_detail_fragment.visibility = VISIBLE
                    } else {
                        group_no_pictures_event_detail_fragment.visibility = INVISIBLE
                    }
                    adapter.submitList(pictures.reversed())
                    adapter.notifyDataSetChanged()
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)
        }

        viewModel.event.subscribe(
            {
                displayEvent(it)
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.participants.subscribe({
            chip_listParticipant.text = resources.getQuantityString(R.plurals.participants, it.size, it.size)
            participants = it
        }, {
            Timber.e(it)
        }).addTo(viewDisposable)

        viewModel.messageDispatcher.subscribe(
            {
                context?.toast(it, Toast.LENGTH_SHORT)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        tv_address_detail_fragment.setOnClickListener {
            val query = tv_address_detail_fragment.text.toString()
            val address = getString(R.string.map_query, query)
            if (query.isNotEmpty()) {
                viewModel.createMapIntent(address)?.let {
                    startActivity(it)
                }
            }

        }

        chip_listParticipant.setOnClickListener { openPopUp() }

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
                    val galleryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    eventId?.let { eventId ->
                        viewModel.putImageWithBitmap(galleryBitmap, eventId, true)
                    }
                }
            }
            42 -> {
                returnIntent?.data?.also {
                    Timber.tag("FOLDER").d("Uri: $it")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail_event_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (isOrganizerMenu == true) {
            menu.findItem(R.id.action_delete_event).isVisible = true
            menu.findItem(R.id.action_edit_event).isVisible = true
        } else if(isOrganizerMenu == false) {
            menu.findItem(R.id.action_quit_event).isVisible = true
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_download_all_pictures -> {
                requireDownload()
                true
            }
            R.id.action_edit_event -> {
                editEvent()
                true
            }
            R.id.action_delete_event -> {
                deleteEvent()
                true
            }
            R.id.action_quit_event -> {
                quitEvent()
                true
            }
            else -> false
        }
    }

    private fun displayEvent(it: EventItem) {
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
                navigation_detail_event.visibility = GONE
                iv_alert_not_accepted_event.visibility = VISIBLE
                not_already_accepted_alert.visibility = VISIBLE

            } else {
                navigation_detail_event.visibility = VISIBLE
                isOrganizerMenu = false
                activity?.invalidateOptionsMenu()
                setNavigation(it.activate != 0)
                rv_listImage.visibility = VISIBLE
            }
            navigation_detail_event.menu.findItem(R.id.action_invitation).isVisible = false

        } else {
            setNavigation(it.activate != 0)
            isOrganizerMenu = true
            activity?.invalidateOptionsMenu()
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
                    setNavigation(true)
                } else {
                    switch_activate_detail_event_fragment.text =
                        getString(R.string.switch_desactivate_detail_event_fragment)
                    viewModel.changeActivationEvent(false)
                    setNavigation(false)
                }
            }
            switch_activate_detail_event_fragment.visibility = VISIBLE
            navigation_detail_event.visibility = VISIBLE
            rv_listImage.visibility = VISIBLE
            navigation_detail_event.menu.findItem(R.id.action_invitation).isVisible = true
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
                    context?.toast(R.string.tv_delete_event_toast_detail_event_fragment, Toast.LENGTH_SHORT)
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
                                    context?.toast(R.string.error_occured_leaving_event, Toast.LENGTH_SHORT)
                                    Timber.e(task2.exception?.localizedMessage)
                                }
                            }

                        } else {
                            context?.toast(R.string.error_occured_leaving_event, Toast.LENGTH_SHORT)
                            Timber.e(task.exception?.localizedMessage)
                        }

                    }
                context?.toast(R.string.exit_event_toast_detail_event_fragment, Toast.LENGTH_SHORT)
            }.show()
    }

    private fun openPopUp() {
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
        context?.toast(R.string.ToastRemoveParticipant, Toast.LENGTH_LONG)
    }

    private fun setNavigation(state: Boolean) {

        if (state) {
            navigation_detail_event.menu.findItem(R.id.action_camera).isVisible = true
            navigation_detail_event.menu.findItem(R.id.action_gallery).isVisible = true
        } else {
            navigation_detail_event.menu.findItem(R.id.action_camera).isVisible = false
            navigation_detail_event.menu.findItem(R.id.action_gallery).isVisible = false
        }

        navigation_detail_event.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_camera -> {
                    permissionManager.executeFunctionWithPermissionNeeded(
                        this,
                        Manifest.permission.CAMERA,
                        {takePhotoByCamera()}
                    )
                    true
                }
                R.id.action_gallery -> {
                    takePhotoByGallery()
                    true
                }
                R.id.action_invitation -> {
                    eventId?.let {
                        GenerationQrCodeActivity.start(activity as MainActivity, it)
                    }
                    true
                }
                R.id.action_list_participants -> {
                    openPopUp()
                    true
                }
                else -> false
            }
        }


    }

    private fun takePhotoByCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                viewModel.createImageFile(context!!)
            } catch (ex: IOException) {
                context?.toast(R.string.error_occured_downloading_photo, Toast.LENGTH_SHORT)
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
            val chooser = Intent.createChooser(galleryIntent, "My Gallery")
            startActivityForResult(chooser, IMAGE_PICK_CODE)
        }
    }

    private fun editEvent() {
        eventId?.let {
            val action =
                DetailEventFragmentDirections.actionDetailEventFragmentToEditDetailEventFragment(it)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    private fun deleteEvent() {
        eventId?.let {
            actionDeleteEvent(it)
        }
    }

    private fun quitEvent() {
        eventId?.let {
            actionExitEvent(it)
        }
    }

    private fun requireDownload() {
        permissionManager.executeFunctionWithPermissionNeeded(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            { downloadPictures() }
        )
    }

    fun performFileSearch() {


        val chooseFile =  Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "vnd.android.document/directory"
        chooseFile.addCategory(Intent.CATEGORY_APP_BROWSER)
        val intent = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(chooseFile, 42)

//        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
//        // browser.
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            // Filter to only show results that can be "opened", such as a
//            // file (as opposed to a list of contacts or timezones)
//            addCategory(Intent.CATEGORY_OPENABLE)
//
//            // Filter to show only images, using the image MIME data type.
//            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
//            // To search for all documents available via installed storage providers,
//            // it would be "*/*".
//            type = "vnd.android.cursor.dir/*"
//        }
//
//        startActivityForResult(intent, 42)
    }

    private fun downloadPictures() {
        //performFileSearch()
        @SuppressLint("SimpleDateFormat")
        var format = SimpleDateFormat("yyyy")
        val colorFilter = PorterDuffColorFilter(0x6033b5e5, PorterDuff.Mode.MULTIPLY)
        var folderIcon = ContextCompat.getDrawable(context!!,
            com.obsez.android.lib.filechooser.R.drawable.ic_folder)
        var fileIcon = ContextCompat.getDrawable(context!!,
            com.obsez.android.lib.filechooser.R.drawable.ic_file)
        val filter = PorterDuffColorFilter(0x600000aa,
                PorterDuff.Mode.SRC_ATOP)
        folderIcon?.mutate()?.colorFilter = filter
        fileIcon?.mutate()?.colorFilter = filter

        ChooserDialog(activity, R.style.FileChooserStyle_Dark)
            .withFilter(true, false)
            .withStartFile(Environment.getExternalStorageDirectory().toString())
            .withStringResources("Choisissez un dossier", "OK", "Annuler")
            .withOptionStringResources("Nouveau", "Supprimer", "Annuler", "OK")
            .withOptionIcons(R.drawable.ic_more_menu, R.drawable.ic_create_new_folder, R.drawable.ic_delete)
            .withAdapterSetter { adapter ->
                adapter.overrideGetView { file, isSelected, isFocused, convertView, parent, inflater ->
//                    val view = inflater.inflate(R.layout.file_chooser_item, parent, false) as ViewGroup
//                    Timber.tag("FOLDER").d("name = ${file.name} / date = ${file.lastModified()} / size = ${file.totalSpace}")
//                    view.text.text = file.name
//                    view.txt_date.text = file.lastModified().toString()
//                    view.txt_size.text = file.totalSpace.toString()
//                    view.setBackgroundColor(Color.RED)
//                    view
                    val view = inflater.inflate(R.layout.file_chooser_item, parent, false) as ViewGroup

                    val tvName = view.file_name
                    val tvPath = view.file_path
                    val tvDate = view.file_date

                    tvName.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    tvPath.setTextColor(ContextCompat.getColor(context!!, R.color.grey))
                    tvDate.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))

                    tvDate.visibility = VISIBLE
                    tvName.text = file.name
                        val icon: Drawable?
                        if (file.isDirectory) {
                            icon = folderIcon?.constantState?.newDrawable()
                            if (file.lastModified() != 0L) {
                                tvDate.text = format.format(Date(file.lastModified()))
                            } else {
                                tvDate.visibility = GONE
                            }
                        } else {
                            icon = fileIcon?.constantState?.newDrawable()
                            tvDate.text = format.format( Date(file.lastModified()))
                        }
                        if (file.isHidden) {
                            tvName.text = "HIDDEN"
                        }
                        tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)

                        if (file !is RootFile) {
                            tvPath.text = file.path
                        } else {
                            tvPath.text = ""
                        }

                        val root = view.findViewById<View>(R.id.root)
                        if (root?.background == null) {
                            root?.setBackgroundResource(R.color.colorPrimary)
                        }
                        if (!isSelected) {
                            root?.background?.clearColorFilter()
                        } else {
                            root?.background?.colorFilter = colorFilter
                        }

                    view
                }
            }
            .withChosenListener { path, pathFile ->
                Timber.e("FOLDER: $path // PATHFILE: $pathFile")
            }
            .enableOptions(true)
            .titleFollowsDir(true)
            .withIcon(R.drawable.ic_folder_title)
            .build()
            .show()

        eventId?.let { id ->
            val eventName = tv_event_name_detail_fragment.text.toString()
            viewModel.getAllPictures(id, weakContext.get()!!, eventName)
            true
        }
    }


    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_detail_event))
        eventId?.let {
            viewModel.getPicturesEvent(it)
            viewModel.getEventInfo(it)
            viewModel.getParticipant(it)
        }
    }

    override fun onDestroyView() {
        weakContext.clear()
        popupWindow?.dismiss()
        super.onDestroyView()
    }
}
