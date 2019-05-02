package com.example.event_app.ui.activity
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.event_app.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.File
import java.io.FileOutputStream
import java.util.*




class PhotoActivity : AppCompatActivity() {
    val REQUEST_PERM_WRITE_STORAGE = 102
    private val CAPTURE_PHOTO = 104
    internal var imagePath: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        btn_take_photo.setOnClickListener{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                val PERMISSION_ALL = 1
                val PERMISSIONS = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE

                )

                if (ActivityCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                         {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
                }


                // Use
                val handler = Handler()
                handler.postDelayed({
                    takePhotoByCamera()
                }, 3000L)
            }





        }
    }




    fun takePhotoByCamera() {
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


                else -> {
                }
            }

        }

    }


    private fun saveImage(finalBitmap: Bitmap) {

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + "/")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val OutletFname = "Image-$n.jpg"
        val file = File(myDir, OutletFname)
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
}