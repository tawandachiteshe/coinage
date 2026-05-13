package com.tawandachiteshe.expensify.core.presentation

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(val id: String, val args: List<Any> = emptyList()) : UiText
}