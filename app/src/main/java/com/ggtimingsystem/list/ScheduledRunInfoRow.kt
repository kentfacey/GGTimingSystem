package com.ggtimingsystem.list

import com.ggtimingsystem.R
import com.ggtimingsystem.models.Run
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_run_details.*
import kotlinx.android.synthetic.main.schedule_info_row.view.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ScheduledRunInfoRow(val run: Run): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // sets date as day month year
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val date = ZonedDateTime.parse(run.date)
        val localZoneDate = date.withZoneSameInstant(ZoneId.of("America/New_York"))
        viewHolder.itemView.date_textview_schedule_info_row.text = localZoneDate.format(formatter)

        // sets miles
        var milesString = run.distance.toString() + " km"
        viewHolder.itemView.miles_textview_schedule_info_row.text = milesString

        // sets people
        var peopleString = run.currentPeople.toString() + "/" + run.maxPeople
        viewHolder.itemView.people_textview_schedule_info_row.text = peopleString

        // sets time of day
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        viewHolder.itemView.time_textview_schedule_info_row.text = localZoneDate.format(timeFormatter)

    }

    override fun getLayout(): Int {

        return R.layout.schedule_info_row
    }
}