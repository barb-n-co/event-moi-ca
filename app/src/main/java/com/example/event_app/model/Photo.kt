package com.example.event_app.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Photo (photoJSON: JSONObject) : Serializable {

    lateinit var url: String
        private set
     var id: Int? = null
        private set
    //var commentaires: MutableList<String> = ArrayList()
    //    private set
    var numberOfLike: Int = 0
        private set
    var auteur: String =""
    private set

    init {
        try {
            url = photoJSON.getString(PHOTO_URL)
            auteur = photoJSON.getString(PHOTO_AUTEUR)
            id = photoJSON.getInt(PHOTO_ID)
            //commentaires = photoJSON.getJSONObject("commentaire");
            numberOfLike = photoJSON.getInt(PHOTO_LIKES)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
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