package com.example.event_app.model

class MyEvents {
    lateinit var key: String
    var idEvent: String? = null
    var idUser: String? = null

    constructor(key: String, idEvent: String?, idUser: String?){
        this.key = key
        this.idEvent = idEvent
        this.idUser = idUser
    }

    constructor(){}
}