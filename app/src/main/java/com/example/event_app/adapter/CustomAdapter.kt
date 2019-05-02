package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R

class CustomAdapter (private val imageIdList: Array<Int>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
    holder.iv?.setImageResource(imageIdList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.list_pic_event, parent, false)
        return ViewHolder(inflateView)
    }

    override fun getItemCount(): Int {
        return imageIdList.size
    }

    class ViewHolder (private var v:View) : RecyclerView.ViewHolder(v){
        //var tvname: TextView? = null
        internal var iv: ImageView? = v.findViewById(R.id.image_item)

    }
}