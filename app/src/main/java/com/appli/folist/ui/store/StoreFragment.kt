package com.appli.folist.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R
import com.appli.folist.models.SharedViewModel
import kotlinx.android.synthetic.main.fragment_store.*

class StoreFragment : Fragment() {

    private lateinit var storeViewModel: StoreViewModel
    private lateinit var sharedModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storeViewModel =
            ViewModelProviders.of(this).get(StoreViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_store, container, false)
        sharedModel = activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!

        return root
    }

    override fun onStart() {
        super.onStart()
        activity!!.setTitle(R.string.menu_store)
        testText.text="111"
    }
}