package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.CGU
import kotlinx.android.synthetic.main.cgu_item.view.*

class CGUAdapter : ListAdapter<CGU, CGUAdapter.ViewHolder>(DiffUserscallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindUser(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.cgu_item, parent, false)
        return ViewHolder(inflateView)
    }

    class DiffUserscallback : DiffUtil.ItemCallback<CGU>() {
        override fun areItemsTheSame(oldItem: CGU, newItem: CGU): Boolean {
            return oldItem.body == newItem.body
        }

        override fun areContentsTheSame(oldItem: CGU, newItem: CGU): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class ViewHolder(private var v: View) :
        RecyclerView.ViewHolder(v) {

        fun bindUser(cgu: CGU) {
            v.tv_title_cgu.text = cgu.title
            v.tv_body_cgu.text = cgu.body
        }
    }
}
