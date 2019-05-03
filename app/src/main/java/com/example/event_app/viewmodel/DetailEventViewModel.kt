package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class DetailEventViewModel(private val eventsRepository: EventRepository) : BaseViewModel()  {
    val event: BehaviorSubject<Event> = BehaviorSubject.create()

    fun getEventInfo(eventId: String) {
        eventsRepository.getEventDetail(eventId).subscribe(
            {
                Log.d("DetailEvent","vm"+it.name)
                event.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)

    }
    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DetailEventViewModel(eventsRepository) as T
        }
    }
}