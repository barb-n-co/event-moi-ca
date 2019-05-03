package com.example.event_app.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Photo {

    var url: String = ""
    var id: Int = -1
    var commentaires: MutableList<Commentaire> = ArrayList()
    var likes: Int = 0
    var auteur: String =""



    constructor(url: String, id: Int, commentaires: MutableList<Commentaire>, likes: Int, auteur: String) {
        this.url = url
        this.id = id
        this.commentaires = commentaires
        this.likes = likes
        this.auteur = auteur
    }

    constructor(url: String, id: Int) {
        this.url = url
        this.id = id
    }

    constructor() { }


    override fun toString(): String {
        return "Photo(url='$url', id=$id, numberOfLike=$likes, auteur='$auteur')"
    }


}