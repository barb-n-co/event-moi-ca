package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.adapter.ListParticipantsAdapter
import com.example.event_app.manager.PermissionManager.Companion.CAPTURE_PHOTO
import com.example.event_app.manager.PermissionManager.Companion.IMAGE_PICK_CODE
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_ALL
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_IMPORT
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository
import com.example.event_app.ui.activity.GenerationQrCodeActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.DetailEventViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.kobakei.materialfabspeeddial.OnMenuItemClick
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import kotlinx.android.synthetic.main.list_participants_popup.*
import kotlinx.android.synthetic.main.list_participants_popup.view.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.lang.ref.WeakReference


class DetailEventFragment : BaseFragment() {

    private val viewModel : DetailEventViewModel by instance(arg = this)
    private lateinit var adapter : CustomAdapter
    private lateinit var listParticipantsAdapter: ListParticipantsAdapter

    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    lateinit var participants: List<User>
    var popupWindow: PopupWindow? = null

    private var eventId: String? = null
    private lateinit var weakContext: WeakReference<Context>
    var idOrganizer = ""

    var imageIdList = ArrayList<Photo>()

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
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
        adapter = CustomAdapter(weakContext.get()!!)

        eventId = arguments?.let {
            DetailEventFragmentArgs.fromBundle(it).eventId
        }

        requestPermissions()

        setFab()

        Log.d("DetailEvent", "event id :" + eventId)
        viewModel.event.subscribe(
            {
                tv_eventName.text = it.nameEvent
                tv_eventDescription.text = it.description
                tv_eventOrga.text = it.nameOrganizer
                tv_eventDateStart.text = it.dateStart
                tv_eventDateEnd.text = it.dateEnd
                setTitleToolbar(it.nameEvent)
                idOrganizer = it.idOrganizer

                if (it.organizer != 1) {
                    iv_generate_qrCode.visibility = View.INVISIBLE
                } else {
                    iv_generate_qrCode.visibility = View.VISIBLE
                    iv_generate_qrCode.setOnClickListener {
                        eventId?.let {
                            GenerationQrCodeActivity.start(activity as MainActivity, it)
                        }
                    }
                }
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)
        tv_listParticipant.setOnClickListener { openPopUp() }

        viewModel.participants.subscribe({
            tv_listParticipant.text = "${it.size} participants"
            tv_listParticipant.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            participants = it
        }, {
            Timber.e(it)
        }).addTo(viewDisposable)


        eventId?.let { notNullId ->
            viewModel.getEventInfo(notNullId)
            viewModel.getParticipant(notNullId)

            //val mGrid = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            val mGrid = GridLayoutManager(context, 3)
            rv_listImage.layoutManager = mGrid
            rv_listImage.adapter = adapter
            ViewCompat.setNestedScrollingEnabled(rv_listImage, false)
            adapter.photosClickPublisher.subscribe(
                { photoId ->
                    val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(
                        notNullId,
                        photoId,
                        idOrganizer
                    )
                    NavHostFragment.findNavController(this).navigate(action)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)

            adapter.submitList(imageIdList)

            viewModel.initPhotoEventListener(notNullId).subscribe(
                { photoList ->
                    adapter.submitList(photoList)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)
        }
    }

    private fun openPopUp() {
        // Inflate a custom view using layout inflater
        val popup = inflate(this.context,R.layout.list_participants_popup,null)


        popupWindow = PopupWindow(popup, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow?.elevation = 10.0F
        TransitionManager.beginDelayedTransition(root_layout)
        popupWindow?.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
        val mLinear = LinearLayoutManager(context)
        listParticipantsAdapter = ListParticipantsAdapter(context!!, idOrganizer)
        popup.rv_listParticipants.layoutManager = mLinear
        popup.rv_listParticipants.adapter = listParticipantsAdapter
        viewModel.getParticipant(eventId!!)
        listParticipantsAdapter.submitList(participants)
        listParticipantsAdapter.userClickPublisher.subscribe(
            {
                viewModel.removeParticipant(eventId!!,it)
                listParticipantsAdapter.notifyDataSetChanged()
            },{
                Timber.e(it)
            }
        ).addTo(viewDisposable)
        val buttonPopup = popup.findViewById<Button>(R.id.btn_popup)
        buttonPopup.setOnClickListener {popupWindow?.dismiss()}
    }

    private fun setFab() {

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
                            startActivityForResult(viewModel.pickImageFromGallery(), IMAGE_PICK_CODE)
                        } else {
                            requestPermissions()
                        }
                    }
                    R.id.action_download_every_photos -> {
                        eventId?.let { id ->
                            viewModel.getAllPictures(id, weakContext.get()!!)
                        }
                    }
                }
            }
        })
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionManager.requestPermissions(permissions, PERMISSION_ALL, activity as MainActivity)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL && grantResults.size == 2) {
            takePhotoByCamera()
        }

        if (requestCode == PERMISSION_IMPORT && grantResults.size == 2) {
            startActivityForResult(viewModel.pickImageFromGallery(), IMAGE_PICK_CODE)
        }
    }

    private fun takePhotoByCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAPTURE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAPTURE_PHOTO -> {
                    val capturedBitmap = returnIntent?.extras!!.get("data") as Bitmap
                    eventId?.let { eventId ->
                        viewModel.putImageWithBitmap(capturedBitmap, eventId)
                    }
                }

                IMAGE_PICK_CODE -> {
                    returnIntent?.extras
                    val galleryUri = returnIntent?.data!!
                    val galeryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    eventId?.let { eventId ->
                        viewModel.putImageWithBitmap(galeryBitmap, eventId)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        weakContext.clear()
        popupWindow?.dismiss()
        super.onDestroyView()

    }
}
