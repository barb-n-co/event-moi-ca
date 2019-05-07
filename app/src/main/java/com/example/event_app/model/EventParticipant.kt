package com.example.event_app.model

class EventParticipant {
    var idUser: String? = null
    var name: String? = null
    var accepted: Int = 0
    var organizer: Int = 0

    constructor(idUser: String?, name: String?, accepted: Int, organizer: Int){
        this.idUser = idUser
        this.name = name
        this.accepted = accepted
        this.organizer = organizer
    }

    constructor(){}
}