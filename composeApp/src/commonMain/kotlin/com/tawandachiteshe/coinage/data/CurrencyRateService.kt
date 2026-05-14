package com.tawandachiteshe.coinage.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RatesResponse(
    val result: String,
    @SerialName("rates") val rates: Map<String, Double>,
    @SerialName("time_last_update_unix") val updatedAt: Long = 0L,
)

class CurrencyRateService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchRatesVsUsd(): Map<String, Double> {
        val response = client.get("https://open.er-api.com/v6/latest/USD")
            .body<RatesResponse>()
        if (response.result != "success") error("Rate fetch failed: ${response.result}")
        return response.rates
    }
}