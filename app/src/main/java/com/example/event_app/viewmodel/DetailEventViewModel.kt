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
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

const val COMPRESSION_QUALITY = 20

class DetailEventViewModel(private val eventsRepository: EventRepository) : BaseViewModel()  {

    val event: BehaviorSubject<Event> = BehaviorSubject.create()


    fun getEventInfo(eventId: String) {
        eventsRepository.getEventDetail(eventId).subscribe(
            {
                Log.d("DetailEvent","vm"+it.name)
                event.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)

    }

    fun initPhotoEventListener(id: String): Observable<List<Photo>> {
        return RxFirebaseDatabase.observeValueEvent(eventsRepository.allPictures.child(id), DataSnapshotMapper.listOf(Photo::class.java))
            .toObservable()
    }

    fun pickImageFromGallery(): Intent {
        val intent = Intent (Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    fun putImageWithBitmap(bitmap: Bitmap, id: String) {

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
        val data = baos.toByteArray()

        RxFirebaseStorage.putBytes(eventsRepository.ref.child("images/${randomPhotoNameGenerator(id)}.png"),data).toFlowable().subscribe(
            {

                val pushPath = eventsRepository.allPictures.child(id).push()
                val key = pushPath.key
                val path = it.metadata!!.path
                val author = id
                key?.let {
                    val value = Photo(key,author, 0, path, mutableListOf())
                    pushImageRefToDatabase(id, pushPath, value)
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(CompositeDisposable())

    }

    private fun pushImageRefToDatabase(id: String, pushPath: DatabaseReference, value: Photo) {
        RxFirebaseDatabase.setValue(eventsRepository.allPictures.child(id),pushPath.setValue(value))
            .subscribe(
                {
                    Timber.d("success but always catch error")
                },
                {
                    Timber.e("setValue error :  $it")
                }
            ).addTo(CompositeDisposable())
    }

    private fun randomPhotoNameGenerator(id: String): String {
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        return "${id}_${n}_${Date().time}"
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }

    fun getAllPictures(eventId: String) {

        RxFirebaseStorage.getStream(eventsRepository.ref.child(eventId)) {task, input ->

        }
            .subscribe(
                {

                },
                {

                }
        ).addTo(CompositeDisposable())
    }

    fun saveImage(finalBitmap: Bitmap, eventName: String): String {

        var imagePath = ""
        val root = Environment.getExternalStorageDirectory().toString()
        val photoFolder = File("$root/$eventName/")
        photoFolder.mkdirs()
        val outletFrame = "${randomPhotoNameGenerator("pic")}.jpg"
        val file = File(photoFolder, outletFrame)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            imagePath = file.absolutePath
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imagePath
    }


    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventsRepository) as T
        }
    }
}