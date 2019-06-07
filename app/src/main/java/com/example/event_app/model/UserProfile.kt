package com.example.event_app.model

import com.google.firebase.storage.StorageReference

data class UserProfile(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var photoUrl: String = "",
    var photoReference: StorageReference? = null
)
