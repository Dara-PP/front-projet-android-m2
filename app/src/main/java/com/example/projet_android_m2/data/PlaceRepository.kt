package com.example.projet_android_m2.data

import android.content.Context
import com.example.projet_android_m2.PlaceDatabase
import com.example.projet_android_m2.PlacePersonality
import kotlinx.coroutines.Dispatchers
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

// Class representing Either<Left|Right>
sealed class Either {
    data class Left(val errorMsg: String) : Either()
    data class Right(val data: Payload) : Either()
}

class PlaceRepository(val context : Context) {
    val dao = PlaceDatabase.getInstance(context).placeDao()

    // check securite bug
    val json = Json { ignoreUnknownKeys = true }
    // Function pour call les Dao
    suspend fun clearDao() {
        println("CLEAR DB")
        dao.clearAll()
    }
    suspend fun insertDao(places: List<PlacePersonality>) {
        dao.insertAll(places)
    }
    suspend fun countDao(): Long {
        println("COUNT")
        return dao.count()
    }
    suspend fun getPerson(offset: Int, size : Int): Flow<List<PlacePersonality>> {
        return dao.getPerson(offset, size)
    }

    // Check si la base à déja les data. Context(dispatcher.io) pour pas bloquer ui
    suspend fun checkInit(status:(String) -> Unit): List<CarteInfos> = withContext(Dispatchers.IO) {
        val count = countDao()
        if (count > 0) {
            println("DEJA REMPLIS")
            status("Données recupérées de la base ROOM")
            return@withContext getData()
        }
        //Sans Room test json seul
        //JsonRead(0,4)
        //Avec Room
        println("NEW DB INIT")
        status("Base vide")
        jsonInsertRoom(status = status)
        return@withContext getData()
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
    // Lecture Json et input dans Room Dao, test simple pour l'instant avec limit
    private suspend fun jsonInsertRoom(limit: Int = 2, status:(String) -> Unit): List<String> = withContext(Dispatchers.IO) {
        val outputRes = mutableListOf<String>()
        val infoInsert = mutableListOf<PlacePersonality>()

        try {
            var indexLigne = 0
            val jsonFile = context.assets.open("people-places.jsonl")
            // ouvre le fichier Json
            val bufferReaderBig = BufferedReader(InputStreamReader(jsonFile))
            // linesequence important pour parcourir le fichier sans charger les 900mo d'un coup !
            for (line in bufferReaderBig.lineSequence()) {
                if (indexLigne >= limit) break

                // Convertie la ligne en JsonElement
                val elementJson = json.parseToJsonElement(line)

                // verifie si erreur dans le payload
                val erreur = elementJson.jsonObject["error"]?.jsonPrimitive?.content
                if (erreur == null) {
                    val payload = json.decodeFromJsonElement(Payload.serializer(), elementJson)
                    val placePerson = payload.places.map { place ->
                        PlacePersonality(
                            personId = payload.id,
                            personNameEn = payload.name.en,
                            personNameFr = payload.name.fr,
                            nameEn = place.name.en,
                            nameFr = place.name.fr,
                            relationId = null,
                            relationNameEn = null,
                            relationNameFr = null,
                            locationLat = place.location.getOrNull(0) ?: 0.0,
                            locationLon = place.location.getOrNull(1) ?: 0.0,
                            zone = place.zone,
                            id = place.id
                        )
                    }
                    infoInsert.addAll(placePerson)
                    outputRes.add("Personne ID ${payload.id} avec $placePerson lieux")
                }
                indexLigne++
            }
            bufferReaderBig.close()
            // Insert dans le ROOM
            if (infoInsert.isNotEmpty()) {
                insertDao(infoInsert)
                val msg = "Succes ${infoInsert.size} lieux insérer dans la ROOM"
                status(msg)
            } else {
                status("Aucun lieu valide pour insertion")
                outputRes.add(0, "Aucun lieu valide pour insertion")
            }
            return@withContext outputRes
        } catch (e: Exception) {
            status("Erreur json -> room${e.localizedMessage}")
            return@withContext listOf("Erreur json -> room${e.localizedMessage}")
        }
    }

    // Lecture sans Room du Json
    fun JsonRead(numeroPage: Int, taillePage: Int = 10, ): List<String> {

        val outputRes = mutableListOf<String>()
        val debut = numeroPage * taillePage
        val fin = debut + taillePage

        return try {
            var indexLigne = 0
            val jsonFile = context.assets.open("people-places.jsonl")
            //val jsonFileLite = context.assets.open("lite.jsonl")
            // ouvre le fichier Json
            val bufferReaderBig = BufferedReader(InputStreamReader(jsonFile))
            //val bufferReaderLite = BufferedReader(InputStreamReader(jsonFileLite))
            // linesequence important pour parcourir le fichier sans charger les 900mo d'un coup !
            for (line in bufferReaderBig.lineSequence()){
                if (indexLigne >= debut && indexLigne < fin) {
                    // Convertie la ligne en JsonElement
                    val elementJson = json.parseToJsonElement(line)

                    // verifie si erreur dans le payload
                    val resultat: Either
                    val erreur = elementJson.jsonObject["error"]?.jsonPrimitive?.content
                    if (erreur != null) {
                        resultat = Either.Left(erreur)
                    } else {
                        resultat =
                            Either.Right(json.decodeFromJsonElement(Payload.serializer(), elementJson))
                    }
                    outputRes.add(resultat.toString())
                }
                if (indexLigne >= fin-1) {
                    break
                }
                indexLigne++
            }
            bufferReaderBig.close()
            outputRes
        } catch (e: Exception){
            listOf("Erreur: ${e.message}")
        }
    }
}
