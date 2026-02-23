package com.example.projet_android_m2

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class AuthLoginUser(
    val id :Int,
    val password: String
)
class KtorServer {
    private val urlServer = "https://ktor-server-forandroidapp.onrender.com"
    private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    suspend fun login(id: String, mdp: String ): Boolean{
        return try {
            val idInt = id.toInt()
            val response: HttpResponse = client.post("$urlServer/auth"){
                contentType(ContentType.Application.Json)
                setBody(AuthLoginUser(id = idInt, password = mdp))
            }
            response.status.value == 200
        } catch (e: Exception){
            println(e.message)
            false
        }
    }
}