package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.CommentChoiceAdapter
import com.example.event_app.model.CommentChoice
import kotlinx.android.synthetic.main.dialog_fragment_comment_choice.*

class CommentChoiceDialogFragment(
    organizer: Boolean,
    alreadyLike: Boolean,
    commentAuthor: Boolean,
    private val commentChoiceListener: (CommentChoice) -> Unit
) : DialogFragment() {

    val likeState = if (alreadyLike) CommentChoice.DISLIKE else CommentChoice.LIKE

    private val listChoices: List<CommentChoice> = if (commentAuthor) {
        listOf(CommentChoice.DELETE, CommentChoice.EDIT, likeState)
    } else if (organizer) {
        listOf(CommentChoice.DELETE, likeState)
    } else {
        listOf(CommentChoice.REPORT, likeState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_comment_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CommentChoiceAdapter(commentChoiceSelectedListener = {
            commentChoiceListener(it)
            dismiss()
        })
        val mLayoutManager = LinearLayoutManager(context)

        rv_comment_choice_dialog_fragment.layoutManager = mLayoutManager
        rv_comment_choice_dialog_fragment.itemAnimator = DefaultItemAnimator()
        rv_comment_choice_dialog_fragment.adapter = adapter
        adapter.submitList(listChoices)
    }
}
