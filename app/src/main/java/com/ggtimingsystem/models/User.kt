package com.ggtimingsystem.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User
    (
        val uid: String,
        var username:String,
        var profileImageUrl: String,
        var runs: MutableMap<String, UserRunItem> = mutableMapOf(),
        var isRunning: Boolean = false
    ) : Parcelable {
}