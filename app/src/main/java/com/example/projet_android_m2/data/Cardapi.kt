package com.example.projet_android_m2.data

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

@Serializable
data class NearCard(
    val id: String,
    val wikidata_id: String,
    val person_name: String,
    val lat: Double,
    val lon: Double,
    val power: Int,
    val distance_km: Double
)

suspend fun KtorServer.getCardsNear(
    context: Context,
    lat: Double,
    lon: Double,
    rangeKm: Double = 5.0
): List<NearCard> {
    return try {
        val token = getToken(context) ?: run {
            println("DEBUG CARDS - Pas de token trouvé")
            return emptyList()
        }
        val response = client.get("$urlServer/card/near?lat=$lat&lon=$lon&range=$rangeKm") {
            header("Authorization", "Bearer $token")
        }
        println("DEBUG CARDS - Statut HTTP : ${response.status.value}")
        if (response.status.value == 200) {
            response.body<List<NearCard>>()
        } else {
            val body = response.bodyAsText()
            println("DEBUG CARDS - Erreur serveur : $body")
            emptyList()
        }
    } catch (e: Exception) {
        println("DEBUG CARDS - Erreur réseau : ${e.message}")
        emptyList()
    }
}