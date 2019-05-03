package com.example.event_app.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Photo {

    var url: String = ""
    var id: Int = -1
    var commentaires: MutableList<Commentaire> = ArrayList()
    var numberOfLike: Int = 0
    var auteur: String =""



    constructor(url: String, id: Int, commentaires: MutableList<Commentaire>, numberOfLike: Int, auteur: String) {
        this.url = url
        this.id = id
        this.commentaires = commentaires
        this.numberOfLike = numberOfLike
        this.auteur = auteur
    }

    constructor(url: String, id: Int) {
        this.url = url
        this.id = id
    }


    companion object {
        private val PHOTO_URL = "url"
        private val PHOTO_ID = "id"
        private val PHOTO_AUTEUR = "auteur"
        private val PHOTO_LIKES = "likes"
    }

    override fun toString(): String {
        return "Photo(url='$url', id=$id, numberOfLike=$numberOfLike, auteur='$auteur')"
    }


}