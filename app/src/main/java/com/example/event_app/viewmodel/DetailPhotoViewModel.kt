package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class DetailPhotoViewModel(private val eventsRepository: EventRepository) : BaseViewModel() {
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()

    fun getPhotoDetail(eventId: String?, photoURL: String?) {
        eventId?.let {eventId ->
            photoURL?.let {photoUrl ->
                eventsRepository.getPhotoDetail(eventId, photoUrl).subscribe(
                    { it ->
                        Log.d("DetailEvent", "vm" + it.url)
                        photo.onNext(it)
                    },
                    { it ->
                        Timber.e(it)
                    }).addTo(disposeBag)
            }
        }
    }

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailPhotoViewModel(eventsRepository) as T
        }
    }
}