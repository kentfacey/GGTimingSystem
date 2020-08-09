package com.ggtimingsystem.run

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ggtimingsystem.R
import com.ggtimingsystem.list.ScheduledRunInfoRow
import com.ggtimingsystem.models.Run
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_available_runs.*
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class AvailableRunsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_runs)

        supportActionBar?.title = "Available Runs"

        fetchScheduledRuns()

    }

    companion object {
        const val RUN_KEY = "RUN_KEY"
    }

    private fun fetchScheduledRuns() {
        val ref = FirebaseDatabase.getInstance().getReference("/runs")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d("AvailableRuns", it.toString())
                    val run = it.getValue(Run::class.java)
                    if(run != null) {
                        adapter.add(
                            ScheduledRunInfoRow(run)
                        )
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val runItem = item as ScheduledRunInfoRow

                    val intent = Intent(view.context, RunDetailsActivity::class.java)
                    intent.putExtra(RUN_KEY, runItem.run)
                    startActivity(intent)
                    finish()

                }
                
                recyclerview_availableruns.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
