package com.example.event_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.event_app.R
import com.obsez.android.lib.filechooser.ChooserDialog
import com.obsez.android.lib.filechooser.tool.RootFile
import kotlinx.android.synthetic.main.file_chooser_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class FolderChooserDialog(private val context: Context) {

    private var chooserDialog: ChooserDialog

    init {

        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat("yyyy")
        val colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        val folderIcon = ContextCompat.getDrawable(context,
            R.drawable.ic_folder_title)
        val filter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.black),
            PorterDuff.Mode.SRC_ATOP)
        folderIcon?.mutate()?.colorFilter = filter


        chooserDialog = ChooserDialog(context, R.style.FileChooserStyle_Dark)
            .withFilter(true, false)
            .withStartFile(Environment.getExternalStorageDirectory().toString())
            .withStringResources(
                context.getString(R.string.choose_folder_title),
                context.getString(R.string.chose_folder_ok),
                context.getString(R.string.chose_folder_cancel)
            )
            .withOptionStringResources(
                context.getString(R.string.chose_folder_new_folder),
                context.getString(R.string.chose_folder_delete),
                context.getString(R.string.chose_folder_cancel),
                context.getString(R.string.chose_folder_ok)
            )
            .withOptionIcons(
                R.drawable.ic_more_menu,
                R.drawable.ic_folder_2,
                R.drawable.trash_basket)
            .withAdapterSetter { adapter ->
                adapter.overrideGetView { file, isSelected, _, _, parent, inflater ->
                    val view = inflater.inflate(R.layout.file_chooser_item, parent, false) as ViewGroup

                    val tvName = view.file_name
                    val tvPath = view.file_path
                    val tvDate = view.file_date

                    tvName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    tvPath.setTextColor(ContextCompat.getColor(context, R.color.grey))
                    tvDate.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

                    tvDate.visibility = View.VISIBLE
                    tvName.text = file.name

                    val icon: Drawable? = folderIcon?.constantState?.newDrawable()

                    if (file.lastModified() != 0L) {
                        tvDate.text = format.format(Date(file.lastModified()))
                    } else {
                        tvDate.visibility = View.GONE
                    }

                    tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)

                    if (file !is RootFile) {
                        tvPath.text = file.path
                    } else {
                        tvPath.text = ""
                    }

                    val root = view.findViewById<View>(R.id.root)
                    if (root?.background == null) {
                        root?.setBackgroundResource(R.color.colorPrimary)
                    }
                    if (!isSelected) {
                        root?.background?.clearColorFilter()
                    } else {
                        root?.background?.colorFilter = colorFilter
                    }

                    view
                }
            }
            .enableOptions(true)
            .titleFollowsDir(true)
            .withIcon(R.drawable.ic_folder_title)

    }

    fun getDialog() : ChooserDialog {
        return chooserDialog
    }
}