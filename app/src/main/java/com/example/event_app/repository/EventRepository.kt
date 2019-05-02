package com.example.event_app.repository

import android.util.Log
import com.example.event_app.model.Event
import com.google.firebase.database.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class EventRepository {

    private val database = FirebaseDatabase.getInstance()
    private val eventsRef : DatabaseReference = database.getReference("allEvents")
    private val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()

    fun fectchEvents() {
        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.getValue(Event::class.java)
                Log.d("FIREBASE", post.toString())
            }
        })
    }
}