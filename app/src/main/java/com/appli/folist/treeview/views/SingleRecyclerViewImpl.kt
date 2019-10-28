package com.appli.folist.treeview.views

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.appli.folist.Tags
import com.appli.folist.treeview.models.*
import com.appli.folist.treeview.utils.px
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_edit_node.view.*
import kotlinx.android.synthetic.main.item_node.view.*
import kotlinx.android.synthetic.main.item_node.view.indentation
import kotlinx.android.synthetic.main.item_node.view.itemLinearLayout
import kotlinx.android.synthetic.main.item_node.view.slideDelete
import kotlinx.android.synthetic.main.item_node.view.slideEdit
import kotlinx.android.synthetic.main.item_node.view.slideSeed
import kotlinx.android.synthetic.main.item_quick_create_node.view.*
import java.util.*
import kotlin.math.roundToInt


private const val TAG = "SingleRecyclerView"

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
            notifyItemRangeInserted(0, viewNodes.size)
//            notifyDataSetChanged()
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
//        itemView.nodeToggle.setImageResource(if (node.isExpanded)R.drawable.ic_down else R.drawable.ic_right)
            notifyItemChanged(viewHolder.adapterPosition)
//        viewHolder.itemView.expandIndicator.startToggleAnimation(node.isExpanded)
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
            recyclerView.realm
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
        val realm: Realm?
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
                if (realm != null && viewNode.rawReference != null) {
                    //TODO:delete from realm
                    if (viewNode.rawReference!!.parent != null) {
                        realm.executeTransactionIfNotInTransaction {
                            viewNode.rawReference!!.parent!!.children.remove(viewNode.rawReference)
                        }
                        if (viewNode.parent != null) {
                            viewNode.parent!!.children.remove(viewNode)
                            fun deleteFromViewNodes(viewNode: ViewTreeNode) {
                                if (viewNode.children.size > 0) {
                                    viewNode.children.forEach { deleteFromViewNodes(it) }
                                }
                                if (viewNode in viewNodes) viewNodes.remove(viewNode)
                            }
                            deleteFromViewNodes(viewNode)
                            notifyItemRangeRemoved(
                                adapterPosition,
                                viewNode.getDisplayedNodeNumber()
                            )
                            notifyDataSetChanged()
                        } else {//level2 node
                            //TODO: confirm dialog
                            findNavController(
                                (recyclerView.rootView.context as Activity),
                                R.id.nav_host_fragment
                            ).navigate(R.id.nav_timeline)
                            (recyclerView.rootView.context as MainActivity).refreshTasksMenu()
                        }
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
                                realm.executeTransactionIfNotInTransaction { seedRoot.children.add(TreeSeedNode(node)) }
                                AppUtils().toast(recyclerView.context, recyclerView.context.getString(R.string.action_done))
                            }
                            //check duplicate
                            if (node.value!!.toString() in seedRoot.children.map { it.value.toString() }) {
                                AppUtils().confirmDialog(recyclerView.context,
                                    recyclerView.context.getString(R.string.action_confirm),
                                    recyclerView.context.getString(R.string.msg_duplicated_seed_confirm_question)
                                    ){ _, _ ->
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

        private fun bindEdit(viewNode: ViewTreeNode) {
            itemView.slideEdit.setOnClickListener {
                //ごめんなさい！！！！！！！！！！！！！
                //原因：WeSwipe
                //TODO:誰か直してくれ
                if (slideButtonCalledTime++ % 2 == 0 && realm != null) {
                    val node = viewNode.rawReference!!
                    val dialogView = ((recyclerView.context as Activity)
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(R.layout.dialog_edit_node, null).apply {
                            //TODO:complete editor
                            nodeTitleEditor.setText(node.value!!.str)
                            nodeInfoTextView.text = recyclerView.context.getString(
                                R.string.node_info_content,
                                node.parent!!.value!!.toString(),
                                node.children.size,
                                node.uuid,
                                node.value!!.uuid
                            )
                            nodeTypeEditor.setText(node.value!!.type)
                            if (node.children.size > 0) {
                                nodeTypeEditor.setText(R.string.node_type_tree)
                                nodeTypeEditor.isEnabled = false

                            }
                            nodeProgressEditor.setText(node.progress.toString())
                            nodePowerEditor.setText(node.value!!.power.toString())
                            nodeNoticeEditor.setText(node.notice?.toString() ?: "")
                            nodeSharedIdEditor.setText(node.sharedId)
                            nodeLinkEditor.setText(node.value!!.link)
                            nodeMediaUriEditor.setText(node.value!!.mediaUri)

                        }
                    AlertDialog.Builder(recyclerView.context).setView(dialogView)
                        .setTitle(R.string.edit_node)
                        .setPositiveButton(recyclerView.context.getString(R.string.action_ok)) { dialog, _ ->
                            //TODO:complete editor
//                            val title = input.text.toString()
//                            if (!title.isBlank()) {
//                                realm.executeTransaction {
//                                    viewNode.rawReference!!.value!!.str = title
//                                }
//                            }
                            notifyItemRangeChanged(0, adapterPosition + 1)
                        }
                        .setNegativeButton(recyclerView.context.getString(R.string.action_cancel)) { dialog, _ -> dialog.cancel() }
                        .show()
                }
            }
        }

        private fun bindCommon(viewNode: ViewTreeNode) {
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
                    var newNode: RawTreeNode? = null

                    if (viewNode.parent != null && realm != null) {
                        //create new RawNode
                        realm.executeTransaction {
                            newNode = realm.createObject(
                                RawTreeNode::class.java,
                                UUID.randomUUID().toString()
                            ).apply {
                                value = realm.createObject(NodeValue::class.java).apply {
                                    //set new node
                                    str = inputStr
                                }
                                parent = viewParent.rawReference
                                if (viewNode.parent!!.children.size <= 1) {
                                    progress = viewNode.parent!!.rawReference!!.progress
                                }
                            }
                            viewParent.rawReference?.children?.add((newNode))
                        }
                        viewParent.children.remove(viewNode)
                        viewNodes.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition + 1)
                        val newViewNode = ViewTreeNode(newNode!!, viewParent, null)
                        viewParent.children.add(newViewNode)
                        viewNodes.add(adapterPosition, newViewNode)
                        viewParent.children.add(viewNode)
                        viewNodes.add(adapterPosition + 1, viewNode)
                        notifyItemRangeInserted(adapterPosition + 1, 2)
//                        notifyItemChanged(adapterPosition-1)
                        notifyItemRangeChanged(0, adapterPosition + 1)
                        itemView.editText.setText("")
                    } else {
                        Log.w(
                            Tags.DEFAULT.name,
                            "SingleRecyclerViewImpl:realm not set, or parent does not exist"
                        )
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
                        newNode = RawTreeNode(seed)

                        realm.executeTransactionIfNotInTransaction {
                            viewParent.rawReference?.children?.add((newNode))
                            realm.copyToRealmOrUpdate(newNode)
                        }
                        //adjust view nodes list
                        //remove old create-node
                        viewParent.children.remove(viewNode)
                        viewNodes.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition + 1)
                        //create new view-node of new-node, update ui
                        val newViewNode = ViewTreeNode(newNode!!, viewParent, null)
                        viewParent.children.add(newViewNode)
                        viewNodes.add(adapterPosition, newViewNode)
                        //add create-node
                        viewParent.children.add(viewNode)
                        viewNodes.add(adapterPosition + 1, viewNode)
                        notifyItemRangeInserted(adapterPosition + 1, 2)
                        notifyItemRangeChanged(0, adapterPosition + 1)
                        //reset editor
                        itemView.editText.setText("")
                    } else {
                        Log.w(
                            Tags.DEFAULT.name,
                            "SingleRecyclerViewImpl:realm not set, or parent does not exist"
                        )
                    }
                }
                AppUtils().hideKeyboard(recyclerView.context as Activity)
            }
        }

        private fun bindBinary(viewNode: ViewTreeNode) {
            itemView.nodeBinaryBox.isVisible = true
            itemView.nodeBinaryBox.setImageResource(
                if (viewNode.rawReference!!.progress >= 100 * viewNode.rawReference!!.value!!.power) R.drawable.ic_checked
                else R.drawable.ic_unchecked
            )
        }

        private fun bindProgress(viewNode: ViewTreeNode) {
            itemView.nodeProgress.isVisible = true
            itemView.nodeProgressText.isVisible = true
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
            (recyclerView.context as MainActivity).refreshTasksMenu()
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

            itemView.nodeTitle.text = viewNode.value.toString()
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
                                    if (viewNode.rawReference!!.progress >= 100 * viewNode.rawReference!!.value!!.power) 0.0
                                    else 100.0 * viewNode.rawReference!!.value!!.power
                            }
                            notifyItemRangeChanged(0, adapterPosition + 1)
                        }
                        //Progress
                        viewNode.rawReference!!.value!!.type == NodeTypes.PROGRESS_NODE.name -> {
                            //TODO:use seek bar
                            val input = EditText(recyclerView.context)
                            input.inputType = InputType.TYPE_CLASS_TEXT
                            AlertDialog.Builder(recyclerView.context).setView(input)
                                .setTitle("input progress")
                                .setPositiveButton("OK") { dialog, _ ->
                                    val num = input.text.toString().toDoubleOrNull()
                                    if (num != null) {
                                        realm!!.executeTransaction {
                                            viewNode.rawReference!!.progress = num
                                        }
                                        notifyItemRangeChanged(0, adapterPosition + 1)
                                    }
                                }.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                                .show()
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
                else -> bindNode(viewNode)
            }
        }

    }
}
