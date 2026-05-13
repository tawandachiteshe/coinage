package com.tawandachiteshe.coinage

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform