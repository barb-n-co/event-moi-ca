package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.AddressMap
import com.example.event_app.repository.MapsRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MapsViewModel (private val mapsRepository: MapsRepository): BaseViewModel(){

    var mapAdress: PublishSubject<AddressMap> = PublishSubject.create()

    fun searchAdress() {
        mapsRepository.mapAdress.subscribe(
            {
                mapAdress.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)

    }

    fun searchAdress (adr : String){
        mapsRepository.getPositionWithAdress(adr)
    }


    class Factory(private val mapsRepository: MapsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(mapsRepository) as T
        }
    }
}