package com.ggtimingsystem.run

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ggtimingsystem.R
import com.ggtimingsystem.database.Database
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.run.active.ActiveRunActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_run_details.*

class RunDetailsActivity : AppCompatActivity() {

    private val database = Database()
    companion object{
        const val RUN_KEY = "RUN_KEY"
        lateinit var run: Run
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_details)

        Log.d("RunDetailsActivity","Run Details Activity Started")
        supportActionBar?.title = "Run Details"

        run = intent.getParcelableExtra<Run>(AvailableRunsActivity.RUN_KEY)

        setUpLayout()
        Log.d("RunDetailsActivity","Layout set up")

        setUpButtons()
        Log.d("RunDetailsActivity","Buttons set up")


    }

    // sets up layout for run details
    private fun setUpLayout() {
        // sets date as day month year
        date_textview_runDetails.text = run.dateString()

        // sets distance in kilometers
        distance_textview_runDetails.text = run.distanceString()

        // sets people
        setPeople()

        // sets time of day
        time_textview_runDetails.text = run.timeOfDayString()

        // start warning
        startWarning_textview_runDetails
    }

    // sets the people
    private fun setPeople() {
        // listens to see if current people changes
        val refCurrentPeople = FirebaseDatabase.getInstance().getReference("/runs/${run.uid}/currentPeople")
        refCurrentPeople.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if(!snapshot.exists()){
                    return
                }
                // if the current people changes
                val snapshotValue = snapshot.getValue(Int::class.java)
                Log.d("RunDetailsActivity", "Snapshot: $snapshotValue")
                val snapshotString = snapshotValue.toString()

                val peopleString = snapshotString + "/" + run.maxPeople

                people_textview_runDetails.text = peopleString

                Log.d("RunDetailsActivity", "Current people: " + run.currentPeople)
            }
        })
        Log.d("RunDetailsActivity","People set up")
    }

    // sets up buttons
    private fun setUpButtons() {
        val userId = FirebaseAuth.getInstance().uid

        // sets up button visibility
        userInRun(userId)

        joinRun_button_runDetails.setOnClickListener {
            Log.d("RunDetailsActivity","join run button clicked")
            database.addToRun(run.uid)
        }

        leave_button_runDetails.setOnClickListener {
            Log.d("RunDetailsActivity","leave button clicked")
            database.leaveRun(run.uid)
        }

        checkIn_button_runDetails.setOnClickListener {
            Log.d("RunDetailsActivity","checkIn button clicked")
            checkIn()
        }


    }

    private fun checkIn() {

        database.saveCheckIn(run.uid, true)


        val intent = Intent(this, ActiveRunActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(RunDetailsActivity.RUN_KEY, run)
        startActivity(intent)


    }

    // returns true if the user is in the race, sets visibility of control buttons
    private fun userInRun(userId: String?) {
        // location of user in database
        val ref = FirebaseDatabase.getInstance().getReference("/runs/${run.uid}/runners/$userId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                // if the users id is in the runs set of runners
                when {
                    run.isComplete() -> {
                        viewResults_button_runDetails.visibility = View.VISIBLE
                        joinRun_button_runDetails.visibility = View.GONE
                        leave_button_runDetails.visibility = View.GONE
                        checkIn_button_runDetails.visibility = View.GONE
                    }
                    // if the user is in the run
                    snapshot.exists() -> {
                        joinRun_button_runDetails.visibility = View.GONE
                        leave_button_runDetails.visibility = View.VISIBLE
                        checkIn_button_runDetails.visibility = View.VISIBLE
                    }
                    // if the user is not in the run
                    else -> {
                        joinRun_button_runDetails.visibility = View.VISIBLE
                        leave_button_runDetails.visibility = View.GONE
                        checkIn_button_runDetails.visibility = View.GONE
                    }
                }
            }
        })
    }



}

