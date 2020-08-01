package com.ggtimingsystem.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Run
    (
        val uid: String = "",
        val date: String = "",
        val distance: Double = 0.0,
        var currentPeople: Int = 0,
        val maxPeople: Int = 0,
        var numCheckedIn: Int = 0,
        var complete: Boolean = false,
        var users: MutableMap<String, RunUserItem> = mutableMapOf(),
        val timeOut: Long = 2
    ) : Parcelable{
}
