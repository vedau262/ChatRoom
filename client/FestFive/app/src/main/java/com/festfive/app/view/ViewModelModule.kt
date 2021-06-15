package com.festfive.app.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.di.scope.ViewModelKey
import com.haas.app.di.ViewModelProviderFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(EmptyViewModel::class)
    abstract fun provideTemplateViewModel(viewModel: EmptyViewModel): ViewModel
}