package com.example.event_app.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.event_app.R
import com.example.event_app.ui.fragment.DetailEventFragment
import kotlinx.android.synthetic.main.list_pic_event.*

class CustomAdapter (private val context: Context, private val imageIdList: Array<Int>)
    : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.list_pic_event, null, true)

            //holder.tvname = convertView!!.findViewById(R.id.) as TextView
            holder.iv = convertView.findViewById(R.id.image_item) as ImageView

            convertView.tag = holder
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = convertView.tag as ViewHolder
        }

        //holder.tvname!!.setText(imageIdList[position].getNames())
        holder.iv!!.setImageResource(imageIdList[position])

        return convertView
    }
    override fun getItem(p0: Int): Any {
        return imageIdList.get(p0)
    }
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }
    override fun getCount(): Int {
        return imageIdList.size
    }
    private inner class ViewHolder {

        var tvname: TextView? = null
        internal var iv: ImageView? = null

    }
}