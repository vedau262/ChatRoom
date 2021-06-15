package com.festfive.app.application

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Singleton

/*
*Created by Nhat.vo on 8/20/2019.
*/
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent : AndroidInjector<DaggerApplication> {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent

    }

    fun inject(app: MyApp)

    override fun inject(instance: DaggerApplication)
}