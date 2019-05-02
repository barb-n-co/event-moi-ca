package com.example.event_app.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Photo (photoJSON: JSONObject) : Serializable {

    lateinit var url: String
        private set
     var id: Int? = null
        private set

    init {
        try {
            url = photoJSON.getString(PHOTO_URL)
            id = photoJSON.getInt(PHOTO_ID)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val PHOTO_URL = "url"
        private val PHOTO_ID = "id"
    }

}