package com.example.event_app.injection

import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val repoModule = Kodein.Module("RepoModule") {
    bind<UserRepository>() with singleton { UserRepository }
    bind<EventRepository>() with singleton { EventRepository }
}