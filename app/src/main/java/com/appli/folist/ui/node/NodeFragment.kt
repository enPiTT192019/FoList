package com.appli.folist.ui.node

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.we.swipe.helper.WeSwipe
import com.appli.folist.R
import com.appli.folist.models.SharedViewModel
import com.appli.folist.treeview.views.SingleRecyclerViewImpl
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils

class NodeFragment : Fragment() {

    private lateinit var sharedModel: SharedViewModel
    private lateinit var nodeId:String
    private lateinit var treeView: SingleRecyclerViewImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_node, container, false)
//
/*        val intent = getIntent()
        val task = intent!!.getStringExtra("TASK")
        activity!!.setTitle(task)*/

        val intent = activity?.intent
        var task = intent?.extras?.getString("TASK")
        activity!!.setTitle(task)

//
        //変数初期化
        sharedModel = activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!
        treeView = view.findViewById(R.id.treeView)
        WeSwipe.attach(treeView)
        sharedModel.realm.observe(this, Observer { treeView.realm=it })
        nodeId= arguments!!.getString("nodeId","")!!

        //木の表示
//        val node=NodeUtils().getNode(sharedModel.realm.value!!,nodeId)
        val node=sharedModel.root.value!!.children.find { it.uuid==nodeId }
        NodeUtils().refreshView(treeView,node)

        return view
    }

    override fun onStart() {
        super.onStart()
/*        val intent = activity?.intent
        val task = intent?.getStringExtra("TASK")
        activity!!.setTitle(task)*/

//        activity!!.setTitle(R.string.menu_task)

        AppUtils().hideKeyboard(context as Activity)
    }
}