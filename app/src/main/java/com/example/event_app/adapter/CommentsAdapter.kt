package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Commentaire
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentsAdapter(): ListAdapter<Commentaire, CommentsAdapter.CommentsViewHolder>(DiffCardCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(comment: Commentaire) {
            itemView.tv_commment.text = comment.comment
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