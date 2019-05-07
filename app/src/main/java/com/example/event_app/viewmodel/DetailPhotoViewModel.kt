package com.example.event_app.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Commentaire
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.google.android.gms.tasks.Task
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class DetailPhotoViewModel(private val eventsRepository: EventRepository) : BaseViewModel() {
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    val commentaires: BehaviorSubject<List<Commentaire>> = BehaviorSubject.create()
    private val folderName = "Event-Moi-Ca"


    fun getPhotoDetail(eventId: String?, photoId: String?) {
        eventId?.let {eventId ->
            photoId?.let {photoId ->
                eventsRepository.getPhotoDetail(eventId, photoId).subscribe(
                    { picture ->
                        Log.d("DetailEvent", "vm" + picture.url)
                        photo.onNext(picture)
                    },
                    { error ->
                        Timber.e(error)
                    }).addTo(disposeBag)

                eventsRepository.fetchCommentaires(photoId).subscribe(
                    {
                        Log.d("DetailEvent", "getCommentaires ${it.get(0)}")
                        commentaires.onNext(it)
                    },
                    {
                        Timber.e(it)
                    }).addTo(disposeBag)
            }
        }

    }

    fun downloadImageOnPhone(url: String): Maybe<ByteArray> {
        return RxFirebaseStorage.getBytes(EventRepository.ref.child(url), 2000*1000*4)
    }

    fun saveImage(byteArray: ByteArray, eventName: String, photoId: String): String {

        val options = BitmapFactory.Options()
        options.inTargetDensity = PixelFormat.RGBA_F16
        val finalBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size - 1, options)

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

    fun deleteImageOrga(eventId: String,photoId: String): Task<Void> {
        return eventsRepository.allPictures.child(eventId).child(photoId).removeValue()
    }

    fun deleteRefFromFirestore(photoUrl: String): Completable {
        return RxFirebaseStorage.delete(eventsRepository.ref.child(photoUrl))
    }

    fun reportPhoto(eventId: String, photo: Photo): Completable {
        return eventsRepository.pushPictureReport(eventId, photo)
    }

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailPhotoViewModel(eventsRepository) as T
        }
    }
}