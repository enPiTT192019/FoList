package com.appli.folist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appli.folist.utils.usersName
import kotlinx.android.synthetic.main.activity_tweet.*
import java.text.SimpleDateFormat
import java.util.*

class tweetActivity : AppCompatActivity() {

    val shareTreeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)
        val inflater: LayoutInflater = LayoutInflater.from(this)
        val seedTitle: String? = intent.getStringExtra("SEEDTITLE")

        button.setOnClickListener {
            if(tweetText.text.toString().length != 0) {
                var tweetId = 1
                var addngo = AddTweet(mDataList, usersName)
                val date = Date()
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                //timelineリスト(mDataList)にPostを追加
                //鯖側はまだ
                addngo.setDataListItems(tweetText.text.toString(), format.format(date), tweetId++)
                addngo.postUpload(tweetText.text.toString(), seedTitle)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}