package com.example.event_app.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.viewmodel.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.factory
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

val viewModelModule = Kodein.Module("ViewModelModule") {

    bind<DetailEventViewModel.Factory>() with provider { DetailEventViewModel.Factory(instance()) }
    bind<DetailEventViewModel>() with factory{fragment: Fragment->
        ViewModelProvider(fragment, instance<DetailEventViewModel.Factory>()).get(DetailEventViewModel::class.java)
    }
    bind<DetailPhotoViewModel.Factory>() with provider { DetailPhotoViewModel.Factory(instance()) }
    bind<DetailPhotoViewModel>() with factory{fragment:Fragment->
        ViewModelProvider(fragment, instance<DetailPhotoViewModel.Factory>()).get(DetailPhotoViewModel::class.java)
    }

    bind<HomeFragmentViewModel.Factory>() with provider { HomeFragmentViewModel.Factory(instance()) }
    bind<HomeFragmentViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<HomeFragmentViewModel.Factory>())
            .get(HomeFragmentViewModel::class.java)
    }
    bind<SplashScreenViewModel>() with factory { activity: FragmentActivity ->
        val factory = SplashScreenViewModel.Factory(instance())
        ViewModelProvider(activity, factory).get(SplashScreenViewModel::class.java)
    }

    bind<MainActivityViewModel>() with factory { activity: FragmentActivity ->
        val factory = MainActivityViewModel.Factory(instance())
        ViewModelProvider(activity, factory).get(MainActivityViewModel::class.java)
    }

    bind<LoginViewModel>() with factory { activity: FragmentActivity ->
        val factory = LoginViewModel.Factory(instance())
        ViewModelProvider(activity, factory).get(LoginViewModel::class.java)
    }

    bind<LoginViewModel.Factory>() with provider { LoginViewModel.Factory(instance()) }
    bind<LoginViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<LoginViewModel.Factory>())
            .get(LoginViewModel::class.java)
    }

    bind<AddEventFragmentViewModel.Factory>() with provider { AddEventFragmentViewModel.Factory(instance()) }
    bind<AddEventFragmentViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<AddEventFragmentViewModel.Factory>())
            .get(AddEventFragmentViewModel::class.java)
    }
}