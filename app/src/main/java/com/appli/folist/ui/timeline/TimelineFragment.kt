package com.appli.folist.ui.timeline

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appli.folist.*
import com.appli.folist.models.Orientation
import com.appli.folist.models.TimelineAttributes
import kotlinx.android.synthetic.main.fragment_timeline.*

class TimelineFragment : Fragment() {

    private lateinit var timelineViewModel: TimelineViewModel
    private lateinit var mAdapter: TimeLineAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAttributes: TimelineAttributes

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mAttributes = TimelineAttributes(
            markerSize = 20,
            //todo
//            markerColor = ContextCompat.getColor(this, R.color.material_grey_500),
            markerColor = 10395294,
            markerInCenter = true,
            linePadding = 2,
//            startLineColor = ContextCompat.getColor(this, R.color.colorAccent),
            startLineColor = 14162784,
//            endLineColor = ContextCompat.getColor(this, R.color.colorAccent),
            endLineColor = 14162784,
            lineStyle = TimelineView.LineStyle.NORMAL,
            lineWidth = 2,
            lineDashWidth = 4,
            lineDashGap = 2
        )

        timelineViewModel =
            ViewModelProviders.of(this).get(TimelineViewModel::class.java)
        val root = inflater!!.inflate(R.layout.fragment_timeline, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        mAttributes.onOrientationChanged = { oldValue, newValue ->
            if(oldValue != newValue) initRecyclerView()
        }

        mAttributes.orientation = Orientation.VERTICAL  //timeline垂直or水平

    }

    private fun initRecyclerView() {
        initAdapter()

        //todo
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            @SuppressLint("LongLogTag")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                //todo
//                dropshadow.visibility = if(recyclerView.getChildAt(0).top < 0) View.VISIBLE else View.GONE
            }
        })
    }

    private fun initAdapter() {
        mLayoutManager = if (mAttributes.orientation == Orientation.HORIZONTAL) {
            LinearLayoutManager(this.activity, RecyclerView.HORIZONTAL, false)
        } else {
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        }

        recyclerView.layoutManager = mLayoutManager
        mAdapter = TimeLineAdapter(mDataList, mAttributes)
        recyclerView.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        button.setOnClickListener {
            val intent = Intent(this.context, tweetActivity::class.java)
            startActivity(intent)
        }
        activity!!.setTitle(R.string.menu_timeline)
    }
}