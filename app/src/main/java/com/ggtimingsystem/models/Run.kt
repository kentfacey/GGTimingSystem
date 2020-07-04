package com.ggtimingsystem.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Run(val uid: String = "", val date: String = "", val miles: Int = 0, var currentPeople: Int = 0, val maxPeople: Int = 0, var complete: Boolean = false, var userIds: MutableList<String> = mutableListOf()) : Parcelable{
}
