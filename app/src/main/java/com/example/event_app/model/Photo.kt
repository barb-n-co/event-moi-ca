package com.example.event_app.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Photo (photoJSON: JSONObject) : Serializable {

    lateinit var url: String
        private set

    init {
        try {
            url = photoJSON.getString(PHOTO_URL)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val PHOTO_URL = "url"
    }

}