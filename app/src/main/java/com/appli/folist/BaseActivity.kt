package com.appli.folist

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class BaseActivity : AppCompatActivity() {

    var toolbar: Toolbar? = null

    //If back button is displayed in action bar, return false
    protected var isDisplayHomeAsUpEnabled: Boolean
        get() = false
        set(value) {
        }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        injectViews()
    }

    protected fun injectViews() {
        toolbar = findViewById(R.id.toolbar)
    }

    fun setContentViewWithoutInject(layoutResId: Int) {
        super.setContentView(layoutResId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Menu
        when (item.itemId) {
        //When home is clicked
            android.R.id.home -> {
                onActionBarHomeIconClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Method for when home button is clicked
    private fun onActionBarHomeIconClicked() {
        if (isDisplayHomeAsUpEnabled) {
            onBackPressed()
        } else {
            finish()
        }
    }
}