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
import com.appli.folist.treeview.models.TreeSeedNode
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import com.appli.folist.utils.getAttribute
import kotlinx.android.synthetic.main.dialog_show_seed.view.*
import kotlinx.android.synthetic.main.dialog_upload_seed.view.*
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

    fun saveSeedToRealm(seed:TreeSeedNode){
        sharedModel.realm.value!!.executeTransactionIfNotInTransaction {
            sharedModel.seedRoot.value!!.children.add(seed)
            sharedModel.realm.value!!.copyToRealmOrUpdate(seed)
        }
    }

    override fun onStart() {
        super.onStart()

        activity!!.setTitle(R.string.menu_seeds)

        val arrayAdapter = MyArrayAdapter(context!!, 0,sharedModel)
        sharedModel.seedRoot.value!!.children.forEach {
            arrayAdapter.add(ListItem(it.value.toString()))
        }
        seedListView.adapter = arrayAdapter
        seedDownloadButton.setOnClickListener {
            TreeSeedNode().download("-LsEAZ7GP1jHokPz8F37"){seed->
                if(seed!=null) {
                    if(seed.value.toString() in sharedModel.seedRoot.value!!.children.map { it.value.toString() }){
                        AlertDialog.Builder(context!!)
                            .setTitle(getString(R.string.action_confirm))
                            .setMessage(getString(R.string.msg_duplicated_seed_confirm_question))
                            .setPositiveButton(android.R.string.yes) { _, _ ->
                                saveSeedToRealm(seed)
                            }
                            .setNegativeButton(android.R.string.no){ dialog, _ -> dialog.cancel()}.show()

                    }else{
                        saveSeedToRealm(seed)
                    }
                }
            }
        }
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
                        }
                    AlertDialog.Builder(context).setView(dialogView)
                        .setNegativeButton(context.getString(R.string.seed_upload)) { dialog, _ ->
                            val dialogView=(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                                .inflate(R.layout.dialog_upload_seed,null).apply {
                                    seedTitleEditor.setText(seed.value.toString())
                                    seedDescriptionEditor.setText("Created by anonym")
                                    sharedModel.user.value?.getAttribute("name"){
                                        seedDescriptionEditor.setText("Created by ${it?:"anonym"}")
                                    }

                                }
                            AlertDialog.Builder(context).setView(dialogView).setTitle(seed.value.toString())
                                .setPositiveButton(context.getString(R.string.seed_upload)) { dialog, _ ->
                                    if(dialogView.seedTitleEditor.text.toString().isBlank()
                                        || dialogView.seedDescriptionEditor.text.toString().isBlank()){
                                        AppUtils().toast(context,context.getString(R.string.msg_field_blank))
                                    }else{
                                        seed.upload(dialogView.seedTitleEditor.text.toString(),
                                            dialogView.seedDescriptionEditor.text.toString()){
                                                //TODO: after upload
                                                AppUtils().toast(context,"done. id:${it}")
                                            }
                                    }
                                }
                                .setNegativeButton(context.getString(R.string.action_cancel)) { dialog, _ -> dialog.cancel() }
                                .show()
                        }
                        .setPositiveButton(context.getString(R.string.action_ok)) { dialog, _ -> dialog.cancel() }
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