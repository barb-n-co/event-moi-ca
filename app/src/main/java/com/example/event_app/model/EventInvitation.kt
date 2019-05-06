package com.example.event_app.model

class EventInvitation {
    lateinit var key: String
    lateinit var idEvent: String
    lateinit var idUser: String

    constructor(key: String, idEvent: String, idUser: String){
        this.key = key
        this.idEvent = idEvent
        this.idUser = idUser
    }

    constructor(){}
}