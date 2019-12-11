package com.appli.folist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class CommentAdapter(val context: Context,
                     val sortedList: List<Pair<String, Int>>,
                     numOfReps: Int,
                     val numColorIds: ArrayList<Int>) : BaseAdapter() {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var sumOfReps = numOfReps.toDouble()

    override fun getCount(): Int {
        return sortedList.count()
    }

    override fun getItem(position: Int): Pair<String, Int> {
        return sortedList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.item_comment, parent, false) //表示するレイアウト取得
        val numtext = view.findViewById<TextView>(R.id.num)
        numtext.text = "${sortedList[position].second}"
        numtext.background = ColorDrawable(ContextCompat.getColor(context, if(numColorIds[position] != 0) numColorIds[position] else R.color.colorWhite))

        view.findViewById<TextView>(R.id.language).text = sortedList[position].first
        view.findViewById<TextView>(R.id.parcent).text = "${String.format("%3.2f", ((sortedList[position].second * 100).toDouble() / sumOfReps))}%"
        return view
    }
}