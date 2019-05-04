package com.example.event_app.model

class Photo  {

    var id : String? = null
    var url : String? = null
    var auteur : String? = null
    var like : Int? = null

    constructor(id: String, auteur: String, like: Int, url: String) {
        this.id = id
        this.auteur = auteur
        this.like = like
        this.url = url
    }

    constructor() {}

    override fun toString(): String {
        return "Photo(id= $id, url= $url, auteur= $auteur, like= $like)"
    }


}