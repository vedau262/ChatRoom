package com.festfive.app.di

import com.festfive.app.data.log.FrogLog
import com.festfive.app.data.log.IFrogLog
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 *Created by NhiNguyen on 3/24/2020.
 */

@Module
abstract class DBModule {
    @Singleton
    @Binds
    internal abstract fun bindLocationInfoDao(dao: FrogLog): IFrogLog
}