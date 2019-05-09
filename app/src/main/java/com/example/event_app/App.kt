package com.example.event_app

import android.app.Application
import android.content.Context
import com.example.event_app.injection.managerModule
import com.example.event_app.injection.repoModule
import com.example.event_app.injection.viewModelModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import timber.log.Timber

class App : Application(), KodeinAware {

    override val kodein = Kodein.lazy {

        bind<Application>() with singleton { this@App}
        bind<Context>() with singleton { instance<Application>() }

        import(repoModule)
        import(viewModelModule)
        import(managerModule)
    }

    override fun onCreate() {
        super.onCreate()

            Timber.plant(Timber.DebugTree())

    }

}