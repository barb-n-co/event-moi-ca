package com.example.event_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.User
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_participant.view.*

class ListParticipantsAdapter(
    private val idOrga: String,
    private val isNotAnOrga: Boolean
) :
    ListAdapter<User, ListParticipantsAdapter.ViewHolder>(DiffUserscallback()) {
    val userClickPublisher: PublishSubject<String> = PublishSubject.create()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindUser(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.item_participant, parent, false)
        return ViewHolder(inflateView, userClickPublisher)
    }

    class DiffUserscallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class ViewHolder(private var v: View, private val userClickPublisher: PublishSubject<String>) :
        RecyclerView.ViewHolder(v) {

        fun bindUser(user: User) {
            v.tv_participants.text = user.name
            if (user.id == idOrga || !isNotAnOrga) {
                v.iv_remove.visibility = View.GONE
            }
            v.iv_remove.setOnClickListener {
                Log.d("ListPartFragm", "in adapter")
                user.id?.let {
                    userClickPublisher.onNext(user.id!!)
                }
            }
        }
    }
}
