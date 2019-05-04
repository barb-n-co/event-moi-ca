package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.manager.PermissionManager.Companion.CAPTURE_PHOTO
import com.example.event_app.manager.PermissionManager.Companion.IMAGE_PICK_CODE
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_ALL
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_IMPORT
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.ui.activity.GenerationQrCodeActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.DetailEventViewModel
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.kodein.di.generic.instance
import timber.log.Timber


class DetailEventFragment : BaseFragment() {

    private val viewModel : DetailEventViewModel by instance(arg = this)
    private lateinit var adapter : CustomAdapter
    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private var eventId: String? = null


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

        adapter = CustomAdapter(context!!)

        eventId = arguments?.let {
            DetailEventFragmentArgs.fromBundle(it).eventId
        }

        requestPermissions()

        setFab()

        Log.d("DetailEvent", "event id :" + eventId)
        viewModel.event.subscribe(
            {
                tv_eventName.text = it.name
                tv_eventDescription.text = it.description
                tv_eventOrga.text = it.organizer
                tv_eventDateStart.text = it.dateStart
                tv_eventDateEnd.text = it.dateEnd
                setTitleToolbar(it.name)
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        eventId?.let {notNullId ->
            viewModel.getEventInfo(notNullId)

            //val mGrid = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
            val mGrid = GridLayoutManager(context, 2)
            rv_listImage.layoutManager = mGrid
            rv_listImage.adapter = adapter
            ViewCompat.setNestedScrollingEnabled(rv_listImage, false)
            adapter.photosClickPublisher.subscribe(
                {photoId ->
                    val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(notNullId, photoId)
                    NavHostFragment.findNavController(this).navigate(action)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)

            adapter.submitList(imageIdList)

            viewModel.initPhotoEventListener(notNullId).subscribe(
                {photoList ->
                    adapter.submitList(photoList)
                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)

        }
    }

    private fun setFab() {
        fabmenu_detail_event.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean =
                this@DetailEventFragment.onOptionsItemSelected(menuItem)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_generate_qrcode -> {
                eventId?.let {
                    GenerationQrCodeActivity.start(activity as MainActivity, it)
                }
            }
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
        }
        return super.onOptionsItemSelected(item)
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
                    //viewModel.saveImage(capturedBitmap)
                    eventId?.let {eventId ->
                        viewModel.putImageWithBitmap(capturedBitmap, eventId)
                    }

                }

                IMAGE_PICK_CODE -> {
                    returnIntent?.extras
                    val galleryUri = returnIntent?.data!!
                    val galeryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    eventId?.let {eventId ->
                        viewModel.putImageWithBitmap(galeryBitmap, eventId)
                    }

                }

            }

        }

    }

}
