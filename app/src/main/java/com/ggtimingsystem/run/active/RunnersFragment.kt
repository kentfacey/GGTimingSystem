package com.ggtimingsystem.run.active

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggtimingsystem.R
import com.ggtimingsystem.list.RunnersRow
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.models.RunnersItem
import com.ggtimingsystem.run.RunDetailsActivity
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_runners.*

class RunnersFragment : Fragment() {

    lateinit var run: String
    private lateinit var ref: DatabaseReference
    var valueEvent = object: ValueEventListener {
        override fun onDataChange(p0: DataSnapshot) {
            val adapterRunners = GroupAdapter<ViewHolder>()
            var runnersList = mutableListOf<RunnersItem>()

            var position = 1
            p0.children.forEach{
                val runner = it.getValue(RunnersItem::class.java)
                if (runner != null) {
                    runnersList.add(runner)
                }
            }

            runnersList.sortByDescending{it.distance}

            val iterator = runnersList.listIterator()
            for(item in iterator) {
                adapterRunners.add(RunnersRow(item, position))
                position += 1
            }

            runnersList_recyclerview_RunnersFragment.adapter = adapterRunners
        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    companion object {
        fun newInstance(): RunnersFragment =
            RunnersFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        run = this.arguments?.getString(ActiveRunActivity.RUN_KEY).toString()
        return inflater.inflate(R.layout.fragment_runners, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RunnerFragment", "Run id: $run")
        fetchScheduledRuns()
    }


    // gets all the runs from the database and puts them in the layout
    private fun fetchScheduledRuns() {
        ref = FirebaseDatabase.getInstance().getReference("/runs/$run/runners")
        ref.orderByChild("distance")
        Log.d("RunnersFragment", "fetchScheduledRuns")

        ref.addValueEventListener(valueEvent)
    }

    override fun onStop() {
        super.onStop()
        ref.removeEventListener(valueEvent)
    }
}