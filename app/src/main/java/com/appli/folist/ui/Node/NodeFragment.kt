package com.appli.folist.ui.Node

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R

class NodeFragment : Fragment() {

    private lateinit var nodeViewModel: NodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nodeViewModel =
            ViewModelProviders.of(this).get(NodeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_node, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        nodeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}