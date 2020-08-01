package com.ggtimingsystem.database

import android.content.Intent
import android.util.Log
import com.ggtimingsystem.list.ScheduledRunInfoRow
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.models.RunUserItem
import com.ggtimingsystem.models.User
import com.ggtimingsystem.run.AvailableRunsActivity
import com.ggtimingsystem.run.RunDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_available_runs.*
import kotlinx.android.synthetic.main.activity_register.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

// handles database saves
open class Database {

    private val userId = FirebaseAuth.getInstance().uid ?: ""
    private val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId")

    // test create run in saveUserToFirebaseDatabase
    fun createRun() {
        val uid = UUID.randomUUID().toString()
        val ref = FirebaseDatabase.getInstance().getReference("runs/$uid")
        val date = ZonedDateTime.now(ZoneId.of("UTC")).toString()
        val run = Run(uid, date, 5.0, 100, 150)
        ref.setValue(run)
    }

    // save user distance in a run
    fun saveDistance(distance: Double, runId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/users/$userId/distance")
        ref.setValue(distance)
    }

    // returns the users distance in a run
    fun getDistance(distance: Double, runId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/users/$userId/distance")
        
    }

    // save the users time in the run at the end of the run
    fun saveTime(time: ZonedDateTime, runId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/users/$userId/time")
        ref.setValue(time.toString())
    }

    // save check-in status in a run
    fun saveCheckIn(runId: String, status: Boolean) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/users/$userId/checkedIn")
        ref.setValue(status)
        Log.d("RunDetailsActivity","user checked in")
    }


    // save a user to the database
    fun saveUser(uid: String, profileImageUrl: String, username: String) {

        val user = User(uid, username, profileImageUrl)
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user to the Firebase Database")
            }
            .addOnFailureListener {
                // failure
            }
    }

    // adds user to the run
    fun addToRun(runId: String) {

        val runRef = FirebaseDatabase.getInstance().getReference("/runs/$runId")

        // runs if there is an open slot to join
        runRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentRun = currentData.getValue(Run::class.java) ?: return Transaction.success(currentData)
                if(currentRun.currentPeople < currentRun.maxPeople){

                    // put user into run
                    currentRun.users[userId!!] = RunUserItem(userId)

                    // increase people value
                    currentRun.currentPeople = currentRun.currentPeople + 1

                    val runUserRef = FirebaseDatabase.getInstance().getReference("/users/$userId/runs/${currentRun.uid}")
                    runUserRef.setValue(currentRun.uid)

                    Log.d("RunDetailsActivity", "Add run user ref")

                    currentData.value = currentRun

                }
                else{
                    // aborts if there is not enough slots to join
                    Log.d("RunDetailsActivity", "Failed to Join Run: Not enough slots!")
                    return Transaction.abort()
                }

                return Transaction.success(currentData)
            }
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d("RunDetailsActivity", "postTransaction:onComplete:$error")
            }
        })

    }

    // takes user out of the run
    fun leaveRun(runId: String) {

        val runRef = FirebaseDatabase.getInstance().getReference("/runs/$runId")

        runRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentRun = currentData.getValue(Run::class.java) ?: return Transaction.success(currentData)

                // remove user from run
                currentRun.users.remove(userId)

                // decrease people value
                currentRun.currentPeople = currentRun.currentPeople - 1

                val runUserRef = FirebaseDatabase.getInstance().getReference("/users/$userId/runs/${currentRun.uid}")
                runUserRef.removeValue()

                Log.d("RunDetailsActivity", "Delete run user ref")


                currentData.value = currentRun

                return Transaction.success(currentData)
            }
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d("RunDetailsActivity", "postTransaction:onComplete:$error")
            }
        })
    }
}