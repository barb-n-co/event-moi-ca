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


    fun putImageWithBitmap(bitmap: Bitmap, id: String) {

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()

        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        RxFirebaseStorage.putBytes(eventsRepository.ref.child("images/${id}_${n}_${Date().time}.png"),data).toFlowable().subscribe(
            {

                val pushPath = eventsRepository.allPictures.child(id).push()
                val key = pushPath.key
                val path = it.metadata!!.path
                key?.let {
                    val value = Photo(key,"moi", 0, path)

                    RxFirebaseDatabase.setValue(eventsRepository.allPictures.child(id),pushPath.setValue(value))
                        .subscribe(
                            {
                                Timber.d("success by always catch error")
                            },
                            {
                                Timber.e("setValue error :  ${it}")
                            }
                        ).addTo(CompositeDisposable())
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(CompositeDisposable())

    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
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

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventsRepository) as T
        }
    }
}