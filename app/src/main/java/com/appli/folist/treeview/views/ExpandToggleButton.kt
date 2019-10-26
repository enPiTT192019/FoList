package com.appli.folist.treeview.views
import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatImageView
import com.appli.folist.R

class ExpandToggleButton : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initCustomAttributes(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style) {
        initCustomAttributes(attributeSet)
    }

    @UiThread
    fun startToggleAnimation(isExpanded: Boolean) {
        (drawable as? Animatable)?.stop()
        setImageResource(if(isExpanded) R.drawable.ic_expand_toggle_animated else R.drawable.ic_collapse_toggle_animated)
        (drawable as Animatable).start()
    }

    @UiThread
    fun setIcon(isExpanded: Boolean) {
        if(isExpanded){
            setImageResource(R.drawable.ic_remove_black_24dp)
        }else{
            setImageResource(R.drawable.ic_expand_collapse_toggle)
        }
    }

    private fun initCustomAttributes(attributeSet: AttributeSet) {
        setImageResource(R.drawable.ic_expand_collapse_toggle)
    }

}