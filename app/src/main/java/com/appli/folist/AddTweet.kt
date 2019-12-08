package com.appli.folist

import com.appli.folist.models.OrderStatus
import com.appli.folist.models.TimeLineModel

class AddTweet(val mDataList: ArrayList<TimeLineModel>){
    fun setDataListItems(content: String, TweetDate: String, TweetId: Int) {
        mDataList.add(0, TimeLineModel(content, TweetDate, OrderStatus.INACTIVE))
    }
}