package com.appli.folist.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.AboutActivity
import com.appli.folist.R
import com.appli.folist.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        var locale: Locale = Locale.getDefault() // アプリで使用されているロケール情報を取得

        // 言語の切替え
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        settingsViewModel =
        ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        return root
    }

    override fun onStart() {
        super.onStart()
        copyrightTextView?.setOnClickListener {
            val intent= Intent(this.context, AboutActivity::class.java)
            startActivity(intent)
        }


        val nowLanguage=AppUtils().getSetting(activity as AppCompatActivity,"lang")
        languageSelector.setSelection(resources.getStringArray(R.array.language).indexOf(nowLanguage))
        languageSelector.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val spinner = parent as? Spinner
                    var language = spinner?.selectedItem as? String
                    if(language!=null&&nowLanguage!=language){
                        activity!!.recreate()
                        AppUtils().setSetting(activity as AppCompatActivity,"lang", language)                //保存
                    }
                }
        }
        activity!!.setTitle(R.string.menu_settings)
    }
}
