package com.example.lpiem.theelderscrolls.injection

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.viewmodel.MainActivityViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.factory
import org.kodein.di.generic.instance

val viewModelModule = Kodein.Module("ViewModelModule") {

    bind<MainActivityViewModel>() with factory { activity: FragmentActivity ->
        val factory = MainActivityViewModel.Factory(instance())
        ViewModelProvider(activity, factory).get(MainActivityViewModel::class.java)
    }

}