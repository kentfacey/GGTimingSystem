package com.ggtimingsystem.run

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ggtimingsystem.R
import com.ggtimingsystem.main.MainActivity
import com.ggtimingsystem.models.Run
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_run_details.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.Result.Companion.failure

class RunDetailsActivity : AppCompatActivity() {
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
        val formatter = DateTimeFormatter.ofPattern("MMM d, uuuu")
        val date = LocalDateTime.parse(run.date)
        date_textview_runDetails.text = date.format(formatter)

        // sets miles
        val milesString = run.miles.toString() + " miles"
        miles_textview_runDetails.text = milesString

        // sets people
        setPeople(run)

        // sets time of day
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        time_textview_runDetails.text = date.format(timeFormatter)

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
                // if the current people changes
                var peopleString = refCurrentPeople.toString() + "/" + run.maxPeople
                people_textview_runDetails.text = peopleString

                run.currentPeople = refCurrentPeople.toString().toInt()

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
            addToRun(userId, run)
        }
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
                if(snapshot.exists()){
                    isInRun = true
                    joinRun_button_runDetails.visibility = View.GONE
                    leave_button_runDetails.visibility = View.VISIBLE
                    checkIn_button_runDetails.visibility = View.VISIBLE
                }
                else{
                    isInRun = false
                    joinRun_button_runDetails.visibility = View.VISIBLE
                    leave_button_runDetails.visibility = View.GONE
                    checkIn_button_runDetails.visibility = View.GONE
                }
            }
        })

        return isInRun
    }

    // adds user to the run
    private fun addToRun(userId: String?, run: Run) {
        if(userInRun(userId, run.uid)) {
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("/runs/${run.uid}/users/$userId")
        val runRef = FirebaseDatabase.getInstance().getReference("/runs/${run.uid}")

        // runs if there is an open slot to join
        runRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentRun = currentData.getValue(Run::class.java) ?: return Transaction.success(currentData)
                if(currentRun.currentPeople < currentRun.maxPeople){
                    currentRun.currentPeople = currentRun.currentPeople + 1
                    currentRun.userIds.add(userId!!)
                }
                else{
                    // aborts if there is not enough slots to join
                    Toast.makeText( this@RunDetailsActivity, "Failed to Join Run: Not enough slots!", Toast.LENGTH_SHORT).show()
                    return Transaction.abort()
                }
                currentData.value = currentRun

                return Transaction.success(currentData)
            }
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d("RunDetailsActivity","postTransaction:onComplete:" + error!!)
            }
        })




    }

}
