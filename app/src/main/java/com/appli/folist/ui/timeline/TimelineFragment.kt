package com.appli.folist.ui.timeline

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            markerColor = ContextCompat.getColor(this.activity!!, R.color.material_grey_500),
            markerInCenter = true,
            linePadding = 2,
            startLineColor = ContextCompat.getColor(this.activity!!, R.color.colorAccent),
            endLineColor = ContextCompat.getColor(this.activity!!, R.color.colorAccent),
            lineStyle = TimelineView.LineStyle.NORMAL,
            lineWidth = 2,
            lineDashWidth = 4,
            lineDashGap = 2
        )

//        val myRef= FirebaseDatabase.getInstance().getReference("posts")
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue(String::class.java)
//                Log.d(TAG, "Value is: " + value!!)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })

        timelineViewModel =
            ViewModelProviders.of(this).get(TimelineViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_timeline, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setDataListItems()
        initRecyclerView()

        mAttributes.onOrientationChanged = { oldValue, newValue ->
            if(oldValue != newValue) initRecyclerView()
        }

        mAttributes.orientation = Orientation.VERTICAL  //timeline垂直or水平

    }

//    private fun setDataListItems() {    //Sample Post
//        mDataList.add(TimeLineModel("Item successfully delivered", "", OrderStatus.INACTIVE))
//        mDataList.add(TimeLineModel("Courier is out to delivery your order", "2017-02-12 08:00", OrderStatus.ACTIVE))
//        mDataList.add(TimeLineModel("Item has reached courier facility at New Delhi", "2017-02-11 21:00", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Item has been given to the courier", "2017-02-11 18:00", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Item is packed and will dispatch soon", "2017-02-11 09:30", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Order is being readied for dispatch", "2017-02-11 08:00", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Order processing initiated", "2017-02-10 15:00", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Order confirmed by seller", "2017-02-10 14:30", OrderStatus.COMPLETED))
//        mDataList.add(TimeLineModel("Order placed successfully", "2017-02-10 14:00", OrderStatus.COMPLETED))
//    }

    private fun initRecyclerView() {
        initAdapter()

        commentView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            @SuppressLint("LongLogTag")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initAdapter() {
        mLayoutManager = if (mAttributes.orientation == Orientation.HORIZONTAL) {
            LinearLayoutManager(this.activity, RecyclerView.HORIZONTAL, false)
        } else {
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        }

        commentView.layoutManager = mLayoutManager
        mAdapter = TimeLineAdapter(mDataList, mAttributes)
        commentView.adapter = mAdapter
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