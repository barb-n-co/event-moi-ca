package com.example.event_app.model

data class CommentaireItem(
    var commentId: String,
    var author: String,
    var authorId: String,
    var comment: String,
    var photoId: String,
    var date: String,
    var likes: List<LikeComment>
)
