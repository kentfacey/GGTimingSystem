package com.ggtimingsystem.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Parcelize
class Run
    (
    val uid: String = "",
    val date: String = "",
    val distance: Double = 0.0,
    var currentPeople: Int = 0,
    val maxPeople: Int = 0,
    var numCheckedIn: Int = 0,
    var runners: MutableMap<String, RunnersItem> = mutableMapOf(),
    val timeOut: Long = 120 // in minutes
    ) : Parcelable{


    // returns true if the run is complete
    fun isComplete(): Boolean {
        val durationBetween = Duration.between(ZonedDateTime.parse(date), ZonedDateTime.now(ZoneId.of("UTC")))
        val diff = durationBetween.toMinutes()

        return diff > timeOut
    }

    // returns true if the run is in progress
    fun inProgress(): Boolean {
        val durationBetween = Duration.between(ZonedDateTime.parse(date), ZonedDateTime.now(ZoneId.of("UTC")))
        val diff = durationBetween.toMinutes()

        return diff in 0 until timeOut
    }

    // returns date as a string
    fun dateString(): String {
        // sets date as day month year
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val date = ZonedDateTime.parse(date)
        val localZoneDate = date.withZoneSameInstant(ZoneId.of("America/New_York"))
        return localZoneDate.format(formatter)
    }

    fun timeOfDayString(): String {
        // sets time of day
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val date = ZonedDateTime.parse(date)
        val localZoneDate = date.withZoneSameInstant(ZoneId.of("America/New_York"))
        return localZoneDate.format(timeFormatter)
    }

    // returns miles as a string
    fun distanceString(): String {
        return "$distance km"
    }

    // returns people in race as a string
    fun totalPeopleString(): String {
        return "$currentPeople/$maxPeople"
    }
}
