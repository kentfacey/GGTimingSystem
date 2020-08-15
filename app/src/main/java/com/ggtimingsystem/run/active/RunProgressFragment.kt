package com.ggtimingsystem.run.active

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.ggtimingsystem.R
import com.ggtimingsystem.database.Database
import com.ggtimingsystem.main.HomeFragment
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.run.AvailableRunsActivity
import com.ggtimingsystem.run.RunDetailsActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_run_progress.*
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

class RunProgressFragment : Fragment() {

    companion object {
        fun newInstance(): RunProgressFragment =
            RunProgressFragment()
        const val REQUEST_FINE_LOCATION = 99
        const val UPDATE_INTERVAL = 5 * 1000 // 5 seconds
            .toLong()
        const val FASTEST_INTERVAL = 2 * 1000 // 2 seconds
            .toLong()
        const val EARTH_RADIUS_KM = 6371 // volumetric mean radius in km  // 3956 in miles
    }

    private var database = Database()

    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // where the loop happens
            onLocationChanged(locationResult.lastLocation)
        }
    }

    private var latitude : Double? = null
    private var longitude : Double? = null

    private var distance : Double = 0.0

    private lateinit var run: Run
    private lateinit var startTime : ZonedDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!);
        run = activity!!.intent.getParcelableExtra<Run>(RunDetailsActivity.RUN_KEY)
        startTime = ZonedDateTime.parse(run.date)

        getDistance()

        setUpButtons()

        setUpLayout()

        // if the start time is before now, start the race timer
        if(startTime.isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) || startTime.isEqual(ZonedDateTime.now(ZoneId.of("UTC")))){
            startLocationUpdates()
        }
    }


    // creates the functions for the buttons in the activity
    private fun setUpButtons() {
        leaveRun_button_active_run.setOnClickListener {
            val intent = Intent(activity, RunDetailsActivity::class.java)
            intent.putExtra(AvailableRunsActivity.RUN_KEY, run)
            startActivity(intent)
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            activity!!.finish()
        }
    }


    // retrieves the current distance from the database
    private fun getDistance(){
        val userId = FirebaseAuth.getInstance().uid ?: ""
        // sets the distance to the current distance run in the database, important if they leave the run and come back
        val ref = FirebaseDatabase.getInstance().getReference("/runs/${run.uid}/users/$userId/distance")
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

        //distance = database.getDistance(run.uid)
        Log.d("ActiveRunActivity", "Distance = $distance")

        /**
         * //TODO: get the distance from the database class (and have this thread wait for the function to complete) instead of on the current thread
        distance = database.getDistance(run.uid)
         */
    }

    // sets up the layout such as the progress bar
    private fun setUpLayout() {
        var twoDecimalDistance = displayDistance()
        endDistance_active_run.text = run.distanceString()
        progressBar_activity_active_run.max = (run.distance * 100).toInt() // because progress bar does not have decimals it is multiplied by a factor of 100

        // sets the progress bar to the current distance run
        progressBar_activity_active_run.progress = (twoDecimalDistance * 100).toInt()
        distance_textview_active_run.text = twoDecimalDistance.toString()

    }

    // Start location updates
    private fun startLocationUpdates() {
        // Create location request
        this.mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.interval =
            UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval =
            FASTEST_INTERVAL

        // Create Location settings request
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest: LocationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity!!)
        settingsClient.checkLocationSettings(locationSettingsRequest)
        checkLocationPermissions()
    } // start location updates


    // checks for permissions before trying to get location updates
    private fun checkLocationPermissions(): Boolean {

        var permissionGranted = true
        // if the app doesn't have these permissions, ask for them
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // request permissions such as location
            requestPermissions()
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            permissionGranted = false
            checkLocationPermissions()
        }
        if (permissionGranted) {
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }
        return permissionGranted
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FINE_LOCATION
        )
    }

    // what happens when the location changes
    fun onLocationChanged(location: Location) {

        calculateDistance(location)
        updateLayout()

    }

    // changes values on layout such as distance and time
    private fun updateLayout() {

        // distance to be displayed in layout
        var twoDecimalDistance = displayDistance()

        // if the distance ran is greater than the runs distance, you finished the race
        if(distance >= run.distance) {
            database.saveTime(ZonedDateTime.now(ZoneId.of("UTC")), run.uid)
            // stop location updates
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)

            twoDecimalDistance = run.distance
            distance = run.distance
        }

        database.saveDistance(distance, run.uid)

        distance_textview_active_run.text = twoDecimalDistance.toString()

        progressBar_activity_active_run.progress = (twoDecimalDistance*100).toInt() // distance times 100 because there is not decimal in the progress bar

        time_textview_active_run.text = timeDifferenceMinutes().toString()
    }

    // distance to be displayed in layout
    private fun displayDistance(): Double {
        return (distance *100).toInt()/100.0
    }
    // calculates the difference in the current time to the start time of the race and returns the minutes
    private fun timeDifferenceMinutes() : Double {

        var currentTime = ZonedDateTime.now(ZoneId.of("UTC"))
        var timeDifference = Duration.between(startTime, currentTime)
        var seconds = timeDifference.seconds
        var minutes = (seconds/60.0 * 100.0).toInt()/100.0  // gets minutes to the 2 decimal place

        return minutes
    }

    // calculates the distance between the previous location and the current location
    private fun calculateDistance(location: Location) {
        // new location has now been determined
        var newLat = location.latitude
        var newLong = location.longitude

        // if values are null then set the first values
        if(latitude == null || longitude == null) {
            latitude = location.latitude
            longitude = location.longitude
        }
        // same coordinate
        else if(newLat == latitude && newLong == longitude){
            // do nothing
        }
        // calculate the distance between the old point and the new point
        else {
            haversine(newLat, newLong)
        }
    }

    // function to calculate distance between two points
    private fun haversine(newLat : Double, newLong : Double){
        var lat1 = Math.toRadians(latitude!!) // in radians
        var lat2 = Math.toRadians(newLat) // in radians

        var dlat = (lat2 - lat1) // difference in radians
        var dlong = Math.toRadians(newLong - longitude!!)// difference in radians

        var a = sin(dlat / 2).pow(2) +
                cos(lat1) * cos(lat2) *
                sin(dlong/2).pow(2)

        var c = 2 * asin(sqrt(a))

        var d = EARTH_RADIUS_KM * c // in km

        Log.d("ActiveRunActivity", "Change In distance: $d")

        distance += d
        Log.d("ActiveRunActivity", "Distance: $distance")

        latitude = newLat
        Log.d("ActiveRunActivity", "Latitude: $latitude")

        longitude = newLong
        Log.d("ActiveRunActivity", "Longitude: $longitude")

    }
}