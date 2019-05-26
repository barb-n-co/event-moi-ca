package com.example.event_app.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.*
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.NotificationRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
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
    val commentaires: PublishSubject<List<CommentaireItem>> = PublishSubject.create()
    val peopleWhoLike: BehaviorSubject<List<LikeItem>> = BehaviorSubject.create()
    val userLike: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val menuListener: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val messageDispatcher: BehaviorSubject<String> = BehaviorSubject.create()
    val onBackPressedTrigger: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val numberOfLikes: BehaviorSubject<String> = BehaviorSubject.create()
    val photoTaker: BehaviorSubject<String> = BehaviorSubject.create()

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

    fun getPhotographProfilePicture(userId: String) {
        userRepository.getUserById(userId).subscribe(
            {
                photoTaker.onNext(it.photoUrl)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    private fun fetchComments(photoId: String) {
        Flowable.zip(
            eventsRepository.fetchCommentaires(photoId),
            eventsRepository.getCommentLikes(photoId),
            BiFunction { t1: List<Commentaire>, t2: List<LikeComment> ->
                Pair(t1, t2)
            }
        ).map { result ->
            result.first.map {
                CommentaireItem(
                    it.commentId,
                    it.author,
                    it.authorId,
                    it.comment,
                    it.photoId,
                    it.date,
                    result.second.filter { like ->
                        like.commentId == it.commentId
                    },
                    it.reported
                )
            }
        }.subscribe(
            {
                commentaires.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun addCommentLike(userId: String, commentId: String, photoId: String) {
        eventsRepository.addNewCommentLike(userId, commentId, photoId).subscribe(
            {
                fetchComments(photoId)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun reportComment(commentaireItem: CommentaireItem, message: String) {
        val commentReported = Commentaire(
            commentaireItem.commentId,
            commentaireItem.author,
            commentaireItem.authorId,
            commentaireItem.comment,
            commentaireItem.photoId,
            commentaireItem.date,
            1
        )
        eventsRepository.editCommentOfPhoto(commentReported).subscribe(
            {
                messageDispatcher.onNext(message)
                fetchComments(commentaireItem.photoId)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun removeCommentLike(likeId: String, photoId: String) {
        eventsRepository.removeCommentLike(likeId, photoId).addOnSuccessListener {
            fetchComments(photoId)
        }
    }

    fun addComment(comment: String, photoId: String): Completable {
        val user = userRepository.currentUser.value!!
        return eventsRepository.addCommentToPhoto(comment, photoId, user)
    }

    fun deleteComment(photoId: String, commentId: String) {
        eventsRepository.deleteCommentOfPhoto(photoId, commentId).addOnSuccessListener {
            fetchComments(photoId)
        }
    }

    fun editComment(comment: Commentaire) {
        eventsRepository.editCommentOfPhoto(comment)
            .subscribe(
                {
                    Timber.d("comment edited")
                    fetchComments(comment.photoId)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)
    }

    private fun deleteComments(photoId: String) {
        eventsRepository.deleteCommentsForDeletedPhoto(photoId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("comments successfully deleted")
            } else {
                Timber.e("a problem occurred in deleting comments for the photo: ${task.exception}")
            }
        }
    }

    fun downloadImageOnPhone(url: String, eventId: String, photoId: String, message: String, errorMessage: String) {
        eventsRepository.downloadImageFile(url)
            .subscribe(
                { byteArray ->
                    if (saveImage(byteArray, eventId, photoId).isNotEmpty()) {
                        messageDispatcher.onNext(message)
                    } else {
                        messageDispatcher.onNext(errorMessage)
                    }
                },
                { error ->
                    Timber.e(error)
                    messageDispatcher.onNext(errorMessage)
                }
            ).addTo(disposeBag)
    }

    private fun saveImage(byteArray: ByteArray, eventName: String, photoId: String): String {

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

    fun deleteImageOrga(
        eventId: String,
        photoId: String,
        url: String,
        isReported: Int,
        message: String,
        errorMessage: String
    ) {
        eventsRepository.deletePhotoOrga(eventId, photoId).addOnCompleteListener {
            if (it.isSuccessful) {
                /** delete from FireStore */
                deleteRefFromFirestore(url, message, errorMessage)
                /** delete likes */
                deleteLikesForPhoto(photoId)
                /** delete comments */
                deleteComments(photoId)
                /** update reported count if needed */
                if (isReported == 1) {
                    eventsRepository.getEventDetail(eventId)
                        .subscribe(
                            {
                                val event = it
                                event.reportedPhotoCount--
                                updateEventReportedPhotoCount(event.idEvent, event)
                                    .subscribe(
                                        {
                                            Timber.d("event Updated")
                                        },
                                        {
                                            Timber.e(it)
                                        }
                                    ).addTo(disposeBag)
                            },
                            {
                                Timber.e(it)
                            }
                        ).addTo(disposeBag)
                }

            } else {
                Timber.e("an error occurred : ${it.exception}")
            }
        }
    }

    private fun deleteRefFromFirestore(photoUrl: String, message: String, errorMessage: String) {
        eventsRepository.deletePhotoFromFireStore(photoUrl)
            .subscribe(
                {
                    messageDispatcher.onNext(message)
                    onBackPressedTrigger.onNext(true)
                },
                { error ->
                    Timber.e(error)
                    messageDispatcher.onNext(errorMessage + error)
                }
            ).addTo(disposeBag)
    }

    private fun reportPhoto(eventId: String, photo: Photo, reportValue: Int): Completable {
        return eventsRepository.pushPictureReport(eventId, photo, reportValue)
    }

    fun getStorageRef(url: String): StorageReference {
        return eventsRepository.getStorageReferenceForUrl(url)
    }

    fun reportOrValidateImage(eventId: String, photo: Photo, delta: Int, message: String, errorMessage: String) {
        val reportValue = if (delta > 0) 1 else 0

        eventsRepository.getEventDetail(eventId)
            .subscribe(
                {
                    reportPhoto(eventId, photo, reportValue)
                        .subscribe(
                            {
                                Timber.d("photo unreported ")
                                messageDispatcher.onNext(message)
                                if (reportValue == 1) {
                                    sendReportMessageToEventOwner(it.idOrganizer)
                                }
                            },
                            {
                                messageDispatcher.onNext(errorMessage)
                                Timber.e(it)
                            }
                        ).addTo(disposeBag)


                    if (it.idEvent == eventId) {
                        val updateEvent = it
                        updateEvent.reportedPhotoCount = it.reportedPhotoCount + delta
                        updateEventReportedPhotoCount(eventId, updateEvent)
                            .subscribe(
                                {
                                    Timber.e("event updated")
                                    menuListener.onNext(true)
                                },
                                {
                                    messageDispatcher.onNext(errorMessage)
                                    Timber.e("error for update event reported photo = $it")
                                }
                            )
                    }

                },
                {
                    Timber.e(it)
                    messageDispatcher.onNext(errorMessage)
                }
            ).addTo(disposeBag)
    }

    private fun updateEventReportedPhotoCount(eventId: String, updateEvent: Event): Completable {
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

    private fun deleteLikesForPhoto(photoId: String) {
        eventsRepository.removeLikes(photoId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("photo likes deleted")
            } else {
                Timber.e("a problem occurred in deleting likes for the photo: ${task.exception}")
            }
        }
    }

    private fun sendReportMessageToEventOwner(eventOwner: String) {
        notificationRepository.sendMessageToSpecificChannel(eventOwner)
    }

    fun getNumberOfLike(list: List<LikeItem>?) {
        var number = 0
        list?.forEach { item ->
            number++
            if (item.userId == UserRepository.currentUser.value?.id) {
                isPhotoAlreadyLiked = true
                userLike.onNext(true)
            }
        }
        numberOfLikes.onNext(number.toString())
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
