package com.festfive.app.view.chat

import com.festfive.app.di.scope.FragmentScoped
import com.festfive.app.view.ChatFragment
import com.festfive.app.view.SetupFragment
import com.festfive.app.viewmodel.chat.ChatViewModelModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [ChatViewModelModule::class])
abstract class ChatViewModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeSetupFragment(): SetupFragment

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeChatFragment(): ChatFragment


}