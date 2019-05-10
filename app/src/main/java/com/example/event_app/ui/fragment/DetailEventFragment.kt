package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.manager.PermissionManager.Companion.CAPTURE_PHOTO
import com.example.event_app.manager.PermissionManager.Companion.IMAGE_PICK_CODE
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_ALL
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_IMPORT
import com.example.event_app.model.*
import com.example.event_app.ui.activity.GenerationQrCodeActivity
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.DetailEventViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.kobakei.materialfabspeeddial.OnMenuItemClick
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.lang.ref.WeakReference


class DetailEventFragment : BaseFragment() {


    private val viewModel : DetailEventViewModel by instance(arg = this)
    private lateinit var adapter : CustomAdapter
    val event: BehaviorSubject<Event> = BehaviorSubject.create()
    private var eventId: String? = null
    private lateinit var weakContext: WeakReference<Context>
    private var idOrganizer = ""


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
                tv_eventName.text = it.name
                tv_eventDescription.text = it.description
                tv_eventOrga.text = it.organizer
                tv_eventPlace.text = spannable { url("",it.place) }
                tv_eventDateStart.text = it.dateStart
                tv_eventDateEnd.text = it.dateEnd
                setTitleToolbar(it.name)
                idOrganizer = it.idOrganizer!!
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)

        tv_eventPlace.setOnClickListener {
          val location = "http://maps.google.co.in/maps?q=" + tv_eventPlace.text.toString()
            val Adresse =  Intent(Intent.ACTION_VIEW, Uri.parse(location))
            startActivity(Adresse)
        }



        eventId?.let {notNullId ->
            viewModel.getEventInfo(notNullId)

            //val mGrid = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            val mGrid = GridLayoutManager(context, 3)
            rv_listImage.layoutManager = mGrid
            rv_listImage.adapter = adapter
            ViewCompat.setNestedScrollingEnabled(rv_listImage, false)
            adapter.photosClickPublisher.subscribe(
                {photoId ->
                    val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(notNullId, photoId, idOrganizer)
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

        fabmenu_detail_event.addOnMenuItemClickListener(object: OnMenuItemClick {
            override fun invoke(miniFab: FloatingActionButton, label: TextView?, itemId: Int) {
                when(itemId) {
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
                    R.id.action_download_every_photos -> {
                        eventId?.let {id ->
                            viewModel.getAllPictures(id, weakContext.get()!!)
                        }
                    }
                }
            }
        })
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION)
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


    override fun onDestroyView() {
        weakContext.clear()
        super.onDestroyView()

    }

}
