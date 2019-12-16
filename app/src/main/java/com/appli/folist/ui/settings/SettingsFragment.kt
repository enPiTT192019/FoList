package com.appli.folist.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.AboutActivity
import com.appli.folist.R
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//言語変更　設計中
        var locale: Locale = Locale.getDefault() // アプリで使用されているロケール情報を取得

        // 言語の切替え
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


//        1.update
//        2.save
//
//        ----
//                fragmet:
//        1.load(ui)
//
//        ---
//                main:
//        1.load(setting)
//
//
//        }


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



        languageSelector.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
//            initialize()
//            initialize()    //再描画

//            fun initialize(){
                override fun onNothingSelected(parent: AdapterView<*>?) {

                    val config=resources.configuration
                    var locale = Locale.JAPANESE
                    config.setLocale(locale)
                    Log.d("folist-lang",locale.toString())
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val spinner = parent as? Spinner
                    var language = spinner?.selectedItem as? String
                    val config=resources.configuration

                    var locale =  when (language) {
                                             "English" ->    Locale.ENGLISH
                                             "日本語" ->     Locale.JAPANESE
                                             "中文" ->       Locale.CHINESE
                                             else ->         Locale.JAPANESE
                    }

                    config.setLocale(locale)
                    resources.updateConfiguration(config,resources.displayMetrics)

//                  resources.updateConfiguration(config, null)
//                  AppUtils().setSetting(this.activity as AppCompatActivity,"lang",locale.toString())                //保存
//                  Log.d("folist-lang",locale.toString())
                }
//            }


        }
        activity!!.setTitle(R.string.menu_settings)
    }
}

//fun initialize(inflater: LayoutInflater, container: ViewGroup?) {
//
//    var locale: Locale = Locale.getDefault() // アプリで使用されているロケール情報を取得
//
//    // 言語の切替え
//    val view = inflater.inflate(R.layout.fragment_settings, container, false)
//    val language: RadioGroup = view.findViewById(R.id.RadioGroup1)
//
//    language.setOnCheckedChangeListener {
//        _, checkedId :Int ->
//        when (R.id.RadioGroup1) {
//            R.id.radio_en -> Locale.ENGLISH
//            R.id.radio_jp -> Locale.JAPAN
//            R.id.radio_ch -> Locale.CHINESE
//        }
//
//        Locale.setDefault(locale) // 新しいロケールを設定
//        val config = Configuration()
//        config.locale = locale
//        val resources: Resources = getActivity()?.baseContext!!.resources
//        resources.updateConfiguration(config, null)
//    }
//}

    /*   val light: RadioGroup = view.findViewById(R.id.RadioGroup1)

    light.setOnCheckedChangeListener {
            _, checkedId :Int ->
        when (R.id.RadioGroup2) {
            R.id.radio_bright ->
            R.id.radio_dark ->
        }
    }*/

/*    Locale.setDefault(locale) // 新しいロケールを設定
    val config = VolumeShape.Configuration()
    config.locale = locale // Resourcesに対するロケールを設定
    val resources = baseContext.resources
    resources.updateConfiguration(config, null) // Resourcesに対する新しいロケールを反映

    initialize() // ※ ポイント 初期化し直し再描画させます*/

