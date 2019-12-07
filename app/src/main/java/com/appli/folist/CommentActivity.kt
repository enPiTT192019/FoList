package com.appli.folist

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val key: String? = intent.getStringExtra("KEY")
        Log.d("COMMENT",key?:"null")

        val ref = FirebaseDatabase.getInstance().getReference("comments").child(key!!)

        commentButton.setOnClickListener {
            val str=commentEditor.text.toString()
            if(!str.isNullOrBlank()){
                val cref = ref.push().setValue(Comment(FirebaseAuth.getInstance().currentUser?.uid?:"unknown user",str))
//                cref.child("content").setValue(str)
//                cref.child("uid").setValue(FirebaseAuth.getInstance().currentUser?.uid?:"unknown user")
            }
            commentEditor.text = null
        }

        if(key != null){
            //TODO:リストにしてください
            //最初の読み取り
//            ref.addListenerForSingleValueEvent(object:ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) { }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    val r= mutableListOf<Comment>()
//                    p0.children.forEach {
//                        val c=it.getValue(Comment::class.java)
//                        if(c!=null){
//                            r.add(c)
//                        }
//                    }
//                    commentsText.text=r.joinToString("\n")
//
//                }
//
//            })

            //投稿後の更新
            ref.addChildEventListener(object :ChildEventListener{
                override fun onCancelled(p0: DatabaseError) { }
                override fun onChildMoved(p0: DataSnapshot, p1: String?) { }
                override fun onChildChanged(p0: DataSnapshot, p1: String?) { }
                override fun onChildRemoved(p0: DataSnapshot) { }
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val r = p0.getValue(Comment::class.java)

                }
            })
        }
    }
//
//    inner class CommentViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
//        val dateinkya = itemView.text_timeline_date
//        val message = itemView.text_comment
//        val userButton = itemView.user_button
//        val userName = itemView.name
//    }

    data class Comment(
        val uid: String,
        val content: String
    ){
        constructor():this("","")
        override fun toString(): String {
            return "$uid:  $content"
        }
    }


}
