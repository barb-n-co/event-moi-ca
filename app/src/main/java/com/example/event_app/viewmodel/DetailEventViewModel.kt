package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.event_app.model.*
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

const val COMPRESSION_QUALITY = 20

class DetailEventViewModel(private val eventsRepository: EventRepository, private val userRepository: UserRepository) : BaseViewModel()  {

    companion object {

        var eventsRepository = EventRepository
        var userRepository = UserRepository

        private fun randomPhotoNameGenerator(id: String): String {
            val generator = Random()
            var n = 10000
            n = generator.nextInt(n)

            return "${id}_${n}_${Date().time}"
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

        private fun getCurrentUser() : User {
            return userRepository.currentUser.value!!
        }

        fun putImageWithBitmap(bitmap: Bitmap, eventId: String) {

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
            val data = baos.toByteArray()

            RxFirebaseStorage.putBytes(
                eventsRepository.ref.child("$eventId/${randomPhotoNameGenerator(eventId)}.png"),data
            )
                .toFlowable()
                .subscribe(
                    {snapshot ->
                        val pushPath = eventsRepository.allPictures.child(eventId).push()
                        val key = pushPath.key
                        val path = snapshot.metadata!!.path
                        val authorId = getCurrentUser().id
                        val authorName = getCurrentUser().name ?: ""
                        authorId?.let {authorIdNotNull ->
                            key?.let {keyNotNull ->
                                val value = Photo(keyNotNull, authorIdNotNull, authorName,  0, path, mutableListOf())
                                pushImageRefToDatabase(eventId, pushPath, value)
                            }
                        }

                    },
                    {
                        Timber.e(it)
                    }
                ).addTo(CompositeDisposable())

        }
    }

    val event: BehaviorSubject<EventItem> = BehaviorSubject.create()


    fun getEventInfo(eventId: String) {
        userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.getEventDetail(eventId)
            Observable.combineLatest(
                eventsRepository.getEventDetail(eventId),
                eventsRepository.getMyEvent(idUser, eventId),
                BiFunction<Event, MyEvents, Pair<Event, MyEvents>> { t1, t2 ->
                    Pair(
                        t1,
                        t2
                    )
                }).map { response ->
                EventItem(
                    response.first.idEvent,
                    response.first.name,
                    idUser,
                    response.first.organizer,
                    response.first.dateStart,
                    response.first.dateEnd,
                    response.second.accepted,
                    response.second.organizer,
                    response.first.description,
                    response.first.idOrganizer
                )

            }.subscribe({
                event.onNext(it)
            },
                {
                    Timber.e(it)
                }).addTo(disposeBag)

        }
    }

    fun initPhotoEventListener(id: String): Observable<List<Photo>> {
        return RxFirebaseDatabase.observeValueEvent(
            eventsRepository.allPictures.child(id),
            DataSnapshotMapper.listOf(Photo::class.java)
        )
            .toObservable()
    }

    fun pickImageFromGallery(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    fun putImageWithBitmap(bitmap: Bitmap, eventId: String) {
        DetailEventViewModel.putImageWithBitmap(bitmap, eventId)
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }

    fun getAllPictures(eventId: String, context: Context) {

        RxFirebaseDatabase.observeSingleValueEvent(
            eventsRepository.allPictures.child(eventId),
            DataSnapshotMapper.listOf(Photo::class.java)
        )
            .subscribe(
                { photoList ->
                    val number = mutableListOf<String>()
                    photoList.forEach { photo ->
                        GlideApp.with(context)
                            .asBitmap()
                            .load(EventRepository.ref.child(photo.url!!))
                            .into(object : CustomTarget<Bitmap>() {

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    Timber.e("an error append $errorDrawable")
                                    Toast.makeText(context, "An error append during download", Toast.LENGTH_SHORT)
                                        .show()
                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    Timber.d("image downloading in progress")
                                    number.add(saveImage(resource, eventId, photo.id!!))
                                    if (number.size == photoList.size) {
                                        Toast.makeText(context, "Download finished", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Timber.d("onLoadCleared $placeholder")
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


    class Factory(private val eventsRepository: EventRepository, private val userRepository: UserRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventsRepository, userRepository) as T
        }
    }
}