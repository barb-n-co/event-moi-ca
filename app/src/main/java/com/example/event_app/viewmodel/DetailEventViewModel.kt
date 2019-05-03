package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.EventRepository
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Observable
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class DetailEventViewModel(private val eventRepository: EventRepository) : BaseViewModel() {

    private val url = "https://firebasestorage.googleapis.com/v0/b/event-moi-ca.appspot.com/o/"

    fun fetchImagesFromFolder(url: String): Observable<Uri> {
        return RxFirebaseStorage.getDownloadUrl(eventRepository.db.getReferenceFromUrl(fullURL(url))).toObservable()
    }

    fun putImageWithBitmap(bitmap: Bitmap) {

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()

        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        RxFirebaseStorage.putBytes(eventRepository.ref.child("images/image$n.png"),data).toFlowable().subscribe(
            {
                Log.d("youpee", it.uploadSessionUri.toString())
            },
            {
                Timber.e(it)
            }
        )

    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }


    private fun fullURL(foldrUrl: String): String {
        return url + foldrUrl
    }

    fun saveImage(finalBitmap: Bitmap): String {

        var imagePath = ""
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
        return imagePath
    }

    fun pickImageFromGallery(): Intent{
        val intent = Intent (Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    class Factory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventRepository) as T
        }
    }
}