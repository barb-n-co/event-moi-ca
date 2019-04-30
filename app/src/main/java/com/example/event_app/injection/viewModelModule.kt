package com.example.lpiem.theelderscrolls.injection

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.viewmodel.HomeFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.factory
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

val viewModelModule = Kodein.Module("ViewModelModule") {

    bind<HomeFragmentViewModel.Factory>() with provider { HomeFragmentViewModel.Factory(instance()) }
    bind<HomeFragmentViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<HomeFragmentViewModel.Factory>())
            .get(HomeFragmentViewModel::class.java)
    }
}