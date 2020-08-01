package com.ggtimingsystem.run

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ggtimingsystem.R
import com.ggtimingsystem.database.Database
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.models.RunUserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_run_details.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RunDetailsActivity : AppCompatActivity() {

    private val database = Database()
    companion object{
        const val RUN_KEY = "RUN_KEY"
        var complete = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_details)

        Log.d("RunDetailsActivity","Run Details Activity Started")
        supportActionBar?.title = "Run Details"

        val run = intent.getParcelableExtra<Run>(AvailableRunsActivity.RUN_KEY)

        setUpLayout(run)
        Log.d("RunDetailsActivity","Layout set up")

        setUpButtons(run)
        Log.d("RunDetailsActivity","Buttons set up")


    }

    // sets up layout for run details
    private fun setUpLayout(run: Run) {
        // sets date as day month year
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val date = ZonedDateTime.parse(run.date)
        val localZoneDate = date.withZoneSameInstant(ZoneId.of("America/New_York"))
        date_textview_runDetails.text = localZoneDate.format(formatter)

        // sets distance in kilometers
        val distanceString = run.distance.toString() + " km"
        distance_textview_runDetails.text = distanceString

        // sets people
        setPeople(run)

        // sets time of day
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        time_textview_runDetails.text = localZoneDate.format(timeFormatter)

        // start warning
        startWarning_textview_runDetails
    }

    // sets the people
    private fun setPeople(run: Run) {
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
    private fun setUpButtons(run: Run) {
        val userId = FirebaseAuth.getInstance().uid

        // sets up button visibility
        userInRun(userId, run.uid)

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
            checkIn(run)
        }


    }

    private fun checkIn(run: Run) {

        database.saveCheckIn(run.uid, true)


        val intent = Intent(this, ActiveRunActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(RunDetailsActivity.RUN_KEY, run)
        startActivity(intent)


    }

    // returns true if the user is in the race, sets visibility of control buttons
    private fun userInRun(userId: String?, runId: String?) : Boolean {
        // location of user in database
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/users/$userId")

        var isInRun = false

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                // if the users id is in the runs set of users
                when {
                    complete -> {
                        viewResults_button_runDetails.visibility = View.VISIBLE
                        joinRun_button_runDetails.visibility = View.GONE
                        leave_button_runDetails.visibility = View.GONE
                        checkIn_button_runDetails.visibility = View.GONE
                    }
                    snapshot.exists() -> {
                        isInRun = true
                        joinRun_button_runDetails.visibility = View.GONE
                        leave_button_runDetails.visibility = View.VISIBLE
                        checkIn_button_runDetails.visibility = View.VISIBLE
                    }
                    else -> {
                        isInRun = false
                        joinRun_button_runDetails.visibility = View.VISIBLE
                        leave_button_runDetails.visibility = View.GONE
                        checkIn_button_runDetails.visibility = View.GONE
                    }
                }
            }
        })

        return isInRun
    }



}

