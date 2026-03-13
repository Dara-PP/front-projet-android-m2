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
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.request.header

@Serializable
data class AuthLoginUser(
    val username: String,
    val password: String
)

@Serializable
data class RegisterUser(
    val username : String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token : String,
    val message : String
)

class KtorServer {
    private val urlServer = "http://10.0.2.2:8080"
    // private val urlServer = "https://ktor-server-forandroidapp.onrender.com"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Auth){
            bearer{}
        }
    }

    suspend fun login(context : Context, username: String, mdp: String ): String?{
        return try {
            val response: HttpResponse = client.post("$urlServer/auth"){
                contentType(ContentType.Application.Json)
                setBody(AuthLoginUser(username = username, password = mdp))
            }

            if(response.status.value == 200){
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token, username)
                println(token)
                token
            }else{
                println("Erreur HTTP: ${response.status}")
                null
            }
        } catch (e: Exception){
            println("ERREUR D'ENVOI KTOR: ${e.message}")
            null
        }
    }

    suspend fun register(context : Context, username: String, mdp: String ): String?{
        return try {
            val response: HttpResponse = client.post("$urlServer/register"){
                contentType(ContentType.Application.Json)
                setBody(RegisterUser(username = username, password = mdp))
            }
            if(response.status.value == 200){
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token, username)
                println("Token apres register : $token")
                token
            }else{
                null
            }
        } catch (e: Exception){
            println(e.message)
            null
        }
    }

    suspend fun me(context : Context): String?{
        return try {
            val token = getToken(context)
            val response: HttpResponse = client.get("$urlServer/me"){
                header("Authorization", "Bearer $token")
            }
            if(response.status.value == 200){
                val body = response.body<String>()
                println(body)
                body
            }else{
                null
            }
        } catch (e: Exception){
            println(e.message)
            null
        }
    }

    fun savToken(context : Context, token: String, username: String){
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("AUTH_TOKEN", token)
            putString("USERNAME", username)
            apply()
        }
    }

    fun getToken(context : Context): String?{
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        return sharedPref.getString("AUTH_TOKEN", null)
    }

    fun getUsername(context : Context): String?{
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        return sharedPref.getString("USERNAME", null)
    }

    fun logout(context: Context){
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}