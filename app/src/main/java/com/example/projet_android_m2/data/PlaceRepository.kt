package com.example.projet_android_m2.data

import android.content.Context
import com.example.projet_android_m2.data.db.CardHistory
import com.example.projet_android_m2.data.db.CardHistoryAction
import com.example.projet_android_m2.data.db.PlaceDatabase
import com.example.projet_android_m2.data.db.PlacePersonality
import com.example.projet_android_m2.data.db.UserCardEntity
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.collections.mutableListOf
import kotlin.random.Random
import android.util.Log
import io.ktor.client.statement.bodyAsText

@Serializable
data class Payload(
    val id : Long,
    val name : Name,
    val places : List<Place>
)
@Serializable
data class Name(
    // si valeur manquante on met null
    val en : String?= "Null",
    val fr : String?= "Null"
)

@Serializable
data class Place(
    val id : Long,
    val name : Name,
    val location : List<Double>,
    val zone : Boolean
)
data class CarteInfos(
    val personName : String,
    val placeName : String,
    val type : String,
    val lat : Double,
    val lon : Double
)

// DTO pour désérialiser la réponse de GET /api/cards/available
@Serializable
data class AvailableCardDto(
    val id: String,
    val person_name: String,
    val lat: Double,
    val lon: Double,
    val power: Double,
    val acquired_at: String? = null
)

// Désérialiser la réponse de GET /api/cards/sync
@Serializable
data class SyncResponse(val owned_cards: List<AvailableCardDto>)

// Class representing Either<Left|Right>
sealed class Either {
    data class Left(val errorMsg: String) : Either()
    data class Right(val data: Payload) : Either()
}

class PlaceRepository(val context : Context) {
    val dao = PlaceDatabase.getInstance(context).placeDao()
    val cardHistoryDao = PlaceDatabase.getInstance(context).cardHistoryDao()
    val userCardDao = PlaceDatabase.getInstance(context).userCardDao()
    private val server = KtorServer()
    // check securite bug
    val json = Json { ignoreUnknownKeys = true }
    // Function pour call les Dao
    suspend fun clearDao() {
        println("CLEAR DB")
        dao.clearAll()
    }
    suspend fun countDao(): Long {
        println("COUNT")
        return dao.count()
    }
    suspend fun getPerson(offset: Int, size : Int): Flow<List<PlacePersonality>> {
        return dao.getPerson(offset, size)
    }


    suspend fun checkInit(status:(String) -> Unit): List<CarteInfos> = withContext(Dispatchers.IO) {
        val count = countDao()
        if (count > 0) {
            println("DEJA REMPLIS")
            status("Données recupérées de la base ROOM")
            return@withContext getData()
        }
        println("NEW DB INIT")
        status("Base vide")
        return@withContext emptyList()
    }

    suspend fun getData(limit: Int = 10): List<CarteInfos> = withContext(Dispatchers.IO) {
        val allPlace = getPerson(offset = 0,limit).first()
        println("GET DATA")
        allPlace.map{place ->
            CarteInfos(
                personName = place.personNameFr ?: place.personNameEn ?: "Null",
                placeName = place.nameFr ?: place.nameEn ?: "Null",
                type = if(place.zone) "Zone" else "Lieu",
                lat = place.locationLat,
                lon = place.locationLon
            )
        }
    }

    //TODO la synchronisation est peut etre parfois pas tres rapide, peut etre faire un sorte de cache server ??
    // bloque la carte apres fail a faire aussi
    // Envoie POST /api/cards/capture :
    //   1. Lit le nom depuis le cache avant de supprimer
    //   2. Supprime la carte du cache local
    //   3. Écrit une entrée dans card_history
    suspend fun captureCard(cardId: String): Boolean = withContext(Dispatchers.IO) {
        val cached = userCardDao.getById(cardId)
        val success = server.captureCard(context, cardId)
        if (success) {
            userCardDao.deleteById(cardId)
            val userId = server.getUsername(context) ?: ""
            cardHistoryDao.insert(
                CardHistory(
                    cardId = cardId,
                    personName = cached?.person_name,
                    userId = userId,
                    action = CardHistoryAction.CAPTURED.value
                )
            )
            Log.d("CAPTURE", "Carte $cardId capturée, historique local get")
        }
        success
    }

    // Synchronise card_history depuis GET /api/cards/sync (cartes possédées par l'user).
    suspend fun syncHistoryFromServer(): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val userId = server.getUsername(context)
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))
            val token = server.getToken(context)
                ?: return@withContext Result.failure(Exception("Pas de token"))

            val response = server.client.get("${server.urlServer}/api/cards/sync") {
                header("Authorization", "Bearer $token")
            }
            val statusCode = response.status.value
            Log.d("SYNC", "syncHistoryFromServer status=$statusCode")

            if (statusCode != 200) {
                val body = response.bodyAsText()
                Log.e("SYNC", "syncHistoryFromServer erreur $statusCode: $body")
                return@withContext Result.failure(Exception("Erreur serveur $statusCode: $body"))
            }

            val syncResponse = response.body<SyncResponse>()
            val dtos = syncResponse.owned_cards
            Log.d("SYNC", "syncHistoryFromServer reçu ${dtos.size} carte(s) depuis /api/cards/sync")

            val entities = dtos.map { dto ->
                CardHistory(
                    cardId = dto.id,
                    personName = dto.person_name,
                    userId = userId,
                    action = CardHistoryAction.CAPTURED.value
                )
            }
            cardHistoryDao.clearForUser(userId)
            cardHistoryDao.insertAll(entities)
            Log.d("SYNC", "Historique synchronisé")
            Result.success(entities.size)
        } catch (e: Exception) {
            Log.e("SYNC", "syncHistoryFromServer error: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }


    // Récupère les cartes disponibles depuis l'API et les met en cache dans Room.
    // Retourne la liste sauvegardée, ou la liste déjà en cache en cas d'erreur réseau.
    suspend fun fetchAndSaveAvailableCards(
        lat: Double,
        lon: Double,
        rangeKm: Double = 5.0
    ): List<UserCardEntity> = withContext(Dispatchers.IO) {
        val token = server.getToken(context)
        if (token == null) {
            Log.w("CARDS", "fetchAndSaveAvailableCards : pas de token, retour cache Room")
            return@withContext userCardDao.getAll().first()
        }
        try {
            val response = server.client.get("${server.urlServer}/api/cards/available") {
                header("Authorization", "Bearer $token")
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("range", rangeKm)
            }
            val statusCode = response.status.value
            Log.d("CARDS", "fetchAndSaveAvailableCards status=$statusCode")
            if (statusCode == 200) {
                val dtos = response.body<List<AvailableCardDto>>()
                Log.d("CARDS", "fetchAndSaveAvailableCards reçu ${dtos.size} cartes")
                val entities = dtos.map { dto ->
                    UserCardEntity(
                        id = dto.id,
                        person_name = dto.person_name,
                        lat = dto.lat,
                        lon = dto.lon,
                        power = dto.power,
                        acquired_at = dto.acquired_at
                    )
                }
                userCardDao.replaceAll(entities)
                entities
            } else {
                Log.w("CARDS", "fetchAndSaveAvailableCards erreur HTTP $statusCode, retour cache")
                userCardDao.getAll().first()
            }
        } catch (e: Exception) {
            Log.e("CARDS", "fetchAndSaveAvailableCards exception: ${e.message}")
            userCardDao.getAll().first()
        }
    }
}