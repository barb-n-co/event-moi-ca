package com.example.event_app.injection

import com.example.event_app.repository.UserRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val repoModule = Kodein.Module("RepoModule") {
    bind<UserRepository>() with singleton { UserRepository.getInstance(instance()) }
}