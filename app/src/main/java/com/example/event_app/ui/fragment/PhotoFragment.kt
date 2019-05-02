package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.example.event_app.R
import kotlinx.android.synthetic.main.fragment_photo.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PhotoFragment : BaseFragment() {
    companion object {
        const val TAG = "PHOTOFRAGMENT"
        private val PERMISSION_ALL = 1
        private val PERMISSION_IMPORT = 2
        private val IMAGE_PICK_CODE = 1000
        private val CAPTURE_PHOTO = 104
        private var imagePath: String? = ""
        fun newInstance(): PhotoFragment = PhotoFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_take_photo.setOnClickListener{
            val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (checkSelfPermission(context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions( permissions, PERMISSION_ALL)
            } else {
                takePhotoByCamera()
            }

        }

        btn_import_photo.setOnClickListener{

            val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (checkSelfPermission(context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions, PERMISSION_IMPORT)
            } else {
                pickImageFromGallery()
            }

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL && grantResults.size == 2) {
            takePhotoByCamera()
        }

        if (requestCode == PERMISSION_IMPORT && grantResults.size == 2) {
            pickImageFromGallery()
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
                    saveImage(capturedBitmap)
                    imgv_capture_image_preview.setImageBitmap(capturedBitmap)
                }

                IMAGE_PICK_CODE ->{

                    imgv_capture_image_preview.setImageURI(returnIntent?.data)
                }

                else -> {
                }
            }

        }

    }


    private fun saveImage(finalBitmap: Bitmap) {

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val outletFrame = "Image-$n.jpg"
        val file = File(myDir, outletFrame)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 20, out)
            imagePath = file.absolutePath
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun pickImageFromGallery(){

        val intent = Intent (Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,IMAGE_PICK_CODE)


    }



}