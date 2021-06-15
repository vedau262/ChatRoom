package com.festfive.app.di

import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.base.viewmodel.IBaseViewModel
import dagger.Binds
import dagger.Module

/**
 * Created by Nhat.vo on 16/11/2020.
 */
@Module
abstract class ConfigureModule {

    @Binds
    abstract fun provideTemplateViewModel(viewModel: EmptyViewModel): IBaseViewModel
}