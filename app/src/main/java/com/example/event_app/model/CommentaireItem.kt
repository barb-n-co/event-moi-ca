package com.example.event_app.model

import com.google.firebase.storage.StorageReference

data class CommentaireItem(
    var commentId: String,
    var author: String,
    var authorId: String,
    var comment: String,
    var photoId: String,
    var date: String,
    var likes: List<LikeComment>,
    var reported: Int = 0,
    var profileImage: String = "",
    var profileImageStorageRef: StorageReference? = null
)
