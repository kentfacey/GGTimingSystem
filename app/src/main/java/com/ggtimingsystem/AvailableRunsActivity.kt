package com.ggtimingsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ggtimingsystem.model.Run
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

        val adapter = GroupAdapter<ViewHolder>()
        recyclerview_availableruns.adapter = adapter

        fetchScheduledRuns()

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
                        adapter.add(ScheduledRunInfoRow(run))
                    }
                }
                recyclerview_availableruns.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
