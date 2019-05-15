package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.CommentChoice
import com.example.event_app.model.Commentaire
import com.example.event_app.ui.fragment.CommentChoiceDialogFragment
import com.example.event_app.ui.fragment.HomeFragment
import kotlinx.android.synthetic.main.comment_item.view.*
import java.net.URLDecoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val fragmentManager: FragmentManager,
    private val idUser: String,
    private val idOrganizer: String?,
    private val commentSelectedListener: (String, CommentChoice) -> Unit,
    private val editCommentListener: (Commentaire) -> Unit
) : ListAdapter<Commentaire, CommentsAdapter.CommentsViewHolder>(DiffCardCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(comment: Commentaire) {
            if (comment.authorId.equals(idUser)) {
                itemView.iv_user_comment_item.visibility = GONE
                itemView.tv_name_comment_item.visibility = GONE
                itemView.tv_date_other_user_comment_item.visibility = INVISIBLE
                itemView.tv_date_user_comment_item.visibility = VISIBLE
                itemView.card_view_other_user_comment_item.visibility = INVISIBLE
                itemView.card_view_user_comment_item.visibility = VISIBLE
                itemView.tv_message_user_comment_item.text = URLDecoder.decode(comment.comment, "utf-8")
                itemView.tv_date_user_comment_item.text = comment.date
                itemView.et_message_user_comment_item.setText(comment.comment)
                itemView.iv_edit_comment_item.setOnClickListener {
                    itemView.group_edit_comment_item.visibility = GONE
                    itemView.tv_message_user_comment_item.visibility = VISIBLE

                    editCommentListener(makeEditedCommentFrom(comment))
                }
            } else {
                itemView.iv_user_comment_item.visibility = VISIBLE
                itemView.tv_name_comment_item.visibility = VISIBLE
                itemView.tv_date_other_user_comment_item.visibility = VISIBLE
                itemView.tv_date_user_comment_item.visibility = INVISIBLE
                itemView.card_view_other_user_comment_item.visibility = VISIBLE
                itemView.card_view_user_comment_item.visibility = INVISIBLE
                itemView.tv_message_other_user_comment_item.text = URLDecoder.decode(comment.comment, "utf-8")
                itemView.tv_date_other_user_comment_item.text = comment.date
                itemView.tv_name_comment_item.text = comment.author
            }

            if (idUser.equals(idOrganizer) || idUser.equals(comment.authorId)) {
                itemView.setOnLongClickListener {
                    val dialogFragment = CommentChoiceDialogFragment(commentChoiceListener = {
                        when (it) {
                            CommentChoice.EDIT -> {
                                itemView.group_edit_comment_item.visibility = VISIBLE
                                itemView.tv_message_user_comment_item.visibility = GONE
                            }
                            else -> commentSelectedListener(comment.commentId, it)
                        }
                    })
                    dialogFragment.show(fragmentManager, HomeFragment.TAG)
                    true
                }
            }
        }

        private fun makeEditedCommentFrom(comment: Commentaire): Commentaire {
            /** making a new object reference */
            val newComment = Commentaire(
                comment.commentId,
                comment.author,
                comment.authorId,
                "",
                comment.photoId,
                "")
            /** modify this reference */
            val date = Calendar.getInstance().time
            val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
            val newDate = df.format(date)
            newComment.date = newDate
            newComment.comment = itemView.et_message_user_comment_item.text.toString()
            return newComment
        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<Commentaire>() {
        override fun areItemsTheSame(oldItem: Commentaire, newItem: Commentaire): Boolean {
            return oldItem.commentId == newItem.commentId
                    && oldItem.comment == newItem.comment
        }

        override fun areContentsTheSame(oldItem: Commentaire, newItem: Commentaire): Boolean {
            return oldItem.comment == newItem.comment
        }
    }
}
