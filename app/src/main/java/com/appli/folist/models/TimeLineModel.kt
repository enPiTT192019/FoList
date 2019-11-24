package com.appli.folist.models

import android.os.Parcelable
import com.appli.folist.models.OrderStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
class TimeLineModel(
        var message: String,
        var date: String,
        var status: OrderStatus
) : Parcelable
