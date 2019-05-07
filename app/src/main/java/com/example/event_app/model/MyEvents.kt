package com.example.event_app.model

class MyEvents {
    var idEvent: String? = null
    var accepted: Int = 0
    var organizer: Int = 0

    constructor(idEvent: String?, accepted: Int, organizer: Int){
        this.idEvent = idEvent
        this.accepted = accepted
        this.organizer = organizer
    }

    constructor(){}
}