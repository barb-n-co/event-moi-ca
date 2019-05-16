package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Event
import com.example.event_app.repository.UserRepository
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_myevent.view.*

class ListEventAdapter : ListAdapter<Event, ListEventAdapter.EventViewHolder>(ListEventAdapter.DiffCardCallback()) {

    val eventsClickPublisher: PublishSubject<String> = PublishSubject.create()
    val organizerClickPublisher: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_myevent, parent, false)
        return EventViewHolder(view, eventsClickPublisher)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View, private val eventsClickPublisher: PublishSubject<String>) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event) {
            //disposition
            itemView.tv_name_myevent_item.text = event.name
            itemView.tv_startDate_myevent_item.text = "Du ${event.dateStart} au  ${event.dateEnd}"

            UserRepository.currentUser.value?.let {
                if (it.id == event.idOrganizer) {
                    itemView.b_refuse_myevent_item.visibility = View.VISIBLE
                    itemView.b_refuse_myevent_item.setOnClickListener {
                        organizerClickPublisher.onNext(event.idOrganizer)
                    }
                } else {
                    itemView.b_refuse_myevent_item.visibility = View.INVISIBLE
                }
            }

            bindPositionClick(event.idEvent)
        }

        private fun bindPositionClick(idEvent: String) {
            itemView.setOnClickListener {
                eventsClickPublisher.onNext(idEvent)
            }
        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }
    }
}
