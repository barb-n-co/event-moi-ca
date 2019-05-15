package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.CommentChoice
import kotlinx.android.synthetic.main.item_comment_choice.view.*

class CommentChoiceAdapter(private val commentChoiceSelectedListener: (CommentChoice) -> Unit) :
    ListAdapter<CommentChoice, CommentChoiceAdapter.CommentChoiceViewHolder>(DiffCardCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentChoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_choice, parent, false)
        return CommentChoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentChoiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(choice: CommentChoice) {
            itemView.tv_item_comment_choice.text = choice.enumToString(itemView.context)
            itemView.setOnClickListener {
                commentChoiceSelectedListener(choice)
            }
        }
    }
}

class DiffCardCallback : DiffUtil.ItemCallback<CommentChoice>() {
    override fun areItemsTheSame(oldItem: CommentChoice, newItem: CommentChoice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CommentChoice, newItem: CommentChoice): Boolean {
        return oldItem == newItem
    }
}

