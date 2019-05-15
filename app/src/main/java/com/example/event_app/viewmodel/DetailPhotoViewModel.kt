package com.example.event_app.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Commentaire
import com.example.event_app.model.Event
import com.example.event_app.model.LikeItem
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.NotificationRepository
import com.example.event_app.repository.UserRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class DetailPhotoViewModel(
    private val eventsRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    val commentaires: PublishSubject<List<Commentaire>> = PublishSubject.create()
    val peopleWhoLike: BehaviorSubject<List<LikeItem>> = BehaviorSubject.create()
    private val folderName = "Event-Moi-Ca"
    var isPhotoAlreadyLiked: Boolean = false


    fun getPhotoDetail(eventId: String?, photoId: String?) {
        eventId?.let { eventId ->
            photoId?.let { photoId ->
                getNumberOfLikes(photoId)
                eventsRepository.getPhotoDetail(eventId, photoId).subscribe(
                    { picture ->
                        Timber.d("vm: ${picture.url}")
                        photo.onNext(picture)
                    },
                    { error ->
                        Timber.e(error)
                    }).addTo(disposeBag)

                fetchCommentaires(photoId)
            }
        }
    }

    fun fetchCommentaires(photoId: String){
        eventsRepository.fetchCommentaires(photoId).subscribe(
            {
                Timber.d( "getCommentaires ${it[0]}")
                commentaires.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)
    }

    fun addComment(comment: String, photoId: String): Completable {
        val user = userRepository.currentUser.value!!
        return eventsRepository.addCommentToPhoto(comment, photoId, user)
    }

    fun deleteComment(photoId: String, commentId: String){
        eventsRepository.deleteCommentOfPhoto(photoId, commentId).addOnSuccessListener {
            fetchCommentaires(photoId)
        }
    }

    fun deleteComments(photoId: String): Task<Void> {
        return eventsRepository.deleteCommentsForDeletedPhoto(photoId)
    }

    fun downloadImageOnPhone(url: String): Maybe<ByteArray> {
        return eventsRepository.downloadImageFile(url)
    }

    fun saveImage(byteArray: ByteArray, eventName: String, photoId: String): String {

        val options = BitmapFactory.Options()
        options.inTargetDensity = PixelFormat.RGBA_8888
        val finalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size - 1, options)

        var imagePath = ""
        val root = Environment.getExternalStorageDirectory().toString()
        val photoFolder = File("$root/$folderName/$eventName/")
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

    fun deleteImageOrga(eventId: String, photoId: String): Task<Void> {
        return eventsRepository.deletePhotoOrga(eventId, photoId)
    }

    fun deleteRefFromFirestore(photoUrl: String): Completable {
        return eventsRepository.deletePhotoFromFireStore(photoUrl)
    }

    fun reportPhoto(eventId: String, photo: Photo, reportValue: Int): Completable {
        return eventsRepository.pushPictureReport(eventId, photo, reportValue)
    }

    fun getStorageRef(url: String): StorageReference {
        return eventsRepository.ref.child(url)
    }

    fun getEvents(): Observable<List<Event>> {
        return eventsRepository.fetchEvents()
    }

    fun updateEventReportedPhotoCount(eventId: String, updateEvent: Event): Completable {
        return eventsRepository.updateEventForPhotoReporting(eventId, updateEvent)

    }

    fun getNumberOfLikes(photoId: String) {
        eventsRepository.getLikesFromPhoto(photoId).subscribe(
            {
                peopleWhoLike.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun addLikes(photoId: String) {
        userRepository.currentUser.value?.let { user ->
            if (user.id != null && user.name != null) {

                if (isPhotoAlreadyLiked) {
                    eventsRepository.deleteLike(user.id!!, photoId)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Timber.d("like deleted")
                                isPhotoAlreadyLiked = false
                            } else {
                                Timber.e(it.exception)
                            }
                        }
                } else {
                    eventsRepository.setNewLike(user.id!!, photoId)
                        .subscribe(
                            {
                                Timber.d("new like added")
                                isPhotoAlreadyLiked = true
                            },
                            {
                                Timber.e(it)
                            }
                        )
                }

            }
        }

    }

    fun deleteLikesForPhoto(photoId: String): Task<Void> {
        return eventsRepository.removeLikes(photoId)
    }

    fun sendReportMessageToEventOwner(eventOwner: String) {
        notificationRepository.sendMessageToSpecificChannel(eventOwner)
    }


    fun initMessageReceiving() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "getInstanceId failed")
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "message with token = $token"
                Timber.d(msg)
            })

        FirebaseMessaging.getInstance().subscribeToTopic("notif_event_moi_ca")
            .addOnCompleteListener { task ->
            var msg = "subscribed !!!"
            if (!task.isSuccessful) {
                msg = "failed to subscribed"
            }
            Timber.d("message for subscribing: $msg")
        }

    }


    class Factory(
        private val eventsRepository: EventRepository,
        private val userRepository: UserRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailPhotoViewModel(eventsRepository, userRepository, notificationRepository) as T
        }
    }
}
