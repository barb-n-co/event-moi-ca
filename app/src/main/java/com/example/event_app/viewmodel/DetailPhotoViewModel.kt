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
import com.google.android.gms.tasks.Task
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
    val userLike: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val folderName = "Event-Moi-Ca"
    var isPhotoAlreadyLiked: Boolean = false


    fun getPhotoDetail(eventId: String?, photoId: String?) {
        eventId?.let { eventIdNotNull ->
            photoId?.let { photoId ->
                getNumberOfLikes(photoId)
                eventsRepository.getPhotoDetail(eventIdNotNull, photoId).subscribe(
                    { picture ->
                        Timber.d("vm: ${picture.url}")
                        photo.onNext(picture)
                    },
                    { error ->
                        Timber.e(error)
                    }).addTo(disposeBag)

                fetchComments(photoId)
            }
        }
    }

    private fun fetchComments(photoId: String){
        eventsRepository.fetchCommentaires(photoId).subscribe(
            {
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
            fetchComments(photoId)
        }
    }

    fun editComment(comment: Commentaire){
        eventsRepository.editCommentOfPhoto(comment).addOnCompleteListener {
            fetchComments(comment.photoId)
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
        return eventsRepository.getStorageReferenceForUrl(url)
    }

    fun getEvents(): Observable<List<Event>> {
        return eventsRepository.fetchEvents()
    }

    fun updateEventReportedPhotoCount(eventId: String, updateEvent: Event): Completable {
        return eventsRepository.updateEventForPhotoReporting(eventId, updateEvent)

    }

    private fun getNumberOfLikes(photoId: String) {
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
                                userLike.onNext(isPhotoAlreadyLiked)
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
                                userLike.onNext(isPhotoAlreadyLiked)
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
