package com.example.projet_android_m2.data

import android.content.Context
import com.example.projet_android_m2.data.db.CardHistory
import com.example.projet_android_m2.data.db.CardHistoryAction
import com.example.projet_android_m2.data.db.PlaceCard
import com.example.projet_android_m2.data.db.PlaceDatabase
import com.example.projet_android_m2.data.db.PlacePersonality
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
import android.util.Log;

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
    val placeCardDao = PlaceDatabase.getInstance(context).placeCardDao()
    val cardHistoryDao = PlaceDatabase.getInstance(context).cardHistoryDao()
    // check securite bug
    val json = Json { ignoreUnknownKeys = true }
    // Function pour call les Dao
    suspend fun clearDao() {
        println("CLEAR DB")
        dao.clearAll()
        placeCardDao.clearAll()
    }
    suspend fun insertDao(places: List<PlacePersonality>) {
        dao.insertAll(places)
    }
    suspend fun countDao(): Long {
        println("COUNT")
        return dao.count()
    }
    suspend fun countCardsDao(): Long {
        return placeCardDao.count()
    }
    suspend fun getPerson(offset: Int, size : Int): Flow<List<PlacePersonality>> {
        return dao.getPerson(offset, size)
    }

    //TODO backend ktorserver ajouter un POST /catch (cardId, userId, score)...
    suspend fun catchCard(cardId: Long, userId: String) = withContext(Dispatchers.IO) {
        placeCardDao.catchCard(cardId, userId)
        cardHistoryDao.insert(CardHistory(cardId = cardId, userId = userId, action = CardHistoryAction.CAPTURED.value))
        println("Carte $cardId attrapée !")
    }
    suspend fun getCardHistory(cardId: Long): List<CardHistory> = withContext(Dispatchers.IO) {
        cardHistoryDao.getHistoryForCard(cardId)
    }
    // TODO SYNCH avec DB local good
    suspend fun transferCard(cardId: Long, toUserId: String) = withContext(Dispatchers.IO) {
        placeCardDao.catchCard(cardId, toUserId)  // met à jour user id de la carte
        cardHistoryDao.insert(
            CardHistory(cardId = cardId, userId = toUserId, action = CardHistoryAction.TRADED.value)
        )
        println("Carte $cardId transférée à $toUserId")
    }

    suspend fun resetAllCatch() = withContext(Dispatchers.IO) {
        placeCardDao.resetAllCatch()
        println("Etats des cartes remises à 0")
    }
    suspend fun getCaughtCards(userId: String): List<PlaceCard> = withContext(Dispatchers.IO) {
        placeCardDao.getUsersCards(userId)
    }
    //TODO: Relation avec le backend pas full local
    suspend fun generatePlaceCards(
        pageSize: Int = 2000,
        status: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {

        val existing = placeCardDao.count()
        val totalPlaces = countDao()
        if (existing >= totalPlaces && existing > 0) {
            status(100, "Cartes déjà générées ($existing cartes)")
            return@withContext Result.success("Cartes déjà générées ($existing cartes)")
        }

        return@withContext try {
            val total = countDao()
            println("DEBUG total places: $total")
            var offset = 0
            var totalGenerated = 0

            status(0, "Génération des cartes... (total lieux : $total)")

            while (true) {
                val page = dao.getPerson(offset, pageSize).first()
                println("DEBUG $offset: ${page.size}")
                if (page.isEmpty()) break

                println("DEBUG avant map")
                val cards = try {
                    page.map { place ->
                        // Rayon max : 20km zone, 500m lieu précis
                        val maxDelta = if (place.zone) 20.0 / 111.0 else 0.0045
                        // Rayon min : évite que la carte soit collée au point d'origine
                        val minDelta = if (place.zone) 2.0 / 111.0 else 0.0009 // 2km min / 100m min
                        // angle aléatoire + distance dans [min, max]
                        val angle = Random.nextDouble(0.0, 2 * Math.PI)
                        val distance = Random.nextDouble(minDelta, maxDelta)
                        val randomLat = place.locationLat + distance * kotlin.math.sin(angle)
                        val randomLon = place.locationLon + distance * kotlin.math.cos(angle)
                        PlaceCard(
                            personId = place.personId,
                            personNameFr = place.personNameFr,
                            personNameEn = place.personNameEn,
                            nameEn = place.nameEn,
                            nameFr = place.nameFr,
                            locationLat = place.locationLat,
                            locationLon = place.locationLon,
                            locationRandomLat = randomLat,
                            locationRandomLon = randomLon,
                            zone = place.zone,
                            iscatch = false,
                            id = place.id
                        )
                    }
                } catch (e: Exception) {
                    println("DEBUG CRASH dans le map : ${e::class.simpleName} - ${e.message}")
                    e.printStackTrace()
                    break
                }
                println("DEBUG après map, cards.size = ${cards.size}")

                placeCardDao.insertAll(cards)
                println("DEBUG  ${cards.size} total card: $totalGenerated")
                val countAfter = placeCardDao.count()
                println("DEBUG placeCardDao.count() : $countAfter")
                totalGenerated += cards.size
                offset += pageSize

                val progress = ((totalGenerated.toFloat() / total) * 100).toInt().coerceIn(0, 99)
                status(progress, "Cartes générées : $totalGenerated / $total")
            }

            status(100, "Génération terminée : $totalGenerated cartes")
            Result.success("$totalGenerated cartes générées")

        } catch (e: Exception) {
            status(0, "Erreur génération cartes : ${e.message}")
            Result.failure(e)
        }
    }

    //Retourne les PlaceCards non attrapées dans un rayon autour du joueur.
    suspend fun getPlaceCardsAroundGps(
        centerLat: Double,
        centerLon: Double,
    ): List<PlaceCard> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val deltaZone = 20.0 / 111.0 // 20km
        val deltaLieu = 0.5 / 111.0 // 500m

        val zones = placeCardDao.getZoneCardsAround(
            minLat = centerLat - deltaZone,
            maxLat = centerLat + deltaZone,
            minLon = centerLon - deltaZone,
            maxLon = centerLon + deltaZone,
            now = now
        )

        val lieux = placeCardDao.getLieuCardsAround(
            minLat = centerLat - deltaLieu,
            maxLat = centerLat + deltaLieu,
            minLon = centerLon - deltaLieu,
            maxLon = centerLon + deltaLieu,
            now = now
        )

        zones + lieux
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
    // Amélioration script ajout dans la Room par chunk perf correct mais assez long encore...
    // TODO:  () Améliorer le script
    // TODO:  () Bug sur télephone quand trop de data !!!
    suspend fun jsonInsertRoomChunk(chunkSize: Int = 1000, status:(current : Int, total : Int, message : String) -> Unit): Result<String> = withContext(Dispatchers.IO) {
        val count = countDao()
        if (count > 0) {
            println("DEJA REMPLIS")
            status(100,100, "Base déjà chargé $count entrées")
            return@withContext Result.success("Base déjà chargé $count entrées")
        }
        val outputRes = mutableListOf<String>()
        val infoInsert = mutableListOf<PlacePersonality>()
        try {
            var indexLigne = 0
            var totalInsert = 0
            var errorCount = 0
            var processLines = 0
            val jsonFile1 = context.assets.open("people-places.jsonl")
            // ouvre le fichier Json
            val bufferReaderBig1 = BufferedReader(InputStreamReader(jsonFile1))
            val totalLines = bufferReaderBig1.useLines{it.count()}
            bufferReaderBig1.close()

            status(0,100,"La machine s'active, total de ligne $totalLines!")
            delay(500)
            val jsonFile2 = context.assets.open("people-places.jsonl")
            val bufferReaderBig2 = BufferedReader(InputStreamReader(jsonFile2))
            status(0,100, "Démarrage du chargement...")

            // linesequence important pour parcourir le fichier sans charger les 900mo d'un coup !
            for (line in bufferReaderBig2.lineSequence()) {
                //if (indexLigne >= chunkSize) break

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
                }else{
                    errorCount++
                }
                indexLigne++
                processLines++

                if(infoInsert.size >= chunkSize){
                    insertDao(infoInsert)
                    totalInsert += infoInsert.size
                    infoInsert.clear()
                    val progressPercent = (processLines*100) / totalLines
                    status(progressPercent,
                        100,
                        "Chargement $progressPercent%\n $totalInsert lieux chargés $progressPercent/$totalLines lignes"
                    )
                        delay(42)
                }
            }

            bufferReaderBig2.close()
            // Insert dans le ROOM
            if (infoInsert.isNotEmpty()) {
                insertDao(infoInsert)
                totalInsert += infoInsert.size
                infoInsert.clear()

                status(100, 100, "Terminé: $totalInsert lieux\n $indexLigne personnalités, \n $errorCount erreurs")
                delay(1000)
            } else {
                status(0,0,"Aucun lieu valide pour insertion")
                outputRes.add(0, "Aucun lieu valide pour insertion")
            }
            kotlinx.coroutines.delay(1000)
            Result.success("Chargés: $totalInsert lieux\n $indexLigne personnalités, \n $errorCount erreurs")

        } catch (e: Exception) {
            status(0,100,"Erreur json -> room${e.localizedMessage}")
            Result.failure(e)
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
    suspend fun getPlacesAroundGps(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double = 2.0
    ): List<PlacePersonality> = withContext(Dispatchers.IO) {

        // Approx 1 degré de latitude radius selon terre
        val deltaLat = radiusKm / 111.0
        val deltaLon = radiusKm / 111.0

        dao.getPlacesAround(
            minLat = centerLat - deltaLat,
            maxLat = centerLat + deltaLat,
            minLon = centerLon - deltaLon,
            maxLon = centerLon + deltaLon
        )
    }
}