package com.festfive.app.model.error

/*
*Created by Nhat.vo on 11/14/2019.
*/

enum class CommonError(var value: Int) {
    NETWORK_ERROR(1),
    RESULT_ERROR(2),
    SERVER_ERROR(3),
    UNAUTHENTICATED(4),
    BAD_REQUEST(5),
    UNKNOWN(8);

    fun fromInt(i: Int): CommonError {
        for (messageType in values()) {
            if (messageType.value == i) {
                return messageType
            }
        }
        return UNKNOWN
    }
}