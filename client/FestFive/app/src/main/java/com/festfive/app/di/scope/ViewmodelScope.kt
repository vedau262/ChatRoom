package com.festfive.app.di.scope

import androidx.lifecycle.ViewModel
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.view.TestViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)