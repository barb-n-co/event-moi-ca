package com.example.event_app.repository

import com.example.event_app.model.Event
import com.google.firebase.database.*
import durdinapps.rxfirebase2.RxFirebaseDatabase
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.DataSnapshotMapper
import io.reactivex.Flowable

object EventRepository {

    private val database = FirebaseDatabase.getInstance()
    private val eventsRef: DatabaseReference = EventRepository.database.getReference("events")

    fun fetchEvents() : Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
    }
}