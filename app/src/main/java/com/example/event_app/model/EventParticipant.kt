package com.example.event_app.model

class EventParticipant {
    var id: String? = null
    var name: String? = null
    var accepted: Int = 0
    var organizer: Int = 0

    constructor(id: String?, name: String?, accepted: Int, organizer: Int){
        this.id = id
        this.name = name
        this.accepted = accepted
        this.organizer = organizer
    }

    constructor(){}
}