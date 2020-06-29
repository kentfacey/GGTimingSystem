package com.ggtimingsystem

import android.annotation.SuppressLint
import com.ggtimingsystem.model.Run
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.schedule_info_row.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScheduledRunInfoRow(private val run: Run): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // sets date as day month year
        val formatter = DateTimeFormatter.ofPattern("MMM d, uuuu")
        val date = LocalDateTime.parse(run.date)
        viewHolder.itemView.date_textview_schedule_info_row.text = date.format(formatter)

        // sets miles
        var milesString = run.miles.toString() + " miles"
        viewHolder.itemView.miles_textview_schedule_info_row.text = milesString

        // sets people
        var peopleString = run.currentPeople.toString() + "/" + run.maxPeople
        viewHolder.itemView.people_textview_schedule_info_row.text = peopleString

        // sets time of day
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        viewHolder.itemView.time_textview_schedule_info_row.text = date.format(timeFormatter)


    }

    override fun getLayout(): Int {

        return R.layout.schedule_info_row
    }
}