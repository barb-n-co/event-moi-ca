package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.manager.PermissionManager.Companion.CAPTURE_PHOTO
import com.example.event_app.manager.PermissionManager.Companion.IMAGE_PICK_CODE
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_ALL
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_IMPORT
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.DetailEventViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.json.JSONObject
import org.kodein.di.generic.instance
import timber.log.Timber


class DetailEventFragment : BaseFragment() {

    private val viewModel : DetailEventViewModel by instance(arg = this)
    private val adapter = CustomAdapter()
    private var eventId: String = ""
    val event: BehaviorSubject<Event> = BehaviorSubject.create()


    var imageIdList = ArrayList<Photo>()

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        eventId = arguments?.let {
            DetailEventFragmentArgs.fromBundle(it).eventId
        }!!
        return inflater.inflate(R.layout.fragment_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

        requestPermissions()

        val nestedScrollView = view.findViewById(R.id.bottomSheetScrollView) as View
        val sheetBehavior = BottomSheetBehavior.from(nestedScrollView)

        btn_camera.setOnClickListener {

            if (permissionManager.checkPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                takePhotoByCamera()
            } else {
                requestPermissions()
            }
        }

        btn_gallery.setOnClickListener {
            if (permissionManager.checkPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                startActivityForResult(viewModel.pickImageFromGallery(), IMAGE_PICK_CODE)
            } else {
                requestPermissions()
            }
        }

        fab_add_photo.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        imageIdList.add(Photo("https://picsum.photos/id/176/200/300",1))
        imageIdList.add(Photo("https://picsum.photos/id/223/200/300",2))
        imageIdList.add(Photo("https://picsum.photos/id/27/300/300",3))
        imageIdList.add(Photo("https://picsum.photos/id/340/200/200",4))
        imageIdList.add(Photo("https://picsum.photos/id/267/300/200",5))

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

        viewModel.getEventInfo(eventId)

//        val layoutManager = StaggeredGridLayoutManager(
//            2, StaggeredGridLayoutManager.VERTICAL
//        )
//        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
//        layoutManager.isItemPrefetchEnabled = true
//
//        rv_listImage.itemAnimator = null
//        rv_listImage.layoutManager = layoutManager

        val mGrid = GridLayoutManager(context, 2)
        rv_listImage.layoutManager = mGrid
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false)
        adapter.photosClickPublisher.subscribe(
            {
                val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(eventId, it)
                NavHostFragment.findNavController(this).navigate(action)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
        adapter.submitList(imageIdList)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionManager.requestPermissions(permissions, PERMISSION_ALL, activity as MainActivity)
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchImagesFromFolder("images/image2.png").subscribe(
            {
                    uri ->
                imageIdList.add(Photo(uri.toString(), 0))
                adapter.submitList(imageIdList)
                adapter.notifyDataSetChanged()
            },
            {
                    throwable -> Log.e("RxFirebaseSample", throwable.toString())
            })
            .addTo(viewDisposable)
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
                    viewModel.putImageWithBitmap(capturedBitmap)
                }

                IMAGE_PICK_CODE ->{
                    returnIntent?.extras
                    val galleryUri = returnIntent?.data!!
                    val galeryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    viewModel.putImageWithBitmap(galeryBitmap)
                }

                else -> {
                }
            }

        }

    }


}
