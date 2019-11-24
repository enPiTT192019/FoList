package com.appli.folist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_tweet.*
import java.text.SimpleDateFormat
import java.util.*

class tweetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)

        button.setOnClickListener {
            if(tweetText.text.toString().length != 0) {
                var tweetId = 1
                var addngo = AddTweet(mDataList)
                val date = Date()
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                addngo.setDataListItems(tweetText.text.toString(), format.format(date), tweetId++)
//                user_button.setImageResource(R.drawable.inkya)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
