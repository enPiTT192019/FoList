package com.appli.folist.ui.store

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.helper.deserialize
import com.algolia.search.model.ObjectID
import com.algolia.search.model.search.Query
import com.appli.folist.*
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
import kotlin.concurrent.thread

var storeItems = ArrayList<Item>()
lateinit var adapter: FoldingCellListAdapter
lateinit var inkyaview: View//TODO:WHAT IS THIS??


class StoreFragment : Fragment() {

    private lateinit var storeViewModel: StoreViewModel
    private lateinit var sharedModel: SharedViewModel
//    lateinit var theListView: ListView
//    lateinit var items: ArrayList<Item>
//    lateinit var adapter: FoldingCellListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storeViewModel = ViewModelProviders.of(this).get(StoreViewModel::class.java)
        val root = inflater.inflate(com.appli.folist.R.layout.fragment_store, container, false)
        sharedModel =
            activity?.run { ViewModelProviders.of(this).get(SharedViewModel::class.java) }!!
        inkyaview = inflater.inflate(R.layout.cell_title_layout, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        theListView = view.findViewById(R.id.storeList) as ListView
//
//        // prepare elements to display

        adapter = FoldingCellListAdapter(this.context, storeItems)
//
//        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)
//        adapter = FoldingCellListAdapter(this.context, storeItems)
        // get our list view
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
        val storeItemPriceText: TextView,
        val storeShareButton: View,
        val detailOpenButton: View,
        val detailCloseButton: View,
        val openedTopText: TextView,
        val openedPrice: TextView,
        val treeViewButton: Button,
        val itemDownloadButton: Button
    )

    //鯖
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
                sharedModel.seedRoot.value!!.addChild(seed)
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
                    view.findViewById(R.id.storeItemPriceText),
                    view.findViewById(R.id.storeShareButton),
                    view.findViewById(R.id.detailOpenButton),
                    view.findViewById(R.id.detailCloseButton),
                    view.findViewById(R.id.openedTopText),
                    view.findViewById(R.id.openedPrice),
                    view.findViewById(R.id.treeViewButton),
                    view.findViewById(R.id.itemDownloadButton)
                )
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }
            val listItem = getItem(position)

            viewHolder.storeItemTitle.text = listItem!!.title
            viewHolder.storeItemContent.text = listItem.description
            viewHolder.openedTopText.text = listItem!!.title
            viewHolder.storeItemPriceText.text =
                if (listItem.price > 0) "￥${listItem.price}" else "無料"
            viewHolder.openedPrice.text =
                if (listItem.price > 0) "￥${listItem.price}" else "無料"

            storeItems.add(Item("￥${listItem.price}", "", listItem!!.title, listItem.description, 0, "今日", "05:10 PM"))

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
                                RawTreeNode(seed,sharedModel.realm.value!!)
                            )
                        }
                AlertDialog.Builder(context).setView(dialogView)
                    .setTitle(context.getString(R.string.title_seed_content))
                    .setPositiveButton(context.getString(R.string.action_ok)) { dialog, _ -> dialog.cancel() }
                    .show()
            }

            viewHolder.detailOpenButton.setOnClickListener{ //store展開
                (view as FoldingCell).toggle(false)
                adapter.registerToggle(0)
            }

            viewHolder.detailCloseButton.setOnClickListener{
                (view as FoldingCell).toggle(false)
                adapter.registerToggle(0)
            }

            viewHolder.treeViewButton.setOnClickListener {
                TreeSeedNode().download(listItem.key) { seed ->
                    if (seed != null) {
                        showStoreSeedContent(seed)
                    } else {
                        //TODO
                    }
                }
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
                    (view as FoldingCell).toggle(true)
                    adapter.registerToggle(0)
                    runBlocking {
                        sharedModel.seedsIndex.value!!.deleteObject(ObjectID(listItem.objectID))
                    }
                    this.remove(listItem)
                    this.notifyDataSetChanged()
                }
            }

            viewHolder.storeShareButton.setOnClickListener {
                val intent=Intent(this.context,CommentActivity::class.java)
                Log.d("COMMENT","comment button clicked")
                intent.putExtra("KEY",listItem.key)
                startActivity(this.context,intent,null)

//                TreeSeedNode().download(listItem.key) { seed ->
//                    if (seed != null) {
////                        println("mokemoketitle "+ seed.value?.str+" mokemoke uuid "+ seed.uuid)
////                        intent.putExtra("SEEDTITLE", seed.value?.str)
////                        intent.putExtra("SEEDUUID", seed.value?.uuid)
////                        startActivity(this
////                        val intent = Intent(.context, intent, null)
//
//                        val intent=Intent(this.context,CommentActivity::class.java)
//                        Log.d("COMMENT","comment button clicked")
//                        intent.putExtra("KEY",listItem.key)
//                        startActivity(this.context,intent,null)
//                    } else {
//                        //TODO
//                    }
//                }
            }

            fun addToTask(seed: TreeSeedNode){
                AppUtils().confirmDialog(
                    context,
                    context.getString(R.string.title_add_to_task),
                    context.getString(R.string.msg_confirm_add_to_task, listItem.title)
                ) { _, _ ->
                    val newNode = RawTreeNode(seed,sharedModel.realm.value!!)
                    val realm = sharedModel.realm.value!!
                    if (seed.value.toString() in sharedModel.root.value!!.children.map { it.value!!.str }) {
                        AppUtils().confirmDialog(
                            context,
                            context.getString(R.string.title_confirm),
                            context.getString(R.string.msg_confirm_overwrite_task, seed.value.toString())
                        ) { _, _ ->
                            realm.executeTransactionIfNotInTransaction {
                                sharedModel.root.value!!.removeAllChild { it.value.toString() == newNode.value.toString() }
                                sharedModel.root.value!!.addChild((newNode))
                                realm.copyToRealmOrUpdate(newNode)
                            }
                        }
                    } else {
                        realm.executeTransactionIfNotInTransaction {
                            sharedModel.root.value!!.addChild((newNode))
                            realm.copyToRealmOrUpdate(newNode)
                        }
                    }
                    //(context as MainActivity).refreshTasksMenu()
                }
            }



            viewHolder.storeItemAddToTaskButton.setOnClickListener {
                val downloaded = listItem.key in sharedModel.seedRoot.value!!.children.map { it.downloadFrom }
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
                            val downadedSeed = sharedModel.seedRoot.value!!.children.find { it.downloadFrom==listItem.key }
                            if(downadedSeed != null){
                                addToTask(downadedSeed)
                            }
                        }
                    }
                }else{
                    val seed = sharedModel.seedRoot.value!!.children.find { it.downloadFrom==listItem.key }
                    if(seed != null){
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

                AlertDialog.Builder(context)
                    .setTitle("ダウンロードが完了しました!\nシードを確認してください。")
                    .setPositiveButton("ok"){ dialog, which ->
                    }.show()
            }

            viewHolder.itemDownloadButton.setOnClickListener {
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
                                                                            AlertDialog.Builder(context)
                                        .setTitle("ダウンロードが完了しました!\nシードを確認してください。")
                                        .setPositiveButton("ok"){ dialog, which ->
                                        }.show()
                                    }
                                    saveSeedToRealm(seed)

                                }
                            } else {
                                saveSeedToRealm(seed)
                                AlertDialog.Builder(context)
                                    .setTitle("ダウンロードが完了しました!\nシードを確認してください。")
                                    .setPositiveButton("ok"){ dialog, which ->
                                    }.show()
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
                try {
                    val query = Query().apply {
                        query = str
                        if (num != null) hitsPerPage = num
                    }
                    thread {
                    runBlocking {
                        try {
                            val result = index.search(query)
                            val seeds = result.hits.deserialize(SeedResult.serializer())
                            getMainExecutor(this@StoreFragment.context).execute {

                                callback(seeds)
                            }
                        }catch (e:Exception){
                            Log.e("err",e.stackTrace.toString())
                        }
                    }
                    }
                } catch (e: RuntimeException) {
                    failed()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        activity!!.setTitle(com.appli.folist.R.string.menu_store)

//        thread {

            search("", STORE_SHOW_LATEST_NUM,callback ={ seeds ->
                setList(seeds)
            })

            storeSearchButton.setOnClickListener {
                search(storeSearchEditor.text.toString(),callback ={ seeds ->
                    setList(seeds)
                })

            }
//        }




        // attach click listener to fold btn


        // add custom btn handler to first list item
//        items.get(0).setRequestBtnClickListener(View.OnClickListener {
//            Toast.makeText(
//                getApplicationContext(),
//                "CUSTOM HANDLER FOR FIRST BUTTON",
//                Toast.LENGTH_SHORT
//            ).show()
//        })
//
//        // add default btn handler for each request btn on each item if custom handler not found
//        adapter.setDefaultRequestBtnClickListener(View.OnClickListener {
//            Toast.makeText(
//                getApplicationContext(),
//                "DEFAULT HANDLER FOR ALL BUTTONS",
//                Toast.LENGTH_SHORT
//            ).show()
//        })

        // set elements to adapter



//        theListView.adapter = adapter
//
//        // set on click event listener to list view
//        theListView.onItemClickListener =
//            AdapterView.OnItemClickListener { adapterView, view, pos, l ->
//                // toggle clicked cell state
//                (view as FoldingCell).toggle(false)
//                // register in adapter that state for selected cell is toggled
//                adapter.registerToggle(pos)
//            }

    }

}

