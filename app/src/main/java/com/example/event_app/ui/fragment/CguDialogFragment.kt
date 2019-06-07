package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CGUAdapter
import com.example.event_app.model.CGU
import kotlinx.android.synthetic.main.dialog_fragment_cgu.*

class CguDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_cgu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_closeDialog.setOnClickListener { dismiss() }

        val cguList = makeCGUList()

        val adapter = CGUAdapter()
        rv_cgu.layoutManager = LinearLayoutManager(context)
        rv_cgu.adapter = adapter
        val itemDecor = DividerItemDecoration(context, VERTICAL)
        rv_cgu.addItemDecoration(itemDecor)
        adapter.submitList(cguList)
    }


    private fun makeCGUList() : MutableList<CGU>{
        val list: MutableList<CGU> = mutableListOf()
        val listOfBody = context?.resources?.getStringArray(R.array.cgu_body_list)
        val listOfTitle = context?.resources?.getStringArray(R.array.cgu_title_list)
        listOfBody?.forEachIndexed { index, s ->
            listOfTitle!![index]?.let {
                list.add(CGU(it, s))
            }
        }
        return list
    }
}
