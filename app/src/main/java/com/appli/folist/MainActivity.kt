package com.appli.folist

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.algolia.search.model.IndexName
import com.appli.folist.models.SharedViewModel
import com.appli.folist.models.TimeLineModel
import com.appli.folist.treeview.models.NodeValue
import com.appli.folist.treeview.models.RawTreeNode
import com.appli.folist.utils.AppUtils
import com.appli.folist.utils.NodeUtils
import com.appli.folist.utils.getAttribute
import com.appli.folist.utils.setAttribute
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

val mDataList = ArrayList<TimeLineModel>()
var loggedIn: Boolean = false

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var tasksMenu:SubMenu
    private lateinit var functionsMenu:SubMenu
    private lateinit var createNodeMenu:SubMenu
    private lateinit var navController:NavController
    private lateinit var navNodesItems:MutableList<MenuItem>
    lateinit var sharedModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setNavigationViewListener()

        //言語設定
        val lang=AppUtils().getSetting(this,"lang")
        if(lang.isNullOrBlank()){
            AppUtils().setSetting(this,"lang","日本語")
        }
        val locale =  when (lang) {
            "English" ->    Locale.ENGLISH
            "日本語" ->     Locale.JAPANESE
            "中文" ->       Locale.SIMPLIFIED_CHINESE
            else ->         Locale.JAPANESE
        }
        val config=resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config,resources.displayMetrics)

        //テスト用
        NodeUtils().clearAllNodesForTest(AppUtils().getRealm(this))

        //変数初期化
        navController = findNavController(R.id.nav_host_fragment)
        sharedModel= ViewModelProviders.of(this).get(SharedViewModel::class.java)
        sharedModel.realm.value=AppUtils().getRealm(this)
        sharedModel.root.value=NodeUtils().getRoot(sharedModel.realm.value!!)
        sharedModel.seedRoot.value=NodeUtils().getSeedRoot(sharedModel.realm.value!!)
        sharedModel.algolia.value=AppUtils().getAlgolia()
        sharedModel.seedsIndex.value=sharedModel.algolia.value?.initIndex(IndexName("seeds"))

        //テスト用
        AppUtils().fillTestNodes(sharedModel.realm.value!!,sharedModel.root.value!!)

        //メニュー初期化
        navNodesItems = mutableListOf()
        tasksMenu = nav_view.menu.addSubMenu(R.string.menu_tasks)
        refreshTasksMenu()
        functionsMenu = nav_view.menu.addSubMenu(R.string.menu_functions)
        functionsMenu.add(R.string.menu_timeline).setIcon(R.drawable.ic_timeline)
        functionsMenu.add(R.string.menu_store).setIcon(R.drawable.ic_store)
        functionsMenu.add(R.string.menu_seeds).setIcon(R.drawable.ic_seeds)
        functionsMenu.add(R.string.menu_settings).setIcon(R.drawable.ic_menu_manage)


        //ログインとナビのユーザー情報の更新
        //TODO:ログインダイアログ
        if(loggedIn == false) {
            sharedModel.login(this, "appli@test.com", "password")
            sharedModel.user.observe(this, Observer {
                if (it != null) {
                    sharedModel.user.value?.setAttribute("name", "TestUser")
                    userEmail.text = "email:${it.email} uid:${it.uid}"
                    it.getAttribute("name") {
                        name.text = it ?: "no name"
                    }
                }
            })
            loggedIn = true
        }
    }



    fun <F : Fragment> getFragment(fragmentClass: Class<F>): F? {
        val navHostFragment = this.supportFragmentManager.fragments.first() as NavHostFragment

        navHostFragment.childFragmentManager.fragments.forEach {
            if (fragmentClass.isAssignableFrom(it.javaClass)) {
                return it as F
            }
        }

        return null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setNavigationViewListener() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    fun refreshTasksMenu(){
        tasksMenu.clear()
        sharedModel.realm.value!!.executeTransaction {
        sharedModel.root.value!!.children.forEach {
            val progress=it.calcProgress()
            val progressText=when{
                progress>=100->"%d".format(progress.roundToInt())
                progress>=10->"%d".format(progress.roundToInt())
                else->"%.1f".format(progress)
            }

            tasksMenu.add("[%s%%] %s".format(progressText,it.value!!.str)).setIcon(R.drawable.ic_node)
        }
        }
        tasksMenu.add(R.string.menu_create_new_task).setIcon(R.drawable.ic_create)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var doNotCloseDrawer=false
        navController.popBackStack()
        when(item.title){
            getString(R.string.menu_store)->navController.navigate(R.id.nav_store)
            getString(R.string.menu_settings)->navController.navigate(R.id.nav_settings)
            getString(R.string.menu_timeline)->navController.navigate(R.id.nav_timeline)
            getString(R.string.menu_seeds)->navController.navigate(R.id.nav_seeds)

            //新規Level2ノード（以下、タスク）
            getString(R.string.menu_create_new_task)->{
                doNotCloseDrawer=true
                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                AlertDialog.Builder(this).setView(input)
                    .setTitle(R.string.menu_input_task_title)
                    .setPositiveButton(getString(R.string.action_ok)) { dialog, _ ->
                        val title = input.text.toString()
                        //同じ名前のタスク・空の入力を不可とする
                        if(title.isBlank()||title in sharedModel.root.value!!.children.map { it.value!!.str }){
                            AppUtils().toast(this,getString(R.string.msg_duplicated_task_title))
                        }else{
                            sharedModel.realm.value!!.executeTransaction{
                                sharedModel.root.value!!.addChild(RawTreeNode(NodeValue(title),sharedModel.root.value!!,sharedModel.realm.value!!))
                            }
                            refreshTasksMenu()
                            AppUtils().hideKeyboard(this@MainActivity)  //Keyboard排除?できてないかもsk
                            val id= sharedModel.root.value!!.children.find { it.value!!.str==title }?.uuid
                            if(id!=null){
                                val bundle = bundleOf("nodeId" to id)
                                navController.navigate(R.id.nav_node,bundle)
                                (nav_view.parent as DrawerLayout).closeDrawer(nav_view)
                            }
//tnk 12/06
                            sharedModel.task_name.value = title
//tnk
                        }
                        AppUtils().hideKeyboard(this)
                    }.setNegativeButton(getString(R.string.action_cancel)) { dialog, _ -> dialog.cancel()}.show()
            }

            //タスク
            else->{
                //[...%]ThisIsATitle -> ThisIsATitle
                val title= """%\] (.*)${'$'}""".toRegex().find(item.title)?.groupValues?.get(1)
//tnk 12/06
                sharedModel.task_name.value = title
//tnk
                val id= sharedModel.root.value!!.children.find { it.value!!.str==title }?.uuid
                if(id!=null){
                    val bundle = bundleOf("nodeId" to id)
                    navController.navigate(R.id.nav_node,bundle)
                }
            }
        }
        if(!doNotCloseDrawer)(nav_view.parent as DrawerLayout).closeDrawer(nav_view)
        sharedModel.task_name.value =
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK) {
            try {
                sharedModel.tempImageUri.value = data?.data.toString()
            } catch (e: Exception) { }
        }
    }
}

class CustomTypefaceSpan(family: String, private val mTypeface: Typeface) : TypefaceSpan(family) {

    override fun updateDrawState(textPaint: TextPaint) {
        applyCustomTypeFace(textPaint, mTypeface)
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        applyCustomTypeFace(textPaint, mTypeface)
    }

    private fun applyCustomTypeFace(paint: Paint, typeface: Typeface) {
        val oldStyle: Int
        val old = paint.getTypeface()
        if (old == null) {
            oldStyle = 0
        } else {
            oldStyle = old!!.getStyle()
        }

        val fake = oldStyle and typeface.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.setFakeBoldText(true)
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.setTextSkewX(-0.25f)
        }

        paint.setTypeface(typeface)
    }
}