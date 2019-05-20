package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.EventItem
import com.example.event_app.repository.UserRepository
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_myevent.view.*

class ListEventAdapter : ListAdapter<EventItem, ListEventAdapter.EventViewHolder>(ListEventAdapter.DiffCardCallback()) {

    val eventsClickPublisher: BehaviorSubject<String> = BehaviorSubject.create()
    val organizerClickPublisher: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_myevent, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: EventItem) {
            //disposition
            itemView.tv_name_myevent_item.text = event.nameEvent
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

    class DiffCardCallback : DiffUtil.ItemCallback<EventItem>() {
        override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }

        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }
    }
}
