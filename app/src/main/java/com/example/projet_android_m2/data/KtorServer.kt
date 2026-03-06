package com.example.projet_android_m2.data

import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// -------------------------
// DATA CLASSES
// -------------------------
@Serializable
data class AuthLoginUser(
    val id: Int,
    val password: String
)

@Serializable
data class RegisterUser(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val message: String
)

@Serializable
data class Card(
    val id: Int? = null,           // optionnel, généré par le serveur
    val name: String,
    val rarity: String,
    val type: String,
    val power: Int,
    val ownerId: String
)

@Serializable
data class CardResponse(
    val cards: List<Card>
)

// -------------------------
// KTOR SERVER CLASS
// -------------------------
class KtorServer {

    private val urlServer = "https://ktor-server-forandroidapp.onrender.com"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
        install(Auth) { bearer {} }
    }

    // -------------------------
    // LOGIN
    // -------------------------
    suspend fun login(context: Context, id: String, mdp: String): String? {
        return try {
            val idInt = id.toInt()
            val response: HttpResponse = client.post("$urlServer/auth") {
                contentType(ContentType.Application.Json)
                setBody(AuthLoginUser(id = idInt, password = mdp))
            }

            if (response.status.value == 200) {
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token, id)
                println("Token après login: $token")
                token
            } else null
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            null
        }
    }

    // -------------------------
    // REGISTER
    // -------------------------
    suspend fun register(context: Context, username: String, mdp: String): String? {
        return try {
            val response: HttpResponse = client.post("$urlServer/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterUser(username = username, password = mdp))
            }

            if (response.status.value == 200) {
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token, username)
                println("Token après register: $token")
                token
            } else null
        } catch (e: Exception) {
            println("Register error: ${e.message}")
            null
        }
    }

    // -------------------------
    // GET USER INFO
    // -------------------------
    suspend fun me(context: Context): String? {
        return try {
            val token = getToken(context)
            val response: HttpResponse = client.get("$urlServer/me") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.value == 200) {
                val body = response.body<String>()
                println("User info: $body")
                body
            } else null
        } catch (e: Exception) {
            println("Get user info error: ${e.message}")
            null
        }
    }

    // -------------------------
    // SAVE TOKEN / USERNAME
    // -------------------------
    fun savToken(context: Context, token: String, username: String) {
        val sharedPref = context.getSharedPreferences("AuthLog", Context.MODE_PRIVATE)
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

    // -------------------------
    // SAUVEGARDE CARTE CAPTUREE
    // -------------------------
    fun saveCapturedCard(context: Context, card: Card) {
        val token = getToken(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.post("$urlServer/cards") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                    setBody(card)
                }

                if (response.status.value == 200) {
                    println("Carte sauvegardée sur le serveur ✅")
                } else {
                    println("Erreur serveur: ${response.status.value}")
                }
            } catch (e: Exception) {
                println("Erreur Ktor: ${e.message}")
            }
        }
    }

    // -------------------------
    // RECUPERER LES CARTES D'UN UTILISATEUR
    // -------------------------
    suspend fun getUserCards(context: Context): List<Card> {
        val token = getToken(context) ?: return emptyList()
        return try {
            val response: CardResponse = client.get("$urlServer/cards") {
                header("Authorization", "Bearer $token")
            }.body<CardResponse>()

            response.cards
        } catch (e: Exception) {
            println("Erreur récupération cartes: ${e.message}")
            emptyList()
        }
    }
}