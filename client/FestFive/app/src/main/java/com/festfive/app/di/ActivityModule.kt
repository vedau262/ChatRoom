package com.festfive.app.di

import com.festfive.app.view.MainActivity
import com.festfive.app.di.scope.ActivityScoped
import com.festfive.app.view.chat.SocketTestActivity
import com.festfive.app.view.ViewModelModule
import com.festfive.app.view.call.GroupCallActivity
import com.festfive.app.view.call.VideoCallActivity
import com.festfive.app.view.chat.ChatViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
*Created by Nhat.vo on 8/27/2019.
*/

@Module(includes = [ViewModelModule::class])
abstract class ActivityModule {

    @ActivityScoped
//    @ContributesAndroidInjector(modules = [MainViewModule::class])
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [ChatViewModule::class])
    internal abstract fun contributeSocketTestActivity(): SocketTestActivity

    @ActivityScoped
    @ContributesAndroidInjector()
    internal abstract fun contributeVideoCallActivity(): VideoCallActivity

    @ActivityScoped
    @ContributesAndroidInjector()
    internal abstract fun contributeGroupVideoCallActivity(): GroupCallActivity

}