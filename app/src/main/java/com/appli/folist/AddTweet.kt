package com.appli.folist

import com.appli.folist.models.OrderStatus
import com.appli.folist.models.TimeLineModel
import com.google.firebase.database.FirebaseDatabase

class AddTweet(val mDataList: ArrayList<TimeLineModel>, val usersName: String?){
    fun setDataListItems(content: String, TweetDate: String, TweetId: Int) {
        mDataList.add(0, TimeLineModel(content, TweetDate, OrderStatus.INACTIVE))
    }

    fun postUpload(
        post: String = "", shareTreeId: String? = ""
    ){
        val ref = FirebaseDatabase.getInstance().getReference("posts")
        val newref = ref.push()
        newref.child("post").setValue(post)
        newref.child("userName").setValue(usersName)
        newref.child("sharetreeId").setValue(shareTreeId)
    }
}