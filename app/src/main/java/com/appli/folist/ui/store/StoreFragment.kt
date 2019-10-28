package com.appli.folist.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.helper.deserialize
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.appli.folist.models.SharedViewModel
import com.appli.folist.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_store.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


class StoreFragment : Fragment() {

    private lateinit var storeViewModel: StoreViewModel
    private lateinit var sharedModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storeViewModel = ViewModelProviders.of(this).get(StoreViewModel::class.java)
        val root = inflater.inflate(com.appli.folist.R.layout.fragment_store, container, false)
        sharedModel = activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!

        return root
    }

    override fun onStart() {
        super.onStart()
        activity!!.setTitle(com.appli.folist.R.string.menu_store)


        @Serializable
        data class SeedResult(
            val title: String,
            val description: String,
            val key:String,
            val price:Int
        )


        testButton.setOnClickListener {
            val index=sharedModel.algolia.value!!.initIndex(IndexName("seeds"))
            val query =  Query().apply {
                query=testEditor.text.toString()
            }
            runBlocking {
                try {

                    val result = index.search(query)
                    val seeds=result.hits.deserialize(SeedResult.serializer())

                    testText.text=seeds.map { "${it.title}:${it.description}\n" }.joinToString()
                }catch (e:RuntimeException){
                    AppUtils().toast(context!!,"search failed.")//TODO
                }
            }
        }






    }
}

