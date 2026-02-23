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

@Serializable
data class AuthLoginUser(
    val id :Int,
    val password: String
)

@Serializable
data class AuthResponse(
    val token : String,
    val message : String
)
class KtorServer {
    private val urlServer = "https://ktor-server-forandroidapp.onrender.com"
    private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            install(Auth){
                bearer{}
            }
        }
    suspend fun login(context : Context, id: String, mdp: String ): String?{
        return try {
            val idInt = id.toInt()
            val response: HttpResponse = client.post("$urlServer/auth"){
                contentType(ContentType.Application.Json)
                setBody(AuthLoginUser(id = idInt, password = mdp))
            }
            // Donne un token maintenant
            if(response.status.value == 200){
                val userData = response.body<AuthResponse>()
                val token = userData.token
                savToken(context, token)
                println(token)
                token
            }else{
                null
            }
        } catch (e: Exception){
            println(e.message)
            null
        }
    }

    fun savToken(context : Context, token: String){
        // Cree le fichier global AuthLog pour toutes les activity et écran, "persistant" tant que pas de logout
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("AUTH_TOKEN", token)
            apply()
        }
    }

    fun getToken(context : Context): String?{
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        return sharedPref.getString("AUTH_TOKEN", null)
    }

    fun logout(context: Context){
        client.clearAuthTokens()
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}