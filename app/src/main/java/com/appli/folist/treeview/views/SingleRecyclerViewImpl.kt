package com.appli.folist.treeview.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.we.swipe.helper.WeSwipeHelper
import com.appli.folist.MainActivity
import com.appli.folist.NodeTypes
import com.appli.folist.R
import com.appli.folist.models.SharedViewModel
import com.appli.folist.treeview.models.*
import com.appli.folist.treeview.utils.px
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import com.appli.folist.utils.toDate
import com.google.firebase.database.FirebaseDatabase
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_datetime_picker.view.*
import kotlinx.android.synthetic.main.dialog_edit_node.view.*
import kotlinx.android.synthetic.main.item_node.view.*
import kotlinx.android.synthetic.main.item_node.view.indentation
import kotlinx.android.synthetic.main.item_node.view.itemLinearLayout
import kotlinx.android.synthetic.main.item_node.view.slideDelete
import kotlinx.android.synthetic.main.item_node.view.slideEdit
import kotlinx.android.synthetic.main.item_node.view.slideSeed
import kotlinx.android.synthetic.main.item_quick_create_node.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


//private const val TAG = "SingleRecyclerView"

class SingleRecyclerViewImpl : RecyclerView,
    TreeView<NodeValue> {
    private val adapter: TreeAdapter by lazy {
        val indentation = indentation.px
        TreeAdapter(
            indentation,
            this
        )
    }
    var realm: Realm? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(
        context,
        attributeSet,
        style
    )

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(adapter)
    }


    fun treeToList(roots: ViewTreeNode): MutableList<ViewTreeNode> {
        val result = mutableListOf(roots)
        val iterator = result.listIterator()
        for (item in iterator) {
            if (item.isExpanded) {
                for (child in item.children) {
                    treeToList(child).forEach {
                        iterator.add(it)
                    }
                }
            }
        }
        return result
    }

    @UiThread
    override fun setRoots(roots: MutableList<ViewTreeNode>) {
        with(adapter) {
            val nodesList = mutableListOf<ViewTreeNode>()
            for (root in roots) {
                NodeUtils().expandAllExceptLeaves(root)
                nodesList.addAll(treeToList(root))
            }
            val beforeCount = viewNodes.size
            viewNodes.clear()
            notifyItemRangeRemoved(0, beforeCount)
            viewNodes = nodesList
            nodesList.forEachIndexed { index, viewTreeNode ->
                viewTreeNode.position=index
            }
            notifyItemRangeInserted(0, viewNodes.size)
        }
    }


    fun setItemOnClick(click: (ViewTreeNode, TreeAdapter.ViewHolder) -> Unit) {
        adapter.setItemOnClick(click)
    }
}

class TreeAdapter(private val indentation: Int, private val recyclerView: SingleRecyclerViewImpl) :
    RecyclerView.Adapter<TreeAdapter.ViewHolder>() {
    internal var viewNodes: MutableList<ViewTreeNode> = mutableListOf()
    private val expandCollapseToggleHandler: (ViewTreeNode, ViewHolder) -> Unit =
        { node, viewHolder ->
            if (node.isExpanded) {
                collapse(viewHolder.adapterPosition)
            } else {
                expand(viewHolder.adapterPosition)
            }
            notifyItemChanged(viewHolder.adapterPosition)
        }
    lateinit var itemOnclick: (ViewTreeNode, ViewHolder) -> Unit

    init {
        setHasStableIds(true)
    }

    fun setItemOnClick(click: (ViewTreeNode, ViewHolder) -> Unit) {
        itemOnclick = click
    }

    override fun getItemId(position: Int): Long {
        return viewNodes[position].id
    }

    override fun getItemViewType(position: Int): Int {
        val node = viewNodes[position]
        return node.type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = ViewNodeUtils().getLayout(viewType)
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false),
            indentation,
            recyclerView,
            recyclerView.realm,
            (recyclerView.context as MainActivity).sharedModel
        )
    }

    override fun getItemCount(): Int {
        return viewNodes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(viewNodes[position])
    }

    @UiThread
    private fun expand(position: Int) {
        if (position >= 0) {
            // expand
            val node = viewNodes[position]
            val insertPosition = position + 1
            val insertedSize = node.children.size
            viewNodes.addAll(insertPosition, node.children)
            node.isExpanded = true
            notifyItemRangeInserted(insertPosition, insertedSize)
        }
    }

    @UiThread
    private fun collapse(position: Int) {
        // collapse
        if (position >= 0) {
            val node = viewNodes[position]
            var removeCount = 0
            fun removeChildrenFrom(cur: ViewTreeNode) {
                viewNodes.remove(cur)
                removeCount++
                if (cur.isExpanded) {
                    cur.children.forEach { removeChildrenFrom(it) }
                    cur.isExpanded = false
                }
            }
            node.children.forEach { removeChildrenFrom(it) }
            node.isExpanded = false
            notifyItemRangeRemoved(position + 1, removeCount)
        }
    }

    companion object {
        //ごめんなさい！！！！！！！！！！！！
        private var slideButtonCalledTime = 0
    }

    inner class ViewHolder(
        view: View,
        private val indentation: Int,
        recyclerView: SingleRecyclerViewImpl,
        val realm: Realm?,
        val sharedModel: SharedViewModel
    ) : RecyclerView.ViewHolder(view), WeSwipeHelper.SwipeLayoutTypeCallBack {


        //for slide
        override fun getSwipeWidth(): Float {
            return itemView.slideDelete.width.toFloat() + itemView.slideEdit.width.toFloat() + itemView.slideSeed.width.toFloat()
        }

        override fun needSwipeLayout(): View {
            return itemView.itemLinearLayout
        }

        override fun onScreenView(): View {
            return itemView.itemLinearLayout
        }

        private fun bindIndentation(viewNode: ViewTreeNode) {
            itemView.indentation.minimumWidth = indentation * viewNode.getLevel()
        }

        private fun bindDelete(viewNode: ViewTreeNode) {
            itemView.slideDelete.setOnClickListener {
                if (slideButtonCalledTime++ % 2 == 0) {
                    if (realm == null || viewNode.rawReference == null) return@setOnClickListener
                    //TODO:confirm?
                    if (viewNode.parent != null) {
                        realm.executeTransactionIfNotInTransaction {
                            viewNode.rawReference!!.parent!!.removeChild(viewNode.rawReference!!)

                            if (viewNode.parent != null) {
                                viewNode.parent!!.children.remove(viewNode)
                                fun deleteFromViewNodes(viewNode: ViewTreeNode) {
                                    if (viewNode.children.size > 0) {
                                        viewNode.children.forEach { deleteFromViewNodes(it) }
                                    }
                                    if (viewNode in viewNodes) viewNodes.remove(viewNode)
                                }
                                deleteFromViewNodes(viewNode)
                            }
                            notifyItemRangeRemoved(
                                adapterPosition,
                                viewNode.getDisplayedNodeNumber()
                            )

                            viewNode.rawReference!!.firebaseRefPath=null
                            notifyDataSetChanged()
                        }
                    } else {//level2 node
                        realm.executeTransactionIfNotInTransaction {
                            sharedModel.root.value!!.removeChild(viewNode.rawReference!!)
                        }

                        val navController = findNavController(
                            (recyclerView.rootView.context as Activity),
                            R.id.nav_host_fragment
                        )
                        navController.popBackStack()
                        navController.navigate(R.id.nav_timeline)
                    }
                }
            }
        }

        private fun bindGenerateSeed(viewNode: ViewTreeNode) {
            itemView.slideSeed.setOnClickListener {
                if (slideButtonCalledTime++ % 2 == 0) {
                    AppUtils().confirmDialog(
                        recyclerView.context,
                        recyclerView.context.getString(R.string.node_generate_seed_title),
                        recyclerView.context.getString(R.string.node_generate_seed_msg)
                    ) { _, _ ->
                        if (realm != null) {
                            val node = viewNode.rawReference!!
                            val seedRoot = NodeUtils().getSeedRoot(realm)
                            fun addSeed() {
                                realm.executeTransactionIfNotInTransaction {
                                    seedRoot.addChild(
                                        TreeSeedNode(node)
                                    )
                                }
                                AppUtils().toast(
                                    recyclerView.context,
                                    recyclerView.context.getString(R.string.action_done)
                                )
                            }
                            //check duplicate
                            if (node.value!!.toString() in seedRoot.children.map { it.value.toString() }) {
                                AppUtils().confirmDialog(
                                    recyclerView.context,
                                    recyclerView.context.getString(R.string.action_confirm),
                                    recyclerView.context.getString(
                                        R.string.msg_duplicated_seed_confirm_question,
                                        node.value!!.toString()
                                    )
                                ) { _, _ ->
                                    realm.executeTransactionIfNotInTransaction {
                                        seedRoot.children.removeAll {
                                            it.value.toString() == node.value!!.toString()
                                        }
                                    }
                                    addSeed()
                                }
                            } else {
                                addSeed()
                            }
                        }
                    }
                }
            }
        }

        fun setNodeRefreshFunctions(node:RawTreeNode){
            setNodeRefreshView(node)
            setNodeChildAdded(node)
            setNodeChildRemoved(node)
            node.children.forEach { setNodeRefreshFunctions(it) }
        }
        private fun setNodeRefreshView(node:RawTreeNode){
            node.refreshView = {
//                                    notifyItemRangeChanged(0,adapterPosition+1)
                Log.d("refresh", "${it.value.toString()}")
//                if(!it.value?.str.isNullOrBlank()){
                if(it.viewNodeRef?.position!=null && it.viewNodeRef?.position!!>0){
                    notifyItemChanged( it.viewNodeRef!!.position!!)
                }
//                }
            }
        }
        private fun setNodeChildAdded(node:RawTreeNode){
            node.refreshChildAdded={parent,viewNode,child->
                if(viewNode!=null) {
                    val newViewNode = ViewTreeNode(child, parent = viewNode, position = 0)
                    viewNode!!.children.add(viewNode.children.size - 1, newViewNode)

                    if (viewNode!!.isExpanded) {
                        //add to last
                        val pos = viewNode.position!! + viewNode.getDisplayedNodeNumber() - 2
                        newViewNode.position = pos

                        viewNodes.add(pos, newViewNode)
                        notifyItemInserted(pos)
                    }

                    notifyDataSetChanged()
                }
            }
        }
        private fun setNodeChildRemoved(node:RawTreeNode){
            node.refreshChildRemoved= {node,viewNode->
                if(viewNode!=null&&viewNode!!.position!=null){
                    viewNodes.remove(viewNode)
//                    //TODO:remove children of this node
                    if(viewNode.position!=null && viewNode.position!!>=0) {
//                        notifyItemRemoved(viewNode.position!!)
//                    }
//                    if(viewNode.position==null || viewNode.position!!<0)return

                        if (viewNode.parent != null) {
                            viewNode.parent!!.children.remove(viewNode)
                            fun deleteFromViewNodes(viewNode: ViewTreeNode) {
                                if (viewNode.children.size > 0) {
                                    viewNode.children.forEach { deleteFromViewNodes(it) }
                                }
                                if (viewNode in viewNodes) viewNodes.remove(viewNode)
                            }
                            deleteFromViewNodes(viewNode)
                        }
                        notifyItemRangeRemoved(
                            adapterPosition,
                            viewNode.getDisplayedNodeNumber()
                        )
                        notifyDataSetChanged()
                    }
                }
            }
        }

        @SuppressLint("NewApi")
        private fun bindEdit(viewNode: ViewTreeNode) {
            itemView.slideEdit.setOnClickListener {
                //ごめんなさい！！！！！！！！！！！！！
                //原因：WeSwipe
                //TODO:誰か直してくれ
                if (slideButtonCalledTime++ % 2 == 0 && realm != null) {
                    val node = viewNode.rawReference!!
                    var noticeTime: Date? = null
                    val dialogView = ((recyclerView.context as Activity)
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(R.layout.dialog_edit_node, null).apply {
                            sharedModel.tempImageUri.removeObservers(context as MainActivity)
                            sharedModel.tempImageUri.value = node.value!!.mediaUri
                            nodeTitleEditor.setText(node.value!!.str)
                            nodeProgressEditor.setText(node.progress.toString())
                            nodePowerEditor.setText(node.value!!.power.toString())

                            nodeNoticeEditor.text = node.notice?.toString()
                                ?: context.getString(R.string.node_notice_notset)
                            nodeSharedIdEditor.text =
                                if (node.sharedId.isNullOrBlank()) context.getString(R.string.node_shared_id_not_shared) else node.sharedId

                            nodeShareButton.isVisible = node.sharedId.isNullOrBlank()
                            nodeLinkEditor.setText(node.value!!.link)

                            nodeLinkEditor.addTextChangedListener {
                                if (URLUtil.isValidUrl(it.toString()) || it.toString().isBlank()) {
                                    nodelinkValidText.text =
                                        context.getString(R.string.node_link_valid)
                                    nodelinkValidText.setTextColor(Color.rgb(0, 255, 0))
                                } else {
                                    nodelinkValidText.text =
                                        context.getString(R.string.node_link_invalid)
                                    nodelinkValidText.setTextColor(Color.rgb(255, 0, 0))
                                }
                            }
                            nodeNoticeEditor.setOnClickListener {
                                AppUtils().datatimeDialog(context as MainActivity) { view, _, _ ->
                                    val d = view.datePicker
                                    val t = view.timePicker
                                    noticeTime =
                                        Date(d.year, d.month, d.dayOfMonth, t.hour, t.minute, 0)
                                    val format =
                                        SimpleDateFormat(context.getString(R.string.picker_format))
                                    nodeNoticeEditor.text = format.format(noticeTime)
                                }
                            }
                            fun resetMedia(uri: String?) {
                                nodeMediaDeleteButton.isVisible = !uri.isNullOrBlank()
                                nodeMedia.layoutParams.width = if (uri.isNullOrBlank()) 36 else 200
                                nodeMedia.layoutParams.height = if (uri.isNullOrBlank()) 36 else 200
                                if (!uri.isNullOrBlank()) {
                                    nodeMedia.setImageURI(Uri.parse(uri))
                                } else {
                                    nodeMedia.setImageResource(R.drawable.ic_add_black_36dp)
                                }
                            }
                            resetMedia(node.value!!.mediaUri)
                            sharedModel.tempImageUri.observe(
                                context as MainActivity,
                                androidx.lifecycle.Observer {
                                    if (!sharedModel.tempImageUri.value.isNullOrBlank()) {
                                        resetMedia(sharedModel.tempImageUri.value!!)
                                    }
                                })
                            nodeMedia.setOnClickListener {
                                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                                photoPickerIntent.type = "image/*"
                                val RESULT_LOAD_IMG = 1
                                (context as MainActivity).startActivityForResult(
                                    photoPickerIntent,
                                    RESULT_LOAD_IMG
                                )
                            }
                            nodeMediaDeleteButton.setOnClickListener {
                                sharedModel.tempImageUri.value = null
                                resetMedia(null)
                            }

                            nodeInfoTextView.text = recyclerView.context.getString(
                                R.string.node_info_content,
                                node.parent?.value?.toString() ?: "root",
                                node.children.size,
                                node.uuid,
                                node.value!!.uuid
                            )
                            if (node.children.size > 0) {//tree
                                nodeTypeText.setText(R.string.node_type_tree)
                                nodeProgressEditor.isEnabled = false
                                nodeTypeSelector.isVisible = false
                            } else {//node
                                nodeTypeText.isVisible = false
                                nodeProgressEditor.isEnabled = true
                                nodeTypeSelector.isEnabled = true
                                nodeTypeBinary.text =
                                    context.getString(R.string.node_type_binary_node)
                                nodeTypeProgress.text =
                                    context.getString(R.string.node_type_progress_node)
                            }

                            nodeTypeBinary.isChecked =
                                node.value!!.type == NodeTypes.BINARY_NODE.name
                            nodeProgressEditor.isEnabled =
                                node.value!!.type == NodeTypes.PROGRESS_NODE.name
                            nodeTypeProgress.isChecked =
                                node.value!!.type == NodeTypes.PROGRESS_NODE.name
                            nodeShareButton.isVisible = node.sharedId.isNullOrBlank()

//                            nodeSyncedIdEditor.text=if(node.syncedId.isNullOrBlank())"" else node.syncedId
//                            nodeSyncButton.isVisible=node.syncedId.isNullOrBlank()
                            nodeSyncedIdEditor.text = node.firebaseRefPath
                            nodeSyncButton.isVisible = true

                            nodeShareButton.setOnClickListener {
                                //TODO:upload to frebase and set shared-id
                            }
                            nodeSyncButton.setOnClickListener {
                               // node.refreshView = {
                               //     //TODO:refresh view
//                            //        notifyItemRangeChanged(0,adapterPosition+1)
                               //     Log.d("refresh", "${it.value.toString()}")
                             //       notifyItemChanged(adapterPosition + 1)
                             //   }
                                node.upload { id ->
                                    nodeSyncedIdEditor.text = id
                                    setNodeRefreshFunctions(node)
                                }
                            }
                        }

                    AlertDialog.Builder(recyclerView.context).setView(dialogView)
                        .setTitle(R.string.edit_node)
                        .setPositiveButton(recyclerView.context.getString(R.string.action_ok)) { dialog, _ ->
                            val title = dialogView.nodeTitleEditor.text.toString()
                            if (!title.isBlank() && title != node.value!!.str
                                && !(node.parent?.parent == null //check duplicate
                                        && title in sharedModel.root.value!!.children.map { it.value.toString() })
                            ) {
                                realm.executeTransactionIfNotInTransaction {
                                    node.value!!.str = title
                                }

//                                if(noticeTime != null){
//                                    var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//                                        .setSmallIcon(R.drawable.folist_icon)
//                                        .setContentTitle("FoList")
//                                        .setContentText(textContent)
//                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                }

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("value/str").setValue(title)
                                }
                            }

                            var power = dialogView.nodePowerEditor.text.toString().toInt()
                            if (power >= 0 && power != node.value!!.power) {
                                realm.executeTransactionIfNotInTransaction {
                                    if(power>10000)power=10000
                                    if(power<=0)power=0
                                    node.value!!.power = power
                                    if (node.progress!! > power * 100) {
                                        node.progress = (power * 100).toDouble()
                                    } else {
                                        node.progress =
                                            (node.progress!! / node.value!!.power * power).toDouble()
                                    }
                                }

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("value/power").setValue(power)
                                }
                            }

                            val type = when {
                                dialogView.nodeTypeBinary.isChecked -> NodeTypes.BINARY_NODE.name
                                dialogView.nodeTypeProgress.isChecked -> NodeTypes.PROGRESS_NODE.name
                                else -> NodeTypes.BINARY_NODE.name
                            }
                            if (type != node.value!!.type) {
                                realm.executeTransactionIfNotInTransaction {
                                    node.value!!.type = type
                                    if (type == NodeTypes.BINARY_NODE.name) {
                                        node.progress =
                                            (if (node.progress!! > 0) node.value!!.power * 100 else 0).toDouble()
                                    }

                                }

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("value/type").setValue(type)
                                }
                            }

                            realm.executeTransactionIfNotInTransaction {
                                node.notice = dialogView.nodeNoticeEditor.text.toString()
                                    .toDate(recyclerView.context.getString(R.string.picker_format))

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("notice").setValue(node.notice)
                                }
                            }

                            if (dialogView.nodeSharedIdEditor.text.toString() != recyclerView.context.getString(
                                    R.string.node_shared_id_not_shared
                                )
                            ) {
                                realm.executeTransactionIfNotInTransaction {
                                    node.sharedId = dialogView.nodeSharedIdEditor.text.toString()
                                }

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("sharedId").setValue(node.sharedId)
                                }
                            }

                            realm.executeTransactionIfNotInTransaction {
                                node.value!!.mediaUri = sharedModel.tempImageUri.value

                                if (!node.firebaseRefPath.isNullOrBlank()) {
//                                    FirebaseDatabase.getInstance().getReference(node.firebaseRefPath!!)
//                                        .child("value/mediaUri").setValue(node.value!!.mediUri)//TODO
                                }
                            }

                            val link = dialogView.nodeLinkEditor.text.toString()
                            if (link != node.value!!.link
                                && (URLUtil.isValidUrl(link) || link.isBlank())
                            ) {
                                realm.executeTransactionIfNotInTransaction {
                                    node.value!!.link = link
                                }

                                if (!node.firebaseRefPath.isNullOrBlank()) {
                                    FirebaseDatabase.getInstance()
                                        .getReference(node.firebaseRefPath!!)
                                        .child("value/link").setValue(link)
                                }
                            }



                            notifyItemRangeChanged(0, adapterPosition + 1)
                        }
                        .setNegativeButton(recyclerView.context.getString(R.string.action_cancel)) { dialog, _ -> dialog.cancel() }
                        .show()
                }
            }
        }

        fun bindLink(viewNode: ViewTreeNode){
            if(!viewNode.rawReference?.value?.link.isNullOrBlank()){
                itemView.rightView.setOnLongClickListener {
                    //TODO
                    val browserIntent=Intent(Intent.ACTION_VIEW, Uri.parse(viewNode.rawReference?.value?.link))
                    (recyclerView.context as Activity).startActivity(browserIntent)
                    true
                }
            }
        }

        private fun bindCommon(viewNode: ViewTreeNode) {
            setNodeRefreshFunctions(viewNode.rawReference!!)
            bindLink(viewNode)
            bindIndentation(viewNode)
            bindDelete(viewNode)
            bindGenerateSeed(viewNode)
            bindEdit(viewNode)
        }

        private fun bindQuickCreateNode(viewNode: ViewTreeNode) {
            bindIndentation(viewNode)
            //シードリスト取得・Spinner更新
            val seedTitles = NodeUtils().getSeedRoot(realm!!).children.map { it.value.toString() }
            val adapter = ArrayAdapter<String>(
                recyclerView.context,
                android.R.layout.simple_dropdown_item_1line,
                seedTitles
            )
            itemView.editText.setAdapter<ArrayAdapter<String>>(adapter)
            itemView.editText.setOnClickListener { (it as AutoCompleteTextView).showDropDown() }
            //hide seed button unless a seed title is inputted
            itemView.seedButton.isVisible = false
            itemView.editText.addTextChangedListener { text ->
                itemView.seedButton.isVisible =
                    (NodeUtils().getSeedRoot(realm).children.find { it.value.toString() == text.toString() } != null)
            }

            //新規ノード
            itemView.createButton.setOnClickListener {
                if (itemView.editText.text.toString().isBlank()) {
                    Toast.makeText(
                        recyclerView.context,
                        "Please input something",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (viewNode.parent != null) {
                    //get variables
                    val inputStr = itemView.editText.text.toString()
                    val viewParent = viewNode.parent as ViewTreeNode
//                    var newNode: RawTreeNode? = null

                    if (viewNode.parent != null && realm != null) {
                        val newNode = RawTreeNode(
                            NodeValue(inputStr),
                            parent = viewParent.rawReference,
                            mRealm = sharedModel.realm.value!!
                        )
                        newNode.progress =
                            if (viewParent.children.size <= 1) viewParent.rawReference!!.progress else 0.0
                        realm.executeTransactionIfNotInTransaction {
                            realm.copyToRealmOrUpdate(newNode)
                        }
                        viewParent.rawReference!!.addChild(newNode)
                        viewParent.children.remove(viewNode)
                        viewNodes.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition + 1)
                        val newViewNode = ViewTreeNode(newNode!!, viewParent,before =  null,onlyText = false,position = adapterPosition+1)
                        viewParent.addChild(newViewNode)
                        viewNodes.add(adapterPosition, newViewNode)
                        viewParent.addChild(viewNode)
                        viewNodes.add(adapterPosition + 1, viewNode)
                        notifyItemRangeInserted(adapterPosition + 1, 2)
                        notifyItemRangeChanged(0, adapterPosition + 1)
                        itemView.editText.setText("")
                    }
                }
                AppUtils().hideKeyboard(recyclerView.context as Activity)
            }


            //シードから生成する
            itemView.seedButton.setOnClickListener {
                if (itemView.editText.text.toString().isBlank()) {
                    Toast.makeText(
                        recyclerView.context,
                        "Please input something",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (viewNode.parent != null) {
                    //get variables
                    val inputStr = itemView.editText.text.toString()
                    val viewParent = viewNode.parent as ViewTreeNode
                    var newNode: RawTreeNode? = null

                    if (viewNode.parent != null && realm != null) {
                        //create new RawNode
                        val seed = NodeUtils().getSeedRoot(realm)
                            .children.find { it.value.toString() == inputStr }
                        if (seed == null) {
                            Toast.makeText(
                                recyclerView.context,
                                "Not found in seed list",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                        newNode = RawTreeNode(seed,viewParent.rawReference!! ,realm)

                        realm.executeTransactionIfNotInTransaction {
                            viewParent.rawReference?.addChild(newNode!!)
                            realm.copyToRealmOrUpdate(newNode)
                        }
                        //adjust view nodes list
                        //remove old create-node
                        viewParent.children.remove(viewNode)
                        viewNodes.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition + 1)
                        //create new view-node of new-node, update ui
                        val newViewNode = ViewTreeNode(newNode!!, viewParent, null,position = adapterPosition+1)
                        viewParent.addChild(newViewNode)
                        viewNodes.add(adapterPosition, newViewNode)
                        //add create-node
                        viewParent.addChild(viewNode)
                        viewNodes.add(adapterPosition + 1, viewNode)
                        notifyItemRangeInserted(adapterPosition + 1, 2)
                        notifyItemRangeChanged(0, adapterPosition + 1)
                        //reset editor
                        itemView.editText.setText("")
                    }
                }
                AppUtils().hideKeyboard(recyclerView.context as Activity)
            }
        }

        private fun bindBinary(viewNode: ViewTreeNode) {
            itemView.nodeBinaryBox.isVisible = true
            itemView.nodeBinaryBox.setImageResource(
                if (viewNode.rawReference!!.progress!! >= 100 * viewNode.rawReference!!.value!!.power) R.drawable.ic_checked
                else R.drawable.ic_unchecked
            )

            if (!viewNode.rawReference!!.firebaseRefPath.isNullOrBlank()
                && !viewNode.rawReference!!.value!!.str.isNullOrBlank()) {
                FirebaseDatabase.getInstance()
                    .getReference(viewNode.rawReference!!.firebaseRefPath!!)
                    .child("progress").setValue(viewNode.rawReference!!.progress)
            }
        }

        private fun bindProgress(viewNode: ViewTreeNode) {
            itemView.nodeProgress.isVisible = true
            itemView.nodeProgressText.isVisible = true
            realm!!.executeTransaction {
                val progress = viewNode.rawReference!!.calcProgress()
                val power = viewNode.rawReference!!.value!!.power
                itemView.nodeProgress.max = 100 * power
                itemView.nodeProgress.progress = progress.roundToInt()
                itemView.nodeProgressText.text = when {
                    progress >= 10000 -> (progress / 1000).roundToInt().toString() + "k"
                    progress >= 1000 -> progress.roundToInt().toString()
                    progress >= 100 -> "%.1f".format(progress)
                    progress >= 10 -> "%.2f".format(progress)
                    else -> " %.2f".format(progress)
                }

                if (viewNode.rawReference!=null
                    &&!viewNode.rawReference!!.firebaseRefPath.isNullOrBlank()
                    && !viewNode.rawReference!!.value!!.str.isNullOrBlank()
                    ) {
                    Log.d("firebase","progress update:$progress")
                    FirebaseDatabase.getInstance()
                        .getReference(viewNode.rawReference!!.firebaseRefPath!!)
                        .child("progress").setValue(progress)
                }
            }
        }

        private fun bindNodeToggle(viewNode: ViewTreeNode, hasCreateNode: Boolean = true) {
            val minChildrenNumber = if (hasCreateNode) 1 else 0
            itemView.nodeToggle.setImageResource(
                when {
                    viewNode.children.size <= minChildrenNumber -> R.drawable.ic_leaf
                    viewNode.isExpanded -> R.drawable.ic_down
                    else -> R.drawable.ic_right
                }
            )
            if (viewNode.children.size <= minChildrenNumber) {
                itemView.nodeToggle.setColorFilter(Color.argb(200, 255, 255, 255))
            } else {
                itemView.nodeToggle.setColorFilter(null)
            }
        }

        private fun bindOnlyText(viewNode: ViewTreeNode) {
            bindCommon(viewNode)
            bindNodeToggle(viewNode, false)
            itemView.nodeTitle.text = viewNode.value.toString()

            itemView.leftView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }
            itemView.middleView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }
            itemView.rightView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }
        }

        private fun bindTest(viewNode: ViewTreeNode) {
            bindCommon(viewNode)
            bindNodeToggle(viewNode, false)
            itemView.nodeBinaryBox.isVisible = false
            bindProgress(viewNode)
            itemView.nodeSharedIcon.isVisible = viewNode.rawReference!!.sharedId != null
            itemView.nodeNoticeIcon.isVisible = viewNode.rawReference!!.notice != null
            itemView.nodeTitle.text = viewNode.value.toString()

            fun showTest(){
                val node=viewNode
                val questions= mutableListOf<String>()
                val correctAnswers=mutableListOf<String>()
                val otherAnswers=mutableListOf<MutableList<String>>()

                node.rawReference!!.children.map{it.value}.forEach{
                    questions.add(it!!.getDetail("question")?:"question")
                    correctAnswers.add(it.getDetail("correctAnswer")?:"correct answer")
                    val oa= mutableListOf(it.getDetail("otherAnswer1")?:"wrong answer",it.getDetail("otherAnswer2")?:"wrong answer",it.getDetail("otherAnswer3")?:"wrong answer")
                    otherAnswers.add(oa)
                }
                AppUtils().TestDialog(recyclerView.context as Activity,node.toString(),questions,correctAnswers,otherAnswers,
                    okCallback = {progress->
                        realm!!.executeTransaction {
                        viewNode.rawReference!!.progress =progress*viewNode.rawReference!!.value!!.power*100
                        }
                        var p:ViewTreeNode?=viewNode
                        while(p!=null){
                            notifyItemChanged(p.position!!)
                            p=p.parent
                        }
                })
                Log.d("test-node",node.toString())
                val a=2
            }
            itemView.middleView.setOnClickListener {
                showTest()
            }
            itemView.leftView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }
            itemView.rightView.setOnClickListener {
                showTest()
            }
        }

        private fun bindNode(viewNode: ViewTreeNode) {
            bindCommon(viewNode)
            itemView.nodeProgress.isVisible = false
            itemView.nodeProgressText.isVisible = false
            itemView.nodeBinaryBox.isVisible = false
            //TODO: add your bind here
            when {
                viewNode.rawReference!!.children.size >= 1 -> {
                    //tree node
                    bindProgress(viewNode)
                }
                viewNode.rawReference!!.value!!.type == NodeTypes.BINARY_NODE.name -> {
                    bindBinary(viewNode)
                }
                viewNode.rawReference!!.value!!.type == NodeTypes.PROGRESS_NODE.name -> {
                    bindProgress(viewNode)
                }
                else -> {
                    bindBinary(viewNode)
                }
            }

            bindNodeToggle(viewNode)
            //TODO:if link is not blank,...

            itemView.nodeTitle.text = viewNode.value.toString()
            itemView.nodeTitle.setTextColor(
                if (viewNode.rawReference!!.children.size >= 1) Color.rgb(
                    100,
                    100,
                    100
                ) else Color.rgb(0, 0, 0)
            )
            itemView.nodeSharedIcon.isVisible = viewNode.rawReference!!.sharedId != null
            itemView.nodeNoticeIcon.isVisible = viewNode.rawReference!!.notice != null


            itemView.middleView.setOnClickListener {
                if (viewNode.children.size <= 1) {//only leaves can be adjusted
                    //TODO:add your on-click event here
                    when {
                        //Tree
                        viewNode.rawReference!!.children.size >= 1 -> {
                            //tree node, do nothing
                        }
                        //Binary
                        viewNode.rawReference!!.value!!.type == NodeTypes.BINARY_NODE.name -> {
                            realm!!.executeTransaction {
                                viewNode.rawReference!!.progress =
                                    if (viewNode.rawReference!!.progress!! >= 100 * viewNode.rawReference!!.value!!.power) 0.0
                                    else 100.0 * viewNode.rawReference!!.value!!.power
                            }
                            var p:ViewTreeNode?=viewNode
                            while(p!=null){
                                notifyItemChanged(p.position!!)
                                p=p.parent
                            }
                        }
                        //Progress
                        viewNode.rawReference!!.value!!.type == NodeTypes.PROGRESS_NODE.name -> {
                            val input = EditText(recyclerView.context)
                            AppUtils().seekbarDialog(
                                recyclerView.context as Activity,
                                viewNode.rawReference!!.progress!!.toInt(),
                                viewNode.rawReference!!.value!!.power * 100

                            ) { progress, _, _ ->
                                realm!!.executeTransaction {
                                    viewNode.rawReference!!.progress = progress.toDouble()
                                }
                                var pp:ViewTreeNode?=viewNode
                                while(pp!=null){
                                    notifyItemChanged(pp.position!!)
                                    pp=pp.parent
                                }
                            }
                        }
                        else -> {

                        }
                    }
                }
            }
            itemView.leftView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }
            itemView.rightView.setOnClickListener {
                expandCollapseToggleHandler(viewNode, this)
            }

        }

        //TODO: create your bind function here, do not forget setOnClickListener
        internal fun bind(viewNode: ViewTreeNode) {
            when (viewNode.type) {
                ViewNodeTypes.QUICK_CREATE_NODE -> bindQuickCreateNode(viewNode)
                ViewNodeTypes.ONLY_TEXT -> bindOnlyText(viewNode)
                ViewNodeTypes.TEST_NODE->bindTest(viewNode)
                else -> bindNode(viewNode)
            }
        }

    }
}
