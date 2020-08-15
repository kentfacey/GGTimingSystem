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
        ref.orderByChild("date")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapterScheduledRuns = GroupAdapter<ViewHolder>()
                val adapterInProgressRuns = GroupAdapter<ViewHolder>()
                val adapterFinishedRuns = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d("AvailableRuns", it.toString())
                    val run = it.getValue(Run::class.java)
                    if(run != null) {
                        when {
                            run.inProgress() -> {
                                adapterInProgressRuns.add(
                                    ScheduledRunInfoRow(run)
                                )
                            }
                            run.isComplete() -> {
                                adapterFinishedRuns.add(
                                    ScheduledRunInfoRow(run)
                                )
                            }
                            else -> {
                                adapterScheduledRuns.add(
                                    ScheduledRunInfoRow(run)
                                )
                            }
                        }
                    }
                }
                adapterScheduledRuns.setOnItemClickListener { item, view ->
                    val runItem = item as ScheduledRunInfoRow

                    val intent = Intent(view.context, RunDetailsActivity::class.java)
                    intent.putExtra(RUN_KEY, runItem.run)
                    startActivity(intent)
                    finish()
                }
                adapterInProgressRuns.setOnItemClickListener { item, view ->
                    val runItem = item as ScheduledRunInfoRow

                    val intent = Intent(view.context, RunDetailsActivity::class.java)
                    intent.putExtra(RUN_KEY, runItem.run)
                    startActivity(intent)
                    finish()
                }
                adapterFinishedRuns.setOnItemClickListener { item, view ->
                    val runItem = item as ScheduledRunInfoRow

                    val intent = Intent(view.context, RunDetailsActivity::class.java)
                    intent.putExtra(RUN_KEY, runItem.run)
                    startActivity(intent)
                    finish()
                }

                scheduledRuns_recyclerview_availableRuns.adapter = adapterScheduledRuns
                inProgressRuns_recyclerview_availableRuns.adapter = adapterInProgressRuns
                finishedRuns_recyclerview_availableRuns.adapter = adapterFinishedRuns

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
