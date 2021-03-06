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

    bind<DetailEventViewModel.Factory>() with provider { DetailEventViewModel.Factory(instance(), instance()) }
    bind<DetailEventViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<DetailEventViewModel.Factory>()).get(DetailEventViewModel::class.java)
    }
    bind<DetailPhotoViewModel.Factory>() with provider {
        DetailPhotoViewModel.Factory(
            instance(),
            instance(),
            instance()
        )
    }
    bind<DetailPhotoViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<DetailPhotoViewModel.Factory>()).get(DetailPhotoViewModel::class.java)
    }

    bind<HomeFragmentViewModel.Factory>() with provider { HomeFragmentViewModel.Factory(instance(), instance()) }
    bind<HomeFragmentViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<HomeFragmentViewModel.Factory>())
            .get(HomeFragmentViewModel::class.java)
    }
    bind<SplashScreenViewModel>() with factory { activity: FragmentActivity ->
        val factory = SplashScreenViewModel.Factory(instance())
        ViewModelProvider(activity, factory).get(SplashScreenViewModel::class.java)
    }

    bind<MainActivityViewModel>() with factory { activity: FragmentActivity ->
        val factory = MainActivityViewModel.Factory(instance(), instance())
        ViewModelProvider(activity, factory).get(MainActivityViewModel::class.java)
    }

    bind<LoginViewModel>() with factory { activity: FragmentActivity ->
        val factory = LoginViewModel.Factory(instance(), instance(), instance())
        ViewModelProvider(activity, factory).get(LoginViewModel::class.java)
    }

    bind<LoginViewModel.Factory>() with provider { LoginViewModel.Factory(instance(), instance(), instance()) }
    bind<LoginViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<LoginViewModel.Factory>())
            .get(LoginViewModel::class.java)
    }

    bind<ProfileViewModel.Factory>() with provider { ProfileViewModel.Factory(instance(), instance()) }
    bind<ProfileViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<ProfileViewModel.Factory>())
            .get(ProfileViewModel::class.java)
    }

    bind<AddEventFragmentViewModel.Factory>() with provider {
        AddEventFragmentViewModel.Factory(
            instance(),
            instance()
        )
    }
    bind<AddEventFragmentViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<AddEventFragmentViewModel.Factory>())
            .get(AddEventFragmentViewModel::class.java)
    }


    bind<MapsViewModel.Factory>() with provider { MapsViewModel.Factory(instance()) }
    bind<MapsViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<MapsViewModel.Factory>())
            .get(MapsViewModel::class.java)
    }


    bind<EventMapViewModel.Factory>() with provider { EventMapViewModel.Factory(instance(), instance(), instance()) }
    bind<EventMapViewModel>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<EventMapViewModel.Factory>())
            .get(EventMapViewModel::class.java)
    }




    bind<ShareGalleryViewModel>() with factory { activity: FragmentActivity ->
        val factory = ShareGalleryViewModel.Factory(instance(), instance())
        ViewModelProvider(activity, factory).get(ShareGalleryViewModel::class.java)
    }

}
