package com.appli.folist.ui.createtask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R

class CreateTaskFragment : Fragment() {

    private lateinit var createTasksViewModel: CreateTasksViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createTasksViewModel =
            ViewModelProviders.of(this).get(CreateTasksViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val textView: TextView = root.findViewById(R.id.text_send)
        createTasksViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}