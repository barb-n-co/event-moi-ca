package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.event_app.R
import com.example.event_app.model.*
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.example.event_app.utils.GlideApp
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val COMPRESSION_QUALITY = 50
const val HIGH_COMPRESSION_QUALITY = 15

class DetailEventViewModel(private val eventsRepository: EventRepository, private val userRepository: UserRepository) :
    BaseViewModel() {

    val participants: BehaviorSubject<List<User>> = BehaviorSubject.create()
    val event: BehaviorSubject<EventItem> = BehaviorSubject.create()
    private var eventLoaded: Event? = null
    val loading: PublishSubject<Boolean> = PublishSubject.create()
    var organizerPhoto: PublishSubject<String> = PublishSubject.create()
    lateinit var currentPhotoPath: String

    init {
        DetailEventViewModel.disposeBag = disposeBag
    }

    companion object {

        var disposeBag = CompositeDisposable()
        var eventsRepository = EventRepository
        var userRepository = UserRepository

        private fun randomPhotoNameGenerator(id: String): String {
            val generator = Random()
            var n = 10000
            n = generator.nextInt(n)

            return "${id}_${n}_${Date().time}"
        }

        private fun pushImageRefToDatabase(id: String, pushPath: DatabaseReference, value: Photo) {
            eventsRepository.pushImageToDatabase(id, pushPath, value)
                .subscribe(
                    {
                        Timber.d("success but always catch error")
                    },
                    {
                        Timber.e("setValue error :  $it")
                    }
                ).addTo(disposeBag)
        }

        fun putImageWithBitmap(bitmap: Bitmap, eventId: String, fromGallery: Boolean) {

            val data: ByteArray
            val baos = ByteArrayOutputStream()
            data = if (fromGallery) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, HIGH_COMPRESSION_QUALITY, baos)
                baos.toByteArray()
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
                baos.toByteArray()
            }

            eventsRepository.putBytesToFireStore(eventId, data, randomPhotoNameGenerator(eventId))
                .subscribe(
                    { snapshot ->
                        val pushPath = eventsRepository.createPicturePath(eventId)
                        val key = pushPath.key
                        val path = snapshot.metadata!!.path
                        val authorId = getCurrentUser().id
                        val authorName = getCurrentUser().name ?: ""
                        authorId?.let { authorIdNotNull ->
                            key?.let { keyNotNull ->
                                val value = Photo(keyNotNull, authorIdNotNull, authorName, 0, path, mutableListOf())
                                pushImageRefToDatabase(eventId, pushPath, value)
                            }
                        }

                    },
                    {
                        Timber.e(it)
                    }
                ).addTo(disposeBag)

        }

        private fun getCurrentUser(): User {
            return Companion.userRepository.currentUser.value!!
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

    fun changeActivationEvent(state: Boolean) {
        eventLoaded?.let {
            val newEvent = it.apply {
                this.activate = if (state) 1 else 0
            }
            eventsRepository.addEvent(it.idOrganizer, it.organizer, newEvent)
        }
    }

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
                }).doOnSubscribe {
                loading.onNext(true)
            }.map { response ->
                eventLoaded = response.first
                EventItem(
                    response.first.idEvent,
                    response.first.name,
                    idUser,
                    response.first.organizer,
                    response.first.place,
                    response.first.dateStart,
                    response.first.dateEnd,
                    response.second.accepted,
                    response.second.organizer,
                    response.first.description,
                    response.first.idOrganizer,
                    response.first.reportedPhotoCount,
                    response.first.isEmptyEvent,
                    response.first.organizerPhoto,
                    response.first.latitude,
                    response.first.longitude,
                    response.first.activate
                )

            }.subscribe({
                getPhotographOrganizerPicture(it.idUser)
                event.onNext(it)
            },
                {
                    Timber.e(it)
                },
                {
                    loading.onNext(false)
                }).addTo(disposeBag)

        }
        getParticipant(eventId)
    }

    fun getPhotographOrganizerPicture(userId: String) {
        userRepository.getUserById(userId)
            .subscribe(
            {
                organizerPhoto.onNext(it.photoUrl)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun getStorageRef(url: String): StorageReference {
        return eventsRepository.getStorageReferenceForUrl(url)
    }

    fun getParticipant(eventId: String) {
        eventsRepository.getParticipants(eventId).subscribe(
            {
                participants.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun initPhotoEventListener(id: String): Observable<List<Photo>> {
        return eventsRepository.getAllPicturesStream(id)
    }

    fun pickImageFromGallery(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    fun putImageWithBitmap(bitmap: Bitmap, eventId: String, fromGallery: Boolean) {
        DetailEventViewModel.putImageWithBitmap(bitmap, eventId, fromGallery)
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }

    fun getBitmapWithPath(): Bitmap {
        return BitmapFactory.decodeFile(currentPhotoPath)
    }

    fun getAllPictures(eventId: String, context: Context, folderName: String) {

        eventsRepository.fetchPictures(eventId)
            .subscribe(
                { photoList ->
                    val number = mutableListOf<String>()
                    photoList.forEach { photo ->
                        GlideApp.with(context)
                            .asBitmap()
                            .load(eventsRepository.getStorageReferenceForUrl(photo.url))
                            .transition(GenericTransitionOptions.with(R.anim.fade_in))
                            .into(object : CustomTarget<Bitmap>() {

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    Timber.e("an error append $errorDrawable")
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_occured_downloading_photo), Toast.LENGTH_SHORT
                                    ).show()
                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    Timber.d("image downloading in progress")
                                    val path = saveImage(resource, folderName, photo.id)
                                    if (path.isNotEmpty()) {
                                        number.add(path)
                                    }
                                    if (number.size == photoList.size) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.download_complete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_occured_downloading_photo),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Timber.d("onLoadCleared $placeholder")
                                }
                            })
                    }
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)

    }

    fun saveImage(finalBitmap: Bitmap, eventName: String, photoId: String): String {

        var imagePath = ""
        val root = Environment.getExternalStorageDirectory().toString()
        val photoFolder = File("$root/Event-Moi-Ca/${eventName.replace(" ", "_")}/")
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

    fun removeParticipant(idEvent: String, userId: String) {
        eventsRepository.exitEvent(idEvent, userId)
        getParticipant(idEvent)
    }

    fun exitEvent(idEvent: String): Task<Void>? {
        return userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.exitEvent(idEvent, idUser)
        }
    }

    fun exitMyEvent(idEvent: String): Task<Void>? {
        return userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.exitMyEvent(idEvent, idUser)
        }
    }


    fun deleteEvent(idEvent: String): Task<Void> {

        /** delete pictures */
        deletePictures(idEvent)
        /** delete events */
        removeEventsReference(idEvent)
        /** delete myEvents */
        val currentUserId = userRepository.currentUser.value?.id
        currentUserId?.let {
            eventsRepository.removeUserEvent(it, idEvent)
        }

        return eventsRepository.removeParticipant(idEvent)
    }

    private fun removeEventsReference(idEvent: String) {
        userRepository.getAllUsers().subscribe(
            {
                it.forEach { user ->
                    user.id?.let { userId ->
                        eventsRepository.removeParticipation(userId, idEvent).addOnCompleteListener {
                            eventsRepository.removeEvent(idEvent)
                        }
                    }
                }
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    private fun deletePictures(idEvent: String) {
        eventsRepository.fetchPictures(idEvent).subscribe(
            {
                it.forEach { picture ->
                    eventsRepository.removeLikes(picture.id).addOnCompleteListener { task ->
                        Timber.d("task is succesful ? : ${task.isSuccessful}")
                    }

                    eventsRepository.deletePhotoFromFireStore(picture.url).subscribe(
                        {
                            Timber.d("photo deleted from fireStore")
                        },
                        {
                            Timber.e(it)
                        }
                    ).addTo(disposeBag)
                }
                eventsRepository.removePictureReference(idEvent)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun createMapIntent(address: String): Intent {
        return Uri.parse(address).let { location ->
            Intent(Intent.ACTION_VIEW, location)
        }
    }

    class Factory(private val eventsRepository: EventRepository, private val userRepository: UserRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventsRepository, userRepository) as T
        }
    }
}
