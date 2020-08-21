package com.ggtimingsystem.list

import android.util.Log
import com.ggtimingsystem.R
import com.ggtimingsystem.models.RunnersItem
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.runners_row.view.*

class RunnersRow(val runner: RunnersItem, position: Int) : Item<ViewHolder>() {
    val place = position

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val positionString = "$place."
        viewHolder.itemView.position_textview_runners_row.text = positionString
        viewHolder.itemView.username_textview_runners_row.text = runner.username
        viewHolder.itemView.distance_textview_runners_row.text = runner.distance.toString()

        Log.d("RunnersRow", "Runners row created")

    }


    override fun getLayout(): Int {

        return R.layout.runners_row
    }
}