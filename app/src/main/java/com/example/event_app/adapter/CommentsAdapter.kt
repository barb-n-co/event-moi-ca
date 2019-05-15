package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Commentaire
import com.example.event_app.ui.fragment.CommentChoiceDialogFragment
import com.example.event_app.ui.fragment.FilterDialogFragment
import com.example.event_app.ui.fragment.HomeFragment
import kotlinx.android.synthetic.main.comment_item.view.*
import java.net.URLDecoder

class CommentsAdapter(private val idUser: String, private val idOrganizer: String?, private val commentSelectedListener: (String) -> Unit): ListAdapter<Commentaire, CommentsAdapter.CommentsViewHolder>(DiffCardCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(comment: Commentaire) {
            if(comment.authorId.equals(idUser)){
                itemView.iv_user_comment_item.visibility = View.GONE
                itemView.tv_name_comment_item.visibility = View.GONE
                itemView.tv_date_other_user_comment_item.visibility = View.INVISIBLE
                itemView.tv_date_user_comment_item.visibility = View.VISIBLE
                itemView.card_view_other_user_comment_item.visibility = View.INVISIBLE
                itemView.card_view_user_comment_item.visibility = View.VISIBLE
                itemView.tv_message_user_comment_item.text = URLDecoder.decode(comment.comment, "utf-8")
                itemView.tv_date_user_comment_item.text = comment.date
            } else {
                itemView.iv_user_comment_item.visibility = View.VISIBLE
                itemView.tv_name_comment_item.visibility = View.VISIBLE
                itemView.tv_date_other_user_comment_item.visibility = View.VISIBLE
                itemView.tv_date_user_comment_item.visibility = View.INVISIBLE
                itemView.card_view_other_user_comment_item.visibility = View.VISIBLE
                itemView.card_view_user_comment_item.visibility = View.INVISIBLE
                itemView.tv_message_other_user_comment_item.text = URLDecoder.decode(comment.comment, "utf-8")
                itemView.tv_date_other_user_comment_item.text = comment.date
                itemView.tv_name_comment_item.text = comment.author
            }

            if(idUser.equals(idOrganizer) || idUser.equals(comment.authorId)){
                itemView.setOnLongClickListener {
                    commentSelectedListener(comment.commentId)
                    true
                }
            }
        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<Commentaire>() {
        override fun areItemsTheSame(oldItem: Commentaire, newItem: Commentaire): Boolean {
            return oldItem.comment == newItem.comment
        }

        override fun areContentsTheSame(oldItem: Commentaire, newItem: Commentaire): Boolean {
            return oldItem == newItem
        }
    }
}
