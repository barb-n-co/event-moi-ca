package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.NumberEvent
import com.example.event_app.model.User
import com.example.event_app.model.UserProfile
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.storage.StorageReference
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(private val userRepository: UserRepository, private val eventRepository: EventRepository) :
    BaseViewModel() {

    var user: BehaviorSubject<UserProfile> = BehaviorSubject.create()
    var eventCount: BehaviorSubject<NumberEvent> = BehaviorSubject.create()
    lateinit var currentPhotoPath: String

    init {
        userRepository.currentUser.subscribe(
            {
                user.onNext(UserProfile(
                    it.id,
                    it.name,
                    it.email,
                    it.photoUrl,
                    eventRepository.getStorageReferenceForUrl(it.photoUrl)
                ))
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)

        getNumberEventUser()
    }

    fun logout() {
        userRepository.fireBaseAuth.signOut()
    }

    fun deleteAccount() {
        userRepository.currentUser.value?.id?.let { idUser ->
            userRepository.deleteAccount(idUser)
                .subscribe(
                    {
                        userRepository.deleteUser()
                    },
                    {
                        Timber.e(it)
                    }
                )
            eventRepository.deleteAllEventOfUser(idUser)
                .subscribe(
                    {
                        eventRepository.deleteParticipantWithId(it, idUser)
                    },
                    {
                        Timber.e(it)
                    }
                ).addTo(disposeBag)
        }
    }

    fun getNumberEventUser() {
        userRepository.currentUser.value?.id?.let {
            eventRepository.myEvents.subscribe(
                { myEventsList ->
                    val numberEvent = NumberEvent(0, 0, 0)
                    myEventsList.forEach { myEvent ->
                        if (myEvent.isEmtyEvent == 1) {
                            // do nothing -> don't count this event
                        } else if (myEvent.accepted == 0 && myEvent.organizer == 0) {
                            numberEvent.invitation += 1
                        } else if (myEvent.accepted == 1 && myEvent.organizer == 0) {
                            numberEvent.participate += 1
                        } else if (myEvent.organizer == 1) {
                            numberEvent.organizer += 1
                        }
                    }
                    eventCount.onNext(numberEvent)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)
        }
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun pickImageFromGallery(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    fun getBitmapWithPath(): Bitmap {
        return BitmapFactory.decodeFile(currentPhotoPath)
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }

    fun putImageWithBitmap(bitmap: Bitmap, userId: String, fromGallery: Boolean) {

        val data: ByteArray
        val baos = ByteArrayOutputStream()
        data = if (fromGallery) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, HIGH_COMPRESSION_QUALITY, baos)
            baos.toByteArray()
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
            baos.toByteArray()
        }

        eventRepository.putBytesToFireStoreForUserPhotoProfile(userId, data, userId)
            .subscribe(
                { snapshot ->
                    user.value?.let { currentUser ->
                        val url = snapshot.metadata!!.path
                        currentUser.photoUrl = url

                        userRepository.updateUser(currentUser.id!!, currentUser.email!!, currentUser.name!!, url)
                    }


                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)

    }

    class Factory(private val userRepository: UserRepository, private val eventRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, eventRepository) as T
        }
    }
}
