package com.example.event_app.model

class Photo  {

    var url : String? = null
    var auteur : String? = null
    var like : Int? = null

    constructor(auteur: String, like: Int, url: String) {
        this.auteur = auteur
        this.like = like
        this.url = url
    }

    constructor() {}

    override fun toString(): String {
        return "Photo(url=$url, auteur=$auteur, like=$like)"
    }


}