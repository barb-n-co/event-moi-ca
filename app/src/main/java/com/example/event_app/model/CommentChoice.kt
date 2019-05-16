package com.example.event_app.model

import android.content.Context
import com.example.event_app.R


enum class CommentChoice(val id: Int) {
    DELETE(R.string.tv_delete_comment_choice),
    REPORT(R.string.tv_report_comment_choice),
    EDIT(R.string.tv_edit_comment_choice),
    LIKE(R.string.tv_like_comment_choice),
    DISLIKE(R.string.tv_dislike_comment_choice);

    fun enumToString(context: Context): String {
        return context.getString(id)
    }
}
