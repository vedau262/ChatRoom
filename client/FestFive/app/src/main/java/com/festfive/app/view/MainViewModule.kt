package com.festfive.app.view

import com.festfive.app.di.scope.FragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainViewModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment
}

