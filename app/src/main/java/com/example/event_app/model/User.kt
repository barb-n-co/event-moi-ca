package com.example.event_app.model

import android.net.Uri

class User {
    var id: String? = null
    var name: String? = null
    var email: String? = null

    constructor(id: String?, name: String?, email: String?){
        this.id = id
        this.name = name
        this.email = email
    }

    constructor(){}
}