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
import com.appli.folist.models.OrderStatus
import com.appli.folist.models.Orientation
import com.appli.folist.models.TimeLineModel
import com.appli.folist.models.TimelineAttributes
import kotlinx.android.synthetic.main.fragment_timeline.*

private var isSet: Boolean = true

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

        if(isSet == true){
            setDataListItems()
            isSet = false
        }

        initRecyclerView()

        mAttributes.onOrientationChanged = { oldValue, newValue ->
            if(oldValue != newValue) initRecyclerView()
        }

        mAttributes.orientation = Orientation.VERTICAL  //timeline垂直or水平

    }

    private fun setDataListItems() {    //チュートリアル文
        mDataList.add(TimeLineModel("FoListへようこそ.\nFoListは、あなたの目標達成の手助けをします." , "2019-12-19 09:00", OrderStatus.ACTIVE))
        mDataList.add(TimeLineModel("タイムラインについて\n\nタイムラインは、皆のタスクの進捗状況、タスクの紹介、発信ができます。右下の「投稿」ボタンから投稿できます。", "2019-12-19 09:00", OrderStatus.ACTIVE))
        mDataList.add(TimeLineModel("タスクについて\n\nFoListは、今までにない木構造のタスク管理アプリです。一つの目標を大きめのタスクに分割し、さらにそれを噛み砕いて小さいタスクにしていきましょう。早速、「新規タスク作成」から木を植えてみましょう!\n\nタスクノードを左にスライドすると、オプションが表示されます。タスクのシードへの追加、編集、削除はそちらから行えます。", "2019-12-19 09:00", OrderStatus.ACTIVE))
        mDataList.add(TimeLineModel("シードについて\n\nシードは、大きいタスクを再利用、ストアにアップロードすることができる機能です。シードへの追加は、どの大きさのタスクでも追加することができます。シードに追加されたタスクは、他のタスクツリーに追加したり、新しいタスクとして0から始めたい時に便利です。" , "2019-12-19 09:00", OrderStatus.ACTIVE))
        mDataList.add(TimeLineModel("ストアについて\n\nストアは、皆がシェアした数々のタスクツリーを閲覧、ダウンロードすることが可能です。" , "2019-12-19 09:00", OrderStatus.ACTIVE))
        mDataList.add(TimeLineModel("ナイトモード\n\n設定から、昼間モード、ナイトモードの切り替えが可能です。" , "2019-12-19 09:00", OrderStatus.ACTIVE))
//        mDataList.add(TimeLineModel("" , "2019-12-19 09:00", OrderStatus.ACTIVE))
    }

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