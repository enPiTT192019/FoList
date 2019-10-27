package com.appli.folist.ui.seeds

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.R
import com.appli.folist.models.SharedViewModel
import com.appli.folist.treeview.models.RawTreeNode
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import kotlinx.android.synthetic.main.dialog_show_seed.view.*
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

        val arrayAdapter = MyArrayAdapter(context!!, 0,sharedModel)
        sharedModel.seedRoot.value!!.children.forEach {
            arrayAdapter.add(ListItem(it.value.toString()))
        }
        seedListView.adapter = arrayAdapter
    }

    class ListItem(val title : String)
    data class ViewHolder(val titleView: TextView,val seedDeleteButton: Button)
    class MyArrayAdapter : ArrayAdapter<ListItem> {
        private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        var sharedModel:SharedViewModel
        constructor(context : Context, resource : Int,sharedModel: SharedViewModel) : super(context, R.layout.seed_list_item){
            this.sharedModel=sharedModel
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var viewHolder : ViewHolder? = null
            var view = convertView

            if (view == null) {
                view = inflater!!.inflate(R.layout.seed_list_item, parent, false)
                viewHolder = ViewHolder(
                    view.findViewById(R.id.item_title),
                    view.findViewById(R.id.seedDeleteButton)
                )
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val listItem = getItem(position)
            viewHolder.titleView.text = listItem!!.title
            viewHolder.titleView.setOnClickListener {
                val seed=sharedModel.seedRoot.value!!.children.find { it.value.toString()== viewHolder.titleView.text}
                if(seed!=null){
                    val dialogView=(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.dialog_show_seed,null).apply {
                            setBackgroundColor(Color.rgb(238,238,238))
                            NodeUtils().refreshViewWithOnlyText(seedContentTreeView, RawTreeNode(seed))
                            seedUploadButton.setOnClickListener {
                                //TODO
                            }
                        }
                    AlertDialog.Builder(context).setView(dialogView).setTitle(seed.value.toString())
                        .setNegativeButton(context.getString(R.string.action_ok)) { dialog, _ -> dialog.cancel() }
                        .show()
                }

            }
            viewHolder.seedDeleteButton.setOnClickListener {
                val title=listItem.title
                this.remove(listItem)
                this.notifyDataSetChanged()
                sharedModel.realm.value!!.executeTransactionIfNotInTransaction {
                    sharedModel.seedRoot.value!!.children.removeAll {
                        it.value.toString()==title
                    }
                }
            }
            return view!!
        }
    }
}