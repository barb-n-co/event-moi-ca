package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*

class ShareGalleryViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    lateinit var currentUser: User
    val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()


    fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
            userRepository.getUserNameFromFirebase()
            currentUser = userRepository.currentUser.value!!
        }
        return user
    }
    fun getEvents() {
        eventsRepository.fetchEvents().subscribe(
            {
                eventList.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
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
                    Log.d("ShareGall","success but always catch error")
                },
                {
                    Log.d("ShareGall","setValue error :  $it")
                }
            ).addTo(CompositeDisposable())
    }

    private fun randomPhotoNameGenerator(id: String): String {
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        return "${id}_${n}_${Date().time}"
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShareGalleryViewModel(userRepository, eventsRepository) as T
        }
    }
}