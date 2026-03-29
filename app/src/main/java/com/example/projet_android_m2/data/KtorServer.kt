package com.example.projet_android_m2.data

import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.*
import io.ktor.client.call.*

@Serializable
data class AuthLoginUser(
    val username: String,
    val password: String
)

@Serializable
data class RegisterUser(
    val username: String,
    val password: String,
    val email: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val message: String
)

@Serializable
data class CaptureRequest(val card_id: String)

@Serializable
data class CardHistoryDto(
    val card_id: String,
    val person_name: String? = null,
    val action: Int = 0,
    val acquired_at: String? = null
)


class KtorServer {
    // internal : accessible depuis les fichiers d'extension du même module
    //internal val urlServer = "http://10.0.2.2:8080"
    internal val urlServer = "https://ktor-server-forandroidapp.onrender.com"

    internal val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun login(context: Context, username: String, mdp: String): String? {
        return try {
            val response: HttpResponse = client.post("$urlServer/auth") {
                contentType(ContentType.Application.Json)
                setBody(AuthLoginUser(username = username, password = mdp))
            }
            if (response.status.value == 200) {
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token, username)
                println(token)
                token
            } else {
                println("Erreur HTTP: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("ERREUR D'ENVOI KTOR: ${e.message}")
            null
        }
    }

    // TODO Check le server 404 server erreur register marche pas
    suspend fun register(context: Context, username: String, mdp: String, email: String): String? {
        return try {
            val response: HttpResponse = client.post("$urlServer/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterUser(username = username, password = mdp, email = email))
            }
            val statusCode = response.status.value
            val body = response.bodyAsText()
            println("DEBUG STATUS SERVER : $statusCode")
            println("DEBUG BODY SERVER : $body")
            if (response.status.value == 200) {
                savToken(context, body, username)
                body
            } else {
                null
            }
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }

    suspend fun me(context: Context): String? {
        return try {
            val token = getToken(context)
            val response: HttpResponse = client.get("$urlServer/me") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.value == 200) {
                val body = response.body<String>()
                println(body)
                body
            } else {
                null
            }
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }

    fun savToken(context: Context, token: String, username: String) {
        val sharedPref = context.getSharedPreferences("AuthLog", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("AUTH_TOKEN", token)
            putString("USERNAME", username)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("AuthLog", Context.MODE_PRIVATE)
        return sharedPref.getString("AUTH_TOKEN", null)
    }

    fun getUsername(context: Context): String? {
        val sharedPref = context.getSharedPreferences("AuthLog", Context.MODE_PRIVATE)
        return sharedPref.getString("USERNAME", null)
    }

    fun logout(context: Context) {
        val sharedPref = context.getSharedPreferences("AuthLog", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }

    suspend fun captureCard(context: Context, cardId: String): Boolean {
        return try {
            val token = getToken(context) ?: run {
                return false
            }
            val url = "$urlServer/api/cards/capture"
            val requestBody = CaptureRequest(card_id = cardId)
            val response: HttpResponse = client.post(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            response.status == io.ktor.http.HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCardHistory(context: Context): List<CardHistoryDto> {
        return try {
            val token = getToken(context) ?: run {
                return emptyList()
            }
            val response: HttpResponse = client.get("$urlServer/api/cards/history") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.value == 200) {
                response.body<List<CardHistoryDto>>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPantheon(context: Context): List<PantheonPlayerResponse> {
        return try {
            val token = getToken(context)
            println("DEBUG TOKEN ENVOYÉ : $token")
            if (token != null) {
                val response = client.get("$urlServer/pantheon") {
                    header("Authorization", "Bearer $token")
                }
                println("DEBUG PANTHEON - Statut HTTP : ${response.status.value}")
                val texteBrut = response.bodyAsText()
                println("DEBUG PANTHEON - Réponse brute du serveur : $texteBrut")
                if (response.status.value == 200) {
                    kotlinx.serialization.json.Json.decodeFromString(texteBrut)
                } else {
                    emptyList()
                }
            } else {
                println("DEBUG PANTHEON - Pas de token trouvé !")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG PANTHEON - Erreur totale (crash réseau) : ${e.message}")
            emptyList()
        }
    }
}
