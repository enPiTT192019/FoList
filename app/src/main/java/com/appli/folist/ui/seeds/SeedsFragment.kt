package com.appli.folist.ui.seeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R
import com.appli.folist.models.SharedViewModel
import kotlinx.android.synthetic.main.fragment_seeds.*

class SeedsFragment : Fragment() {

    private lateinit var seedsViewModel: SeedsViewModel
    private lateinit var sharedModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        seedsViewModel =
            ViewModelProviders.of(this).get(SeedsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_seeds, container, false)
        sharedModel = activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!

        return root
    }

    override fun onStart() {
        super.onStart()

        textViewTest.text=sharedModel.seedRoot.value!!.children.map { it.value.toString() }.joinToString(",")

    }
}