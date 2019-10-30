package com.appli.folist.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.appli.folist.ALGOLIA_API_KEY
import com.appli.folist.ALGOLIA_APP_ID
import com.appli.folist.treeview.models.NodeValue
import com.appli.folist.treeview.models.RawTreeNode
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration


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

    fun fillTestNodes(realm: Realm){
        val root=NodeUtils().getRoot(realm)
        realm.executeTransaction {
            root.apply {
                children.add(RawTreeNode(NodeValue("２０１９年の目標"),this).apply {
                    //特に意味なし
                    children.add(RawTreeNode(NodeValue("生きる"),this))
                })
                children.add(RawTreeNode(NodeValue("ショッピングリスト"),this).apply {
                    //Seedsのアピール
                    children.add(RawTreeNode(NodeValue("ケンタッキー"),this))
                    children.add(RawTreeNode(NodeValue("コーラ"),this))
                    //Seedsの使い方として、複数のSeedsを組み合わせてSeedをつくることがかのう
                    //「重複のものはどうする（たとえば複数のレシピに同じ食材がある）」→「処理しない」
                    //「作り方が間違ってる」→「あくまでもでも」
                    children.add(RawTreeNode(NodeValue("[献立]和食セット"),this).apply {
                        children.add(RawTreeNode(NodeValue("[レシピ]塩鮭"),this).apply {
                            children.add(RawTreeNode(NodeValue("しゃけ"),this))
                            children.add(RawTreeNode(NodeValue("調味料",power = 0),this).apply {
                                children.add(RawTreeNode(NodeValue("しお"),this))
                            })
                        })
                        children.add(RawTreeNode(NodeValue("[レシピ]豚汁"),this).apply {
                            children.add(RawTreeNode(NodeValue("豚肉"),this))
                            children.add(RawTreeNode(NodeValue("じゃがいも"),this))
                            children.add(RawTreeNode(NodeValue("にんじん"),this))
                            children.add(RawTreeNode(NodeValue("調味料",power = 0),this).apply {
                                children.add(RawTreeNode(NodeValue("味噌"),this))
                            })
                        })
                        children.add(RawTreeNode(NodeValue("[レシピ]冷奴"),this).apply {
                            children.add(RawTreeNode(NodeValue("豆腐"),this))
                            children.add(RawTreeNode(NodeValue("しょうが"),this))
                            children.add(RawTreeNode(NodeValue("調味料",power = 0),this).apply {
                                children.add(RawTreeNode(NodeValue("醤油"),this))
                            })
                        })
                    })
                })
                children.add(RawTreeNode(NodeValue("ダイエット"),this).apply {
                    //階層構造・Storeでプラン検索のアピール,power, progress node機能
                    //TODO
                })
                children.add(RawTreeNode(NodeValue("基本情報資格を取得"),this).apply {
                    //階層構造のアピール,TODO:notice
                    children.add(RawTreeNode(NodeValue("教科書を読む"),this).apply {
                        children.add(RawTreeNode(NodeValue("技術部分"),this))
                        children.add(RawTreeNode(NodeValue("ビジネス部分"),this))
                        children.add(RawTreeNode(NodeValue("マネジメント部分"),this))
                        children.add(RawTreeNode(NodeValue("法律部分"),this))
                    })
                    children.add(RawTreeNode(NodeValue("二ヶ月毎日練習問題を読む",power = 60),this))
                    children.add(RawTreeNode(NodeValue("過去問をやる"),this).apply {
                        children.add(RawTreeNode(NodeValue("平成２９年春"),this))
                        children.add(RawTreeNode(NodeValue("平成２９年秋"),this))
                        children.add(RawTreeNode(NodeValue("平成３０年春"),this))
                        children.add(RawTreeNode(NodeValue("平成３０年秋"),this))
                        children.add(RawTreeNode(NodeValue("平成３１年春"),this))
                        children.add(RawTreeNode(NodeValue("平成３１年秋"),this))
                    })
                })
                children.add(RawTreeNode(NodeValue("『TOEIC単語マスター』"),this).apply {
                    //TODO:バーコード連携のアピール
                    //TODO:テスト機能
                    children.add(RawTreeNode(NodeValue("Chapter1"),this).apply {//TODO: can be clicked, -> test
                        children.add(RawTreeNode(NodeValue("hello"),this))//TODO: can not be clicked
                        children.add(RawTreeNode(NodeValue("nice"),this))
                        children.add(RawTreeNode(NodeValue("meet"),this))
                        children.add(RawTreeNode(NodeValue("you"),this))
                    })
                    children.add(RawTreeNode(NodeValue("Chapter2"),this).apply {
                        children.add(RawTreeNode(NodeValue("my"),this))
                        children.add(RawTreeNode(NodeValue("name"),this))
                        children.add(RawTreeNode(NodeValue("is"),this))
                        children.add(RawTreeNode(NodeValue("Yamamoto"),this))
                        children.add(RawTreeNode(NodeValue("what"),this))
                        children.add(RawTreeNode(NodeValue("your"),this))
                    })
                })
                children.add(RawTreeNode(NodeValue("学園祭の準備"),this).apply {
                    //TODO:協力作業のアピール
                    //何をするかがわからないからだれか書いてくれ
                    //例えば買い物：物１、物２とか
                    children.add(RawTreeNode(NodeValue("name"),this))
                    children.add(RawTreeNode(NodeValue("name"),this))
                    children.add(RawTreeNode(NodeValue("name"),this))
                })
                children.add(RawTreeNode(NodeValue("引っ越し"),this).apply {
                    //Storeで情報検索のアピール, TODO:Line version
                    children.add(RawTreeNode(NodeValue("一か月前"),this).apply {
                        children.add(RawTreeNode(NodeValue("部屋の解約"),this))
                        children.add(RawTreeNode(NodeValue("引っ越し業者に依頼"),this).apply {
                            children.add(RawTreeNode(NodeValue("A社"),this))//TODO:link,power=0
                            children.add(RawTreeNode(NodeValue("B社"),this))//TODO:link,power=0
                            children.add(RawTreeNode(NodeValue("C社"),this))//TODO:link,power=0
                        })
                    })
                    children.add(RawTreeNode(NodeValue("一か月以内"),this).apply {
                        //TODO:link
                        children.add(RawTreeNode(NodeValue("役所で"),this).apply {
                            children.add(RawTreeNode(NodeValue("転出手続き"),this))
                            children.add(RawTreeNode(NodeValue("健康保険の喪失手続き"),this))
                            children.add(RawTreeNode(NodeValue("印鑑登録の廃止"),this))
                        })
                        children.add(RawTreeNode(NodeValue("電話で"),this).apply {
                            children.add(RawTreeNode(NodeValue("電気の解約"),this))
                            children.add(RawTreeNode(NodeValue("水道の解約"),this))
                            children.add(RawTreeNode(NodeValue("ガスの解約"),this))
                        })
                        children.add(RawTreeNode(NodeValue("各種住所変更"),this).apply {
                            children.add(RawTreeNode(NodeValue("携帯電話"),this))
                            children.add(RawTreeNode(NodeValue("銀行"),this))
                        })
                        children.add(RawTreeNode(NodeValue("郵便局で：転送の依頼"),this))
                    })
                    children.add(RawTreeNode(NodeValue("当日"),this).apply {
                        children.add(RawTreeNode(NodeValue("ガスなどの立合い"),this))
                        children.add(RawTreeNode(NodeValue("新居のガスなどの立会い"),this))
                    })
                    children.add(RawTreeNode(NodeValue("引っ越し後"),this).apply {
                        children.add(RawTreeNode(NodeValue("役所で"),this).apply {
                            children.add(RawTreeNode(NodeValue("転入手続き"),this))
                            children.add(RawTreeNode(NodeValue("健康保険の手続き"),this))
                            children.add(RawTreeNode(NodeValue("印鑑登録"),this))
                        })
                        children.add(RawTreeNode(NodeValue("警察署で免許の住所変更"),this))
                    })
                })
            }
        }

    }

}