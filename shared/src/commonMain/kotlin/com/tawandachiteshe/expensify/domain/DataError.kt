package com.tawandachiteshe.expensify.domain

sealed interface DataError : Error {
    enum class Network : DataError {
        NO_INTERNET,
        REQUEST_TIMEOUT,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        NOT_FOUND,
        TOO_MANY_REQUESTS,
        UNKNOWN
    }
    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}