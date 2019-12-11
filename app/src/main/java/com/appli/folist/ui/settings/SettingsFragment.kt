package com.appli.folist.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.appli.folist.AboutActivity
import com.appli.folist.R
import com.appli.folist.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.languageGroup
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

        languageGroup.setOnCheckedChangeListener {
            RadioGroup1, radioId ->
            val config=resources.configuration
            locale =  when (radioId) {
                    R.id.radio_en -> Locale.ENGLISH
                    R.id.radio_jp ->  Locale.JAPANESE
                    R.id.radio_ch ->  Locale.CHINESE
                else->Locale.JAPANESE
                }
            Log.d("folist-lang",locale.toString())

                AppUtils().setSetting(this.activity as AppCompatActivity,"lang",locale.toString())

            config.setLocale(locale) // 新しいロケールを設定
            resources.updateConfiguration(config,resources.displayMetrics)


        }

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