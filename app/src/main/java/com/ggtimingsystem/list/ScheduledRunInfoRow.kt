package com.ggtimingsystem.list

import android.graphics.Color
import com.ggtimingsystem.R
import com.ggtimingsystem.models.Run
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.schedule_info_row.view.*

class ScheduledRunInfoRow(val run: Run): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // sets date as day month year
        viewHolder.itemView.date_textview_schedule_info_row.text = run.dateString()

        // sets miles
        viewHolder.itemView.miles_textview_schedule_info_row.text = run.distanceString()

        // sets people
        viewHolder.itemView.people_textview_schedule_info_row.text = run.totalPeopleString()

        // sets time of day
        viewHolder.itemView.time_textview_schedule_info_row.text = run.timeOfDayString()

        // TODO: get colors from resource values
        // if the run is complete set background color to red
        if(run.isComplete()) viewHolder.itemView.schedule_info_row.setBackgroundColor(Color.RED)
        // if the run is in progress set background color to blue
        if(run.inProgress()) viewHolder.itemView.schedule_info_row.setBackgroundColor(Color.BLUE)

    }


    override fun getLayout(): Int {

        return R.layout.schedule_info_row
    }
}