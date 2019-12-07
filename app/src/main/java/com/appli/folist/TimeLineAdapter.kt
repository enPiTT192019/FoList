package com.appli.folist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appli.folist.models.Orientation
import com.appli.folist.models.TimeLineModel
import com.appli.folist.models.TimelineAttributes
import com.appli.folist.utils.DateTimeUtils
import com.appli.folist.utils.usersName
import kotlinx.android.synthetic.main.item_timeline.view.*

class TimeLineAdapter(private val mFeedList: List<TimeLineModel>, private var mAttributes: TimelineAttributes) : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View

        view = if (mAttributes.orientation == Orientation.HORIZONTAL) {
            layoutInflater.inflate(R.layout.item_timeline, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_timeline, parent, false)
        }
        return TimeLineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val timeLineModel = mFeedList[position]

        if (timeLineModel.date.isNotEmpty()) {
            holder.date.visibility = View.VISIBLE
            holder.date.text = DateTimeUtils.parseDateTime(timeLineModel.date, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm")
        } else
            holder.date.visibility = View.GONE

        holder.message.text = timeLineModel.message
        holder.userName.text = usersName

        holder.userButton.setOnClickListener {
            //TODO
            //プロフ画像押下時。プロフィール画面表示
        }
    }

    override fun getItemCount() = mFeedList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val date = itemView.text_timeline_date
        val message = itemView.text_timeline_title
        val userButton = itemView.user_button
        val userName = itemView.name
    }
}