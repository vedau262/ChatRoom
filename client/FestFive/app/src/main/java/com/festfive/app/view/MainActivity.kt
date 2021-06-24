package com.festfive.app.view

import android.os.Bundle
import android.util.Log
import android.view.View
import com.facebook.stetho.DumperPluginsProvider
import com.facebook.stetho.Stetho
import com.facebook.stetho.dumpapp.DumperPlugin
import com.festfive.app.R
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.databinding.ActivityMainBinding
import com.festfive.app.model.Frog
import com.festfive.app.model.test.Listenner
import com.festfive.app.model.test.User
import io.realm.*
import timber.log.Timber

class MainActivity : BaseActivity<ActivityMainBinding, TestViewModel>(), View.OnClickListener {
    lateinit var realm : Realm

    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        dataBinding.apply {
            this.user = User("aaa", "xxx", "111")
            this.listenner = Listenner("le", "a", true)

        }
//        mViewModel.parseData()

//        mViewModel.mUser.observe(this, Observer { dataBinding.user = it })

        mViewModel._isSuccess.observeForever {
            Log.d("test", "$it")
        }

        initRealm()
        insertFrog()
    }

    private fun initRealm() {
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    private fun insertFrog() {
        var frog : Frog? = null
        realm.executeTransaction { transition ->
            frog = transition.createObject(Frog::class.java)
            frog?.apply {
                name = "frog 1"
                age = 1
                species = "tree frog"
                owner = "Greg"
            }
        }

        val listener = RealmObjectChangeListener{ changeFrog: Frog?,
                                                  changeSet: ObjectChangeSet? ->
            if(changeSet!!.isDeleted){
                Timber.e("the frog was deleted")
            } else {
                for(filename in changeSet.changedFields){
                    Timber.e("Field $filename changed to: ${changeFrog?.name}")
                }
            }

        }

        frog?.addChangeListener(listener)
        realm.executeTransaction {
            frog?.name = "frog xxx"
        }

        val frogA = realm.where(Frog::class.java).equalTo("name", "frog xxx").findFirst()
        realm.executeTransaction {
            frogA?.name = "frog yyy"
        }
    }



    override fun onClick(p0: View?) {
        Log.d("test","onClick")
    }

    override fun onDestroy() {
        if (!realm.isClosed()) {
            realm.close()
        }
        super.onDestroy()
    }



}