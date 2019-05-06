package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Commentaire
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.google.android.gms.tasks.Task
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Completable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class DetailPhotoViewModel(private val eventsRepository: EventRepository) : BaseViewModel() {
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()
    val commentaires: BehaviorSubject<List<Commentaire>> = BehaviorSubject.create()


    fun getPhotoDetail(eventId: String?, photoId: String?) {
        eventId?.let {eventId ->
            photoId?.let {photoId ->
                eventsRepository.getPhotoDetail(eventId, photoId).subscribe(
                    { it ->
                        Log.d("DetailEvent", "vm" + it.url)
                        photo.onNext(it)
                    },
                    { it ->
                        Timber.e(it)
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

    fun deleteImageOrga(eventId: String,photoId: String): Task<Void> {
        return eventsRepository.allPictures.child(eventId).child(photoId).removeValue()
    }

    fun deleteRefFromFirestore(photoUrl: String): Completable {
        return RxFirebaseStorage.delete(eventsRepository.ref.child(photoUrl))

    }

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailPhotoViewModel(eventsRepository) as T
        }
    }
}