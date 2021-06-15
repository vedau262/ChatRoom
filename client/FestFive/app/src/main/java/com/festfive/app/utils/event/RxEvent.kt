package com.festfive.app.utils.event

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


/*
*Created by NhiNguyen on 9/17/2019.
*/

object RxEvent {

    private val publisher = PublishSubject.create<Any>()

    fun send(event: Any) {
        publisher.onNext(event)
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}