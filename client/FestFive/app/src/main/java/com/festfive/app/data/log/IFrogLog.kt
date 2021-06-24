package com.festfive.app.data.log

import com.festfive.app.model.Frog
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IFrogLog {
    fun saveFrog(frog: Frog) : Completable
    fun getAllFrog() : Single<MutableList<Frog>>
    fun getFrogByName(name : String) : Single<MutableList<Frog>>
}