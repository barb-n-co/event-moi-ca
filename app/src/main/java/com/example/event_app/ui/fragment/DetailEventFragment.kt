package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CustomAdapter
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.viewmodel.DetailEventViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_detail_event.*
import org.json.JSONObject
import org.kodein.di.generic.instance
import timber.log.Timber


class DetailEventFragment : BaseFragment() {
    private var json = JSONObject()
    private var json2 = JSONObject()
    private var json3 = JSONObject()
    private var json4 = JSONObject()
    private var json5 = JSONObject()
    private val viewModel : DetailEventViewModel by instance(arg = this)
    private val adapter = CustomAdapter()
    private val PERMISSION_ALL = 1
    private val PERMISSION_IMPORT = 2
    private val IMAGE_PICK_CODE = 1000
    private val CAPTURE_PHOTO = 104
    private var imagePath: String? = ""


    var imageIdList = ArrayList<Photo>()
    var loremIpsum =
        "Lorem ipvitae ultricies velit, quis pretium tellus. Aliquam ac augue accumsan arcu lobortis tincidunt. Morbi fringilla a nibh non dignissim. Integer faucibus tortor sed tellus vulputate vestibulum. Nunc ut erat non dolor congue commodo a venenatis dui. Maecenas non rutrum ipsum. Donec rhoncus ligula eget nulla feugiat porta. "

    companion object {
        const val TAG = "DETAIL_EVENT_FRAGMENT"
        fun newInstance(): DetailEventFragment = DetailEventFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        json.put("url","https://rickandmortyapi.com/api/character/avatar/1.jpeg")
        json.put("id",1)
        json2.put("url","https://rickandmortyapi.com/api/character/avatar/2.jpeg")
        json2.put("id",2)

        json3.put("url","https://rickandmortyapi.com/api/character/avatar/3.jpeg")
        json4.put("url","https://rickandmortyapi.com/api/character/avatar/4.jpeg")
        json5.put("url","https://rickandmortyapi.com/api/character/avatar/5.jpeg")


        return inflater.inflate(R.layout.fragment_detail_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mockEvent = Event(42, "MockEvent", loremIpsum, "Maintenant", "Jamais !!")

        val nestedScrollView = view.findViewById(R.id.bottomSheetScrollView) as View
        val sheetBehavior = BottomSheetBehavior.from(nestedScrollView)

        tv_eventOrga.text = "Tritri"
        tv_eventName.text = mockEvent.name
        tv_eventDateStart.text = mockEvent.dateStart
        tv_eventDateEnd.text = mockEvent.dateEnd
        tv_eventDescription.text = mockEvent.description

        btn_camera.setOnClickListener {
            val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions( permissions, PERMISSION_ALL)
            } else {
                takePhotoByCamera()
            }
        }

        btn_gallery.setOnClickListener {
            val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions, PERMISSION_IMPORT)
            } else {
                startActivityForResult(viewModel.pickImageFromGallery(), IMAGE_PICK_CODE)
            }
        }

        fab_add_photo.setOnClickListener {
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        imageIdList.add(Photo(json))
        imageIdList.add(Photo(json2))
        imageIdList.add(Photo(json3))
        imageIdList.add(Photo(json4))
        imageIdList.add(Photo(json5))



        //val mGrid = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        val mGrid = GridLayoutManager(context, 2)
        rv_listImage.layoutManager = mGrid
        rv_listImage.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(rv_listImage, false);
        adapter.photosClickPublisher.subscribe(
            {
                val action = DetailEventFragmentDirections.actionDetailEventFragmentToDetailPhotoFragment(it)
                NavHostFragment.findNavController(this).navigate(action)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        adapter.submitList(imageIdList)

    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchImagesFromFolder("images/image2.png").subscribe(
            {
                    uri ->
                imageIdList.add(Photo(JSONObject().put("url", uri.toString())))
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
