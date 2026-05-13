package com.tawandachiteshe.expensify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform