package com.example.event_app.repository

import com.example.event_app.model.Event
import io.reactivex.Observable

class EventRepository {

    private val listEvent = ArrayList<Event>()

    fun fectchEvents() : Observable<Event> {
        listEvent.add(Event(1, "PN 1", "Aucune", "2019-02-11", "2019-02-12"))
        listEvent.add(Event(1, "PN 2", "Aucune", "2019-02-11", "2019-02-12"))
        listEvent.add(Event(1, "PN 3", "Aucune", "2019-02-11", "2019-02-12"))
        listEvent.add(Event(1, "PN 4", "Aucune", "2019-02-11", "2019-02-12"))
        listEvent.add(Event(1, "PN 5", "Aucune", "2019-02-11", "2019-02-12"))
        listEvent.add(Event(1, "PN 6", "Aucune", "2019-02-11", "2019-02-12"))

        return
    }
}