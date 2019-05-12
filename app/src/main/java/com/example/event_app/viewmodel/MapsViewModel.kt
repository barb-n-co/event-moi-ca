package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.AddressMap
import com.example.event_app.repository.MapsRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class MapsViewModel (private val mapsRepository: MapsRepository): BaseViewModel(){
    var mapAdress: BehaviorSubject<AddressMap> = BehaviorSubject.create()

    fun searchAdress() {
        mapsRepository.mapAdress.subscribe(
            {
                mapAdress.onNext(it)

            },
            {
                Timber.e(it)
            }
        )

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