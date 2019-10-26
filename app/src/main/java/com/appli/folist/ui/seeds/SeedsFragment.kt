package com.appli.folist.ui.seeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R

class SeedsFragment : Fragment() {

    private lateinit var seedsViewModel: SeedsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        seedsViewModel =
            ViewModelProviders.of(this).get(SeedsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val textView: TextView = root.findViewById(R.id.text_send)
        seedsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}