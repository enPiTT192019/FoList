package com.appli.folist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*




class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var nodesMenu:SubMenu
    private lateinit var functionsMenu:SubMenu
    private lateinit var navController:NavController
    private lateinit var navNodesItems:MutableList<MenuItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setNavigationViewListener()


        navController = findNavController(R.id.nav_host_fragment)

        navNodesItems= mutableListOf()
        nodesMenu=nav_view.menu.addSubMenu(R.string.nodes)
        //TODO
        for(i in 1..3){
            navNodesItems.add(nodesMenu.add("item$i").setIcon(R.drawable.ic_menu_share))
        }
        functionsMenu=nav_view.menu.addSubMenu(R.string.functions)
        functionsMenu.add(R.string.menu_timeline).setIcon(R.drawable.ic_menu_slideshow)
        functionsMenu.add(R.string.menu_store).setIcon(R.drawable.ic_menu_share)
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
        navController.popBackStack()
        when(item.title){
            getString(R.string.menu_store)->navController.navigate(R.id.nav_store)
            getString(R.string.menu_settings)->navController.navigate(R.id.nav_settings)
            getString(R.string.menu_timeline)->navController.navigate(R.id.nav_timeline)
            else->navController.navigate(R.id.nav_node)

        }
        (nav_view.parent as DrawerLayout).closeDrawer(nav_view)
        return true
    }
}
