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
import io.ktor.client.statement.bodyAsText

@Serializable
data class AuthLoginUser(
    val id :Int,
    val password: String
)
@Serializable
data class RegisterUser(
    val username : String,
    val password: String,
    val email : String
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
                // backend à changer pour le login !
                savToken(context, token, id) // temporaire
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
    // TODO Check le server 404 server erreur register marche pas
    suspend fun register(context : Context, username: String, mdp: String, email: String ): String?{
        return try {
            val response: HttpResponse = client.post("$urlServer/users"){
                contentType(ContentType.Application.Json)
                setBody(RegisterUser(username = username, password = mdp, email = email ))
            }
            // Donne un token maintenant
            // TODO Changement server a faire pour token ?
            val statusCode = response.status.value
            val body = response.bodyAsText()
            println("DEBUG STATUS SERVER : $statusCode")
            println("DEBUG BODY SERVER : $body")
            if(response.status.value == 200){
                //val userData = response.body<AuthResponse>()
                //val token = userData.token
                //println("Token apres register : $token")
                //token
                val body = response.bodyAsText()
                savToken(context, body, username)
                body
            }else{
                null
            }
        } catch (e: Exception){
            println(e.message)
            null
        }
    }

    // Get les informations de l'utilisateur // temporaire !!
    suspend fun me(context : Context): String?{
        return try {
            val token = getToken(context)
            val response: HttpResponse = client.get("$urlServer/me"){
                header("Authorization", "Bearer $token")
            }
            // Donne un token maintenant
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
        // Cree le fichier global AuthLog pour toutes les activity et écran, "persistant" tant que pas de logout
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
        //client.clearAuthTokens() // check si c'est ok
        val sharedPref = context.getSharedPreferences("AuthLog",Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}