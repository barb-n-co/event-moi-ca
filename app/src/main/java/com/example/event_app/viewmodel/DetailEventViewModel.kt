package com.example.event_app.viewmodel

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.EventRepository
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Flowable
import timber.log.Timber
import java.io.ByteArrayOutputStream


class DetailEventViewModel(private val eventRepository: EventRepository) : BaseViewModel() {

    private val url = "https://firebasestorage.googleapis.com/v0/b/event-moi-ca.appspot.com/o/"

    fun fetchImagesFromFolder(url: String): Flowable<Uri> {
        return RxFirebaseStorage.getDownloadUrl(eventRepository.db.getReferenceFromUrl(fullURL(url))).toFlowable()
    }

    fun putImage(drawable: Drawable) {

        val bitmap = (drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()

        RxFirebaseStorage.putBytes(eventRepository.ref.child("images/image.png"),data).toFlowable().subscribe(
            {
                Log.d("youpee", it.uploadSessionUri.toString())
            },
            {
                Timber.e(it)
            }
        )


    }

    private fun fullURL(foldrUrl: String): String {
        return url + foldrUrl
    }

    class Factory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventRepository) as T
        }
    }
}