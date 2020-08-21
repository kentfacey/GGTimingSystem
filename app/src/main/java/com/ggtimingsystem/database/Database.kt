package com.ggtimingsystem.database

import android.util.Log
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.models.RunnersItem
import com.ggtimingsystem.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

// handles database saves
open class Database {

    private val userId = FirebaseAuth.getInstance().uid ?: ""
    private val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId")
    private var username = getUserName(userId)

    // test create run in saveUserToFirebaseDatabase
    fun createRun() {
        val uid = UUID.randomUUID().toString()
        val ref = FirebaseDatabase.getInstance().getReference("runs/$uid")
        var date = ZonedDateTime.now(ZoneId.of("UTC"))
        //date = date.plusMinutes(20)
        val dateString = date.toString()
        val run = Run(uid, dateString, 5.0, 0, 150)
        ref.setValue(run)
    }

    // save user distance in a run
    fun saveDistance(distance: Double, runId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/runners/$userId")
        val distanceListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    ref.child("distance").setValue(distance)
                }
            }

        }

        ref.addListenerForSingleValueEvent(distanceListener)

    }

    // returns the users distance in a run
    fun getDistance(runId: String): Double {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/runners/$userId/distance")
        var distance = 0.0
        val distanceListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val num = snapshot.value as Number
                    distance = num.toDouble()
                    Log.d("Database", "Double read $distance")
                }
            }

        }

        ref.addListenerForSingleValueEvent(distanceListener)
        return distance
    }

    // save the users time in the run at the end of the run
    fun saveTime(time: ZonedDateTime, runId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/runners/$userId")
        val timeListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    ref.child("time").setValue(time.toString())
                }
            }

        }

        ref.addListenerForSingleValueEvent(timeListener)
    }

    // save check-in status in a run
    fun saveCheckIn(runId: String, status: Boolean) {
        val ref = FirebaseDatabase.getInstance().getReference("/runs/$runId/runners/$userId/checkedIn")
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
                    currentRun.runners[userId] = RunnersItem(userId, username)
                    Log.d("Database", "username = $username")

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
                currentRun.runners.remove(userId)

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

    // returns the user object
    fun getUserName(userId: String): String{
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId/username")
        var name = ""


        val usernameListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    Log.d("Database", "Snapshot username: ${snapshot.value}")
                    name = snapshot.value.toString()
                    username = snapshot.value.toString()
                }
            }

        }

        ref.addListenerForSingleValueEvent(usernameListener)
        Log.d("Database", "Username found: $name")
        return name
    }
}