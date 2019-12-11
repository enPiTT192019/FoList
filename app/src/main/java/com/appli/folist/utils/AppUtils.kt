package com.appli.folist.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.appli.folist.ALGOLIA_API_KEY
import com.appli.folist.ALGOLIA_APP_ID
import com.appli.folist.R
import com.appli.folist.treeview.models.NodeForFirebase
import com.appli.folist.treeview.models.NodeValue
import com.appli.folist.treeview.models.RawTreeNode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.dialog_datetime_picker.view.*
import kotlinx.android.synthetic.main.dialog_seek_bar.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AppUtils {
    fun toast(activity: Context, str:String){
        Toast.makeText(activity,str,Toast.LENGTH_SHORT).show()
    }
    fun setSetting(activity: AppCompatActivity,key:String,value:String){
        val pref= PreferenceManager.getDefaultSharedPreferences(activity)
        pref.edit{
            putString(key,value)
        }
    }
    fun getSetting(activity: AppCompatActivity,key:String):String?{
        val pref= PreferenceManager.getDefaultSharedPreferences(activity)
        return pref.getString(key,null)
    }

    fun getGson():Gson{
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }
    fun getRealm(activity: AppCompatActivity): Realm {
        Realm.init(activity)
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(config)
        return Realm.getDefaultInstance()!!
    }

    fun hideKeyboard(mActivity: Activity) {
        // Check if no view has focus:
        val view = mActivity.currentFocus
        if (view != null) {
            val inputManager = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun getAlgolia(): ClientSearch {
        val appID = ApplicationID(ALGOLIA_APP_ID)
        val apiKey = APIKey(ALGOLIA_API_KEY)
        val client = ClientSearch(appID, apiKey)
        return client
    }

    fun confirmDialog(context:Context,
                      title:String="Title",
                      message:String="",
                      cancelCallback:(DialogInterface,Int)->Unit={dialog, _ ->dialog.cancel()},
                      okCallback:(DialogInterface,Int)->Unit){
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                okCallback(dialog,whichButton)
            }
            .setNegativeButton(android.R.string.no){dialog, whichButton ->
                cancelCallback(dialog,whichButton)
            }.show()

    }

    fun datatimeDialog(context:Activity,
                       cancelCallback:(DialogInterface, Int)->Unit={  dialog, _ ->dialog.cancel()},
                       okCallback:(View, DialogInterface,Int)->Unit){

        val view=(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.dialog_datetime_picker, null).apply {
                timePicker.setIs24HourView(true)
            }
        AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.picker_title)
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                okCallback(view,dialog,whichButton)
            }
            .setNegativeButton(android.R.string.no){dialog, whichButton ->
                cancelCallback(dialog,whichButton)
            }.show()
    }

    fun seekbarDialog(context:Activity,
                      now:Int=0,
                      max:Int=100,
                       cancelCallback:(DialogInterface, Int)->Unit={  dialog, _ ->dialog.cancel()},
                       okCallback:(Int, DialogInterface,Int)->Unit){

        val view=(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.dialog_seek_bar, null).apply {
                progressSeekBar.max=max
                progressSeekBar.progress=now
                progressText.text= "${progressSeekBar.progress}%"
                progressSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                        progressText.text= "$progress%"
                     }
                })
            }
        AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.seekbar_title)
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                okCallback(view.progressSeekBar.progress,dialog,whichButton)
            }
            .setNegativeButton(android.R.string.no){dialog, whichButton ->
                cancelCallback(dialog,whichButton)
            }.show()

    }

    fun hasPermissions(context: Context?, vararg permissions:String): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    fun fillTestNodes(realm: Realm,root:RawTreeNode){
        realm.executeTransaction {
            root.apply {

                //-LvaL_KCf583Cr7ZVj56
//                addChild(RawTreeNode(NodeValue("２０１９年の目標"),this,mRealm=realm).apply {
//                    //特に意味なし
//                    addChild(RawTreeNode(NodeValue("生きる"),this,mRealm=realm))
//                })
                addChild(RawTreeNode(NodeValue("協力ノード"),this,mRealm=realm).apply {
                    //特に意味なし
                    //TODO:delete
                    firebaseRefPath="synced-nodes/-Lval6ac-hvYZoHeLhZM/data"
                    getRef()?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}
                        override fun onDataChange(p0: DataSnapshot) {
                            val remote = p0.getValue(NodeForFirebase::class.java)
                            if (remote != null) {
                                resetWithoutChildren(remote, parent, realm)
                                setSync()
                            }
                        }
                    })

//                    addChild(RawTreeNode(NodeValue("生きる"),this,mRealm=realm))
                })
                addChild(RawTreeNode(NodeValue("ショッピングリスト"),this,mRealm=realm).apply {
                    //Seedsのアピール
                    addChild(RawTreeNode(NodeValue("ケンタッキー"),this,mRealm=realm))
                    addChild(RawTreeNode(NodeValue("コーラ"),this,mRealm=realm))
                    //Seedsの使い方として、複数のSeedsを組み合わせてSeedをつくることがかのう
                    //「重複のものはどうする（たとえば複数のレシピに同じ食材がある）」→「処理しない」
                    //「作り方が間違ってる」→「あくまでもでも」
                    addChild(RawTreeNode(NodeValue("[献立]和食セット"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("[レシピ]塩鮭"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("しゃけ"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("調味料",power = 0),this,mRealm=realm).apply {
                                addChild(RawTreeNode(NodeValue("しお"),this,mRealm=realm))
                            })
                        })
                        addChild(RawTreeNode(NodeValue("[レシピ]豚汁"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("豚肉"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("じゃがいも"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("にんじん"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("調味料",power = 0),this,mRealm=realm).apply {
                                addChild(RawTreeNode(NodeValue("味噌"),this,mRealm=realm))
                            })
                        })
                        addChild(RawTreeNode(NodeValue("[レシピ]冷奴"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("豆腐"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("しょうが"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("調味料",power = 0),this,mRealm=realm).apply {
                                addChild(RawTreeNode(NodeValue("醤油"),this,mRealm=realm))
                            })
                        })
                    })
                })
                addChild(RawTreeNode(NodeValue("ダイエット"),this,mRealm=realm).apply {
                    //階層構造・Storeでプラン検索のアピール,power, progress node機能
                    //TODO
                })
                addChild(RawTreeNode(NodeValue("基本情報資格を取得"),this,mRealm=realm).apply {
                    //階層構造のアピール,TODO:notice
                    addChild(RawTreeNode(NodeValue("教科書を読む"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("技術部分"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("ビジネス部分"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("マネジメント部分"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("法律部分"),this,mRealm=realm))
                    })
                    addChild(RawTreeNode(NodeValue("二ヶ月毎日練習問題を読む",power = 60),this,mRealm=realm))
                    addChild(RawTreeNode(NodeValue("過去問をやる"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("平成２９年春"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("平成２９年秋"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("平成３０年春"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("平成３０年秋"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("平成３１年春"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("平成３１年秋"),this,mRealm=realm))
                    })
                })
                addChild(RawTreeNode(NodeValue("『TOEIC単語マスター』"),this,mRealm=realm).apply {
                    //TODO:バーコード連携のアピール
                    //TODO:テスト機能
                    addChild(RawTreeNode(NodeValue("Chapter1"),this,mRealm=realm).apply {//TODO: can be clicked, -> test
                        addChild(RawTreeNode(NodeValue("hello"),this,mRealm=realm))//TODO: can not be clicked
                        addChild(RawTreeNode(NodeValue("nice"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("meet"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("you"),this,mRealm=realm))
                    })
                    addChild(RawTreeNode(NodeValue("Chapter2"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("my"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("name"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("is"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("Yamamoto"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("what"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("your"),this,mRealm=realm))
                    })
                })
                addChild(RawTreeNode(NodeValue("学園祭の準備"),this,mRealm=realm).apply {
                    //TODO:協力作業のアピール
                    //何をするかがわからないからだれか書いてくれ
                    //例えば買い物：物１、物２とか
                    addChild(RawTreeNode(NodeValue("name"),this,mRealm=realm))
                    addChild(RawTreeNode(NodeValue("name"),this,mRealm=realm))
                    addChild(RawTreeNode(NodeValue("name"),this,mRealm=realm))
                })
                addChild(RawTreeNode(NodeValue("引っ越し"),this,mRealm=realm).apply {
                    //Storeで情報検索のアピール, TODO:Line version
                    addChild(RawTreeNode(NodeValue("一か月前"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("部屋の解約"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("引っ越し業者に依頼"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("A社"),this,mRealm=realm))//TODO:link,power=0
                            addChild(RawTreeNode(NodeValue("B社"),this,mRealm=realm))//TODO:link,power=0
                            addChild(RawTreeNode(NodeValue("C社"),this,mRealm=realm))//TODO:link,power=0
                        })
                    })
                    addChild(RawTreeNode(NodeValue("一か月以内"),this,mRealm=realm).apply {
                        //TODO:link
                        addChild(RawTreeNode(NodeValue("役所で"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("転出手続き"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("健康保険の喪失手続き"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("印鑑登録の廃止"),this,mRealm=realm))
                        })
                        addChild(RawTreeNode(NodeValue("電話で"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("電気の解約"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("水道の解約"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("ガスの解約"),this,mRealm=realm))
                        })
                        addChild(RawTreeNode(NodeValue("各種住所変更"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("携帯電話"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("銀行"),this,mRealm=realm))
                        })
                        addChild(RawTreeNode(NodeValue("郵便局で：転送の依頼"),this,mRealm=realm))
                    })
                    addChild(RawTreeNode(NodeValue("当日"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("ガスなどの立合い"),this,mRealm=realm))
                        addChild(RawTreeNode(NodeValue("新居のガスなどの立会い"),this,mRealm=realm))
                    })
                    addChild(RawTreeNode(NodeValue("引っ越し後"),this,mRealm=realm).apply {
                        addChild(RawTreeNode(NodeValue("役所で"),this,mRealm=realm).apply {
                            addChild(RawTreeNode(NodeValue("転入手続き"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("健康保険の手続き"),this,mRealm=realm))
                            addChild(RawTreeNode(NodeValue("印鑑登録"),this,mRealm=realm))
                        })
                        addChild(RawTreeNode(NodeValue("警察署で免許の住所変更"),this,mRealm=realm))
                    })
                })

                //TODO:塾の学生の宿題ーーー塾の宿題
//                addChild()

            }
        }

//        root.upload(realm,"synced-nodes/replace-with-uid/data")
    }

}

fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm:ss"): Date? {
    val sdFormat = try {
        SimpleDateFormat(pattern)
    } catch (e: IllegalArgumentException) {
        null
    }
    val date = sdFormat?.let {
        try {
            it.parse(this)
        } catch (e: ParseException){
            null
        }
    }
    return date
}