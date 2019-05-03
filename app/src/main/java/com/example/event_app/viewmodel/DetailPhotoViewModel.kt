package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class DetailPhotoViewModel (private val eventsRepository: EventRepository) : BaseViewModel()  {
    val photo: BehaviorSubject<Photo> = BehaviorSubject.create()

    fun getPhotoDetail(eventId: String, photoId: Int) {
        eventsRepository.getPhotoDetail(eventId, photoId).subscribe(
            {
                Log.d("DetailEvent","vm"+it.url)
                photo.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)

    }
    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailPhotoViewModel(eventsRepository) as T
        }
    }
}