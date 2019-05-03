package com.example.event_app.model

class EventInvitation {
    var idEvent: String? = null
    var idUser: String? = null

    constructor(idEvent: String?, idUser: String?){
        this.idEvent = idEvent
        this.idUser = idUser
    }

    constructor(){}
}