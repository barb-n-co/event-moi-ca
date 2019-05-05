package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
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

    fun putImageWithBitmap(bitmap: Bitmap, eventId: String) {

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
        val data = baos.toByteArray()

        RxFirebaseStorage.putBytes(eventsRepository.ref.child("$eventId/${randomPhotoNameGenerator(eventId)}.png"),data).toFlowable().subscribe(
            {

                val pushPath = eventsRepository.allPictures.child(eventId).push()
                val key = pushPath.key
                val path = it.metadata!!.path
                UserRepository.currentUser.value?.id.let {author ->
                    author?.let {certifiedNotNullAuthor ->
                        key?.let {
                            val value = Photo(key,certifiedNotNullAuthor, 0, path, mutableListOf())
                            pushImageRefToDatabase(eventId, pushPath, value)
                        }
                    }
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

    fun getAllPictures(eventId: String, context: Context) {

        RxFirebaseDatabase.observeSingleValueEvent(eventsRepository.allPictures.child(eventId), DataSnapshotMapper.listOf(Photo::class.java))
            .subscribe(
                {photoList ->
                    var number = 0
                    for ((i, photo) in photoList.withIndex()) {
                        GlideApp.with(context)
                            .asBitmap()
                            .load(EventRepository.ref.child(photo.url!!))
                            .into(object : CustomTarget<Bitmap>(){

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    Timber.e( "an error append $errorDrawable")
                                    number++
                                    if (number == photoList.size) {
                                        Toast.makeText(context, "Download finished", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    Timber.d( "image downloading in progress")
                                    saveImage(resource, eventId, photo.id!!)
                                    number++
                                    if (number == photoList.size) {
                                        Toast.makeText(context, "Download finished", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Timber.d( "onLoadCleared $placeholder")
                                }

                            })

                    }

                },
                {

                }
            ).addTo(CompositeDisposable())

    }

    fun saveImage(finalBitmap: Bitmap, eventName: String, photoId: String): String {

        var imagePath = ""
        val root = Environment.getExternalStorageDirectory().toString()
        val photoFolder = File("$root/Event-Moi-Ca/$eventName/")
        photoFolder.mkdirs()
        val outletFrame = "$photoId.jpg"
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