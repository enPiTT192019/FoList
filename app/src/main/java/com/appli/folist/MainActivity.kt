package com.appli.folist

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.appli.folist.models.SharedViewModel
import com.appli.folist.utils.AppUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var tasksMenu:SubMenu
    private lateinit var functionsMenu:SubMenu
    private lateinit var createNodeMenu:SubMenu
    private lateinit var navController:NavController
    private lateinit var navNodesItems:MutableList<MenuItem>
    private lateinit var sharedModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setNavigationViewListener()


        //変数初期化
        navController = findNavController(R.id.nav_host_fragment)
        sharedModel= ViewModelProviders.of(this).get(SharedViewModel::class.java)
        sharedModel.realm.value=AppUtils().getRealm(this)

        //メニュー初期化
        navNodesItems= mutableListOf()
        tasksMenu=nav_view.menu.addSubMenu(R.string.menu_tasks)
        //TODO
        for(i in 1..3){
            navNodesItems.add(tasksMenu.add("item$i").setIcon(R.drawable.ic_node))
        }
        tasksMenu.add(R.string.menu_create_new_task).setIcon(R.drawable.ic_create)

        functionsMenu=nav_view.menu.addSubMenu(R.string.menu_functions)
        functionsMenu.add(R.string.menu_timeline).setIcon(R.drawable.ic_timeline)
        functionsMenu.add(R.string.menu_store).setIcon(R.drawable.ic_store)
        functionsMenu.add(R.string.menu_seeds).setIcon(R.drawable.ic_seeds)
        functionsMenu.add(R.string.action_settings).setIcon(R.drawable.ic_menu_manage)


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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var doNotCloseDrawer=false
        navController.popBackStack()
        when(item.title){
            getString(R.string.menu_store)->navController.navigate(R.id.nav_store)
            getString(R.string.menu_settings)->navController.navigate(R.id.nav_settings)
            getString(R.string.menu_timeline)->navController.navigate(R.id.nav_timeline)
            getString(R.string.menu_seeds)->navController.navigate(R.id.nav_seeds)
            getString(R.string.menu_create_new_task)->{
                doNotCloseDrawer=true
                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                AlertDialog.Builder(this).setView(input)
                    .setTitle(R.string.menu_input_task_title)
                    .setPositiveButton("OK") { _, _ ->
                        val title = input.text.toString()
                        AppUtils().toast(this,title)
                        //TODO:Level2では同じく名前のタスクを不可とする

                        tasksMenu.clear()
                        //TODO:メニューの更新・データの更新
                        for(i in 1..3){
                            navNodesItems.add(tasksMenu.add("item$i").setIcon(R.drawable.ic_node))
                        }
                        tasksMenu.add(title).setIcon(R.drawable.ic_node)
                        tasksMenu.add(R.string.menu_create_new_task).setIcon(R.drawable.ic_create)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()

            }
            else->{
                val bundle = bundleOf("nodeId" to item.title)//TODO
                navController.navigate(R.id.nav_node,bundle)
            }

        }
        if(!doNotCloseDrawer)(nav_view.parent as DrawerLayout).closeDrawer(nav_view)
        return true
    }
}
