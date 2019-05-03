package com.example.event_app.injection

import com.example.event_app.manager.PermissionManager
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val managerModule = Kodein.Module("ManagerModule") {
    bind<PermissionManager>() with singleton { PermissionManager(instance()) }
}
