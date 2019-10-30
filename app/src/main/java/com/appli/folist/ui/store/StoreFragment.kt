package com.appli.folist.ui.store

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.helper.deserialize
import com.algolia.search.model.ObjectID
import com.algolia.search.model.search.Query
import com.appli.folist.MainActivity
import com.appli.folist.R
import com.appli.folist.STORE_SHOW_LATEST_NUM
import com.appli.folist.models.SharedViewModel
import com.appli.folist.treeview.models.RawTreeNode
import com.appli.folist.treeview.models.TreeSeedNode
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import kotlinx.android.synthetic.main.dialog_show_seed.view.*
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
        sharedModel =
            activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!

        return root
    }

    @Serializable
    data class SeedResult(
        val title: String,
        val description: String,
        val key: String,
        val price: Int,
        val objectID: String
    )

    fun setList(items: List<SeedResult>) {
        val arrayAdapter = StoreAdapter(context!!, 0, sharedModel)
        if (items.isNotEmpty()) {
            items.forEach {
                arrayAdapter.add(
                    StoreListItem(
                        it.title,
                        it.description,
                        it.key,
                        it.price,
                        it.objectID
                    )
                )
            }
            storeList.adapter = arrayAdapter

        } else {
            storeList.adapter = arrayAdapter
        }
    }

    class StoreListItem(
        val title: String,
        val description: String,
        val key: String,
        val price: Int,
        val objectID: String
    )

    data class ViewHolder(
        val storeItemTitle: TextView,
        val storeItemAddToTaskButton: View,
        val storeItemDownloadButton: View,
        val storeItemDeleteButton: View,
        val storeItemContent: TextView,
        val storeItemPriceText: TextView
    )

    class StoreAdapter : ArrayAdapter<StoreListItem> {
        private var inflater: LayoutInflater? =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        var sharedModel: SharedViewModel

        constructor(context: Context, resource: Int, sharedModel: SharedViewModel) : super(
            context,
            R.layout.store_item
        ) {
            this.sharedModel = sharedModel
        }

        fun saveSeedToRealm(seed: TreeSeedNode) {
            sharedModel.realm.value!!.executeTransactionIfNotInTransaction {
                sharedModel.seedRoot.value!!.children.add(seed)
                sharedModel.realm.value!!.copyToRealmOrUpdate(seed)
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var viewHolder: ViewHolder? = null
            var view = convertView

            if (view == null) {
                view = inflater!!.inflate(R.layout.store_item, parent, false)
                viewHolder = ViewHolder(
                    view.findViewById(R.id.storeItemTitle),
                    view.findViewById(R.id.storeItemAddToTaskButton),
                    view.findViewById(R.id.storeItemDownloadButton),
                    view.findViewById(R.id.storeItemDeleteButton),
                    view.findViewById(R.id.storeItemContent),
                    view.findViewById(R.id.storeItemPriceText)
                )
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }
            val listItem = getItem(position)

            viewHolder.storeItemTitle.text = listItem!!.title
            viewHolder.storeItemContent.text = listItem.description
            viewHolder.storeItemPriceText.text =
                if (listItem.price > 0) "￥${listItem.price}" else ""

            //TODO: if author, enable delete
            //TODO: if downloaded disable download
            fun showStoreSeedContent(seed: TreeSeedNode) {
                //TODO: only show level2 nodes if price>0
                val dialogView =
                    (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(R.layout.dialog_show_seed, null).apply {
                            setBackgroundColor(Color.rgb(238, 238, 238))
                            NodeUtils().refreshViewWithOnlyText(
                                seedContentTreeView,
                                RawTreeNode(seed)
                            )
                        }
                AlertDialog.Builder(context).setView(dialogView)
                    .setTitle(context.getString(R.string.title_seed_content))
                    .setPositiveButton(context.getString(R.string.action_ok)) { dialog, _ -> dialog.cancel() }
                    .show()
            }

            viewHolder.storeItemTitle.setOnClickListener {
                TreeSeedNode().download(listItem.key) { seed ->
                    if (seed != null) {
                        showStoreSeedContent(seed)
                    } else {
                        //TODO
                    }
                }
            }

            viewHolder.storeItemContent.setOnClickListener {
                TreeSeedNode().download(listItem.key) { seed ->
                    if (seed != null) {
                        showStoreSeedContent(seed)
                    } else {
                        //TODO
                    }
                }
            }

            viewHolder.storeItemDeleteButton.setOnClickListener {
                AppUtils().confirmDialog(
                    context,
                    context.getString(R.string.title_confirm),
                    context.getString(R.string.msg_confirm_delete, listItem.title)
                ) { _, _ ->
                    runBlocking {
                        sharedModel.seedsIndex.value!!.deleteObject(ObjectID(listItem.objectID))
                    }

                    this.remove(listItem)
                    this.notifyDataSetChanged()
                }
            }
            fun addToTask(seed: TreeSeedNode){
                AppUtils().confirmDialog(
                    context,
                    context.getString(R.string.title_add_to_task),
                    context.getString(R.string.msg_confirm_add_to_task, listItem.title)
                ) { _, _ ->
                    val newNode = RawTreeNode(seed)
                    val realm = sharedModel.realm.value!!
                    if (seed.value.toString() in sharedModel.root.value!!.children.map { it.value!!.str }) {
                        AppUtils().confirmDialog(
                            context,
                            context.getString(R.string.title_confirm),
                            context.getString(R.string.msg_confirm_overwrite_task, seed.value.toString())
                        ) { _, _ ->
                            realm.executeTransactionIfNotInTransaction {
                                sharedModel.root.value!!.children.removeAll { it.value.toString() == newNode.value.toString() }
                                sharedModel.root.value!!.children.add((newNode))
                                realm.copyToRealmOrUpdate(newNode)
                            }
                        }
                    } else {
                        realm.executeTransactionIfNotInTransaction {
                            sharedModel.root.value!!.children.add((newNode))
                            realm.copyToRealmOrUpdate(newNode)
                        }
                    }
                    (context as MainActivity).refreshTasksMenu()
                }
            }
            viewHolder.storeItemAddToTaskButton.setOnClickListener {
                val downloaded=listItem.key in sharedModel.seedRoot.value!!.children.map { it.downloadFrom }
                if(!downloaded){
                    AppUtils().confirmDialog(
                        context,
                        context.getString(R.string.title_download),
                        if (listItem.price == 0) context.getString(R.string.msg_confirm_download_free, listItem.title)
                        else context.getString(R.string.msg_confirm_download_purchase, listItem.title, listItem.price)
                    ) { _, _ ->
                        TreeSeedNode().download(listItem.key) { seed ->
                            if (seed != null) {
                                if (seed.value.toString() in sharedModel.seedRoot.value!!.children.map { it.value.toString() }) {
                                    AppUtils().confirmDialog(
                                        context,
                                        context.getString(R.string.action_confirm),
                                        context.getString(R.string.msg_duplicated_seed_confirm_question,seed.value.toString())
                                    ){_,_->
                                        sharedModel.realm.value!!.executeTransactionIfNotInTransaction {
                                            sharedModel.seedRoot.value!!.children.removeAll {
                                                it.value.toString() == seed.value!!.toString()
                                            }
                                        }
                                        saveSeedToRealm(seed)
                                    }
                                } else {
                                    saveSeedToRealm(seed)
                                }
                            }
                            val downadedSeed=sharedModel.seedRoot.value!!.children.find { it.downloadFrom==listItem.key }
                            if(downadedSeed!=null){
                                addToTask(downadedSeed)
                            }
                        }
                    }
                }else{
                    val seed=sharedModel.seedRoot.value!!.children.find { it.downloadFrom==listItem.key }
                    if(seed!=null){
                        addToTask(seed)
                    }
                }


            }
            viewHolder.storeItemDownloadButton.setOnClickListener {
                AppUtils().confirmDialog(
                    context,
                    context.getString(R.string.title_download),
                    if (listItem.price == 0) context.getString(R.string.msg_confirm_download_free, listItem.title)
                    else context.getString(R.string.msg_confirm_download_purchase, listItem.title, listItem.price)
                ) { _, _ ->
                    TreeSeedNode().download(listItem.key) { seed ->
                        if (seed != null) {
                            if (seed.value.toString() in sharedModel.seedRoot.value!!.children.map { it.value.toString() }) {
                                AppUtils().confirmDialog(
                                    context,
                                    context.getString(R.string.action_confirm),
                                    context.getString(R.string.msg_duplicated_seed_confirm_question,seed.value.toString())
                                ){_,_->
                                    sharedModel.realm.value!!.executeTransactionIfNotInTransaction {
                                        sharedModel.seedRoot.value!!.children.removeAll {
                                            it.value.toString() == seed.value!!.toString()
                                        }
                                    }
                                    saveSeedToRealm(seed)
                                }
                            } else {
                                saveSeedToRealm(seed)
                            }
                        }
                    }
                }
            }

            return view!!
        }
    }

    fun search(
        str: String, num: Int? = null,
        failed: () -> Unit = {
            AppUtils().toast(
                context!!,
                getString(R.string.store_search_failed_msg)
            )
        },
        callback: (List<SeedResult>) -> Unit = { setList(it) }
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

        search("", STORE_SHOW_LATEST_NUM) { seeds ->
            setList(seeds)
        }

        storeSearchButton.setOnClickListener {
            search(storeSearchEditor.text.toString()) { seeds ->
                setList(seeds)
            }
        }

    }
}

