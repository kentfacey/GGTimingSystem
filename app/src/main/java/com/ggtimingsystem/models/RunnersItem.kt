package com.ggtimingsystem.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@Parcelize
// a userItem that is is held in a run
class RunnersItem
    (
        val userId: String = "",
        val username: String = "",
        var checkedIn: Boolean = false,
        var position: Int = 0,
        var time: String? = null,
        var distance: Double = 0.0

    ) : Parcelable {
}