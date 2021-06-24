package com.festfive.app.data.log

import com.festfive.app.model.Frog
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Provider

class FrogLog @Inject constructor(private val dao: Provider<Realm>) : IFrogLog{
    override fun saveFrog(frog: Frog): Completable {
        return Completable.create {emitter ->
            dao.get().use {
                it.executeTransaction{
                    it.insert(frog)
                    emitter.onComplete()
                }
            }
        }
    }

    override fun getAllFrog(): Single<MutableList<Frog>> {
        return Single.create{emitter ->
            dao.get().use {
                it.executeTransaction{
                    val realmResult = it.where(Frog::class.java).findAll()
                    emitter.onSuccess(it.copyFromRealm(realmResult))
                }
            }
        }
    }

    override fun getFrogByName(name: String): Single<MutableList<Frog>> {
        TODO("Not yet implemented")
    }
}