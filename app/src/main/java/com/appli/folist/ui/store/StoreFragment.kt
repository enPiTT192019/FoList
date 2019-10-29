package com.appli.folist.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.helper.deserialize
import com.algolia.search.model.search.Query
import com.appli.folist.R
import com.appli.folist.STORE_SHOW_LATEST_NUM
import com.appli.folist.models.SharedViewModel
import com.appli.folist.utils.AppUtils
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
        sharedModel =
            activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!

        return root
    }

    @Serializable data class SeedResult(
        val title: String,
        val description: String,
        val key: String,
        val price: Int
    )

    fun setList(seeds:List<SeedResult>){

    }

    fun search(
        str: String, num: Int? = null,
        failed: () -> Unit = {
            AppUtils().toast(
                context!!,
                getString(R.string.store_search_failed_msg)
            )
        },
        callback: (List<SeedResult>) -> Unit={setList(it)}
    ) {
        val index = sharedModel.seedsIndex.value
        if (index != null) {
            runBlocking {
                try {
                    val query = Query().apply {
                        query = str
                        if (num != null) hitsPerPage = num
                    }
                    val result = index.search(query)
                    val seeds = result.hits.deserialize(SeedResult.serializer())
                    callback(seeds)
                } catch (e: RuntimeException) {
                    failed()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity!!.setTitle(com.appli.folist.R.string.menu_store)

        search("", STORE_SHOW_LATEST_NUM){seeds->
           setList(seeds)
        }

    }
}

