package com.example.projet_android_m2.ui.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.KtorServer
import com.example.projet_android_m2.data.NearCard
import com.example.projet_android_m2.data.PlaceRepository
import com.example.projet_android_m2.data.db.UserCardEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import org.maplibre.spatialk.geojson.Position

private const val RANGE_KM = 5.0

// Convertit une UserCardEntity (cache API) en NearCard pour l'affichage sur la carte.
fun UserCardEntity.toNearCard(userLat: Double, userLon: Double): NearCard {
    val dLat = lat - userLat
    val dLon = lon - userLon
    val distKm = sqrt(dLat * dLat + dLon * dLon) * 111.0
    return NearCard(
        id = id,
        wikidata_id = "",
        person_name = person_name,
        lat = lat,
        lon = lon,
        power = power.toInt(),
        distance_km = distKm
    )
}

@Composable
fun OpenStreetMap() {
    val context = LocalContext.current
    val server = remember { KtorServer() }
    val repo = remember { PlaceRepository(context) }
    val scope = rememberCoroutineScope()
    val userId = server.getUsername(context) ?: ""

    // Localisation & Caméra
    val locationProvider = rememberDefaultLocationProvider()
    val locationState = rememberUserLocationState(locationProvider)
    val cameraState = rememberCameraState()

    var userLat by remember { mutableDoubleStateOf(-1.0) }
    var userLon by remember { mutableDoubleStateOf(-1.0) }

    // Données
    var cards by remember { mutableStateOf<List<NearCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showUserStats by remember { mutableStateOf(false) }

    // États des Sélections (Sheet d'info)
    var selectedCard by remember { mutableStateOf<NearCard?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    //  États du Jeu (Capture)
    var showCaptureList by remember { mutableStateOf(false) }
    var cardToCapture by remember { mutableStateOf<NearCard?>(null) }
    var currentGame by remember { mutableStateOf(pickRandomGame()) }

    // Appelle GET /api/cards/available, met en cache Room, puis affiche les marqueurs.
    // Si le résultat est vide (offline + cache vide), conserve les cartes déjà affichées.
    suspend fun refreshCards(lat: Double, lon: Double) {
        if (lat == -1.0 || lon == -1.0) return
        isLoading = true
        try {
            val entities = repo.fetchAndSaveAvailableCards(lat, lon, RANGE_KM)
            if (entities.isNotEmpty()) {
                cards = entities.map { it.toNearCard(lat, lon) }
            }
            Log.d("CARDS", "Total affiché : ${cards.size} cartes")
        } catch (e: Exception) {
            Log.e("CARDS", "Erreur chargement cartes : ${e.message}")
            Toast.makeText(context, "Erreur chargement des cartes", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // Charge le cache Room immédiatement au démarrage (offline ou avant que la localisation arrive)
    LaunchedEffect(Unit) {
        val cached = repo.userCardDao.getAll().first()
        if (cached.isNotEmpty()) {
            cards = cached.map { it.toNearCard(userLat, userLon) }
        }
    }

    // Surveillance de la position
    LaunchedEffect(Unit) {
        snapshotFlow { locationState.location }
            .filterNotNull()
            .distinctUntilChanged { old, new ->
                val diffLat = abs(old.position.latitude - new.position.latitude)
                val diffLon = abs(old.position.longitude - new.position.longitude)
                diffLat < 0.0001 && diffLon < 0.0001
            }
            .collectLatest { userLoc ->
                userLat = userLoc.position.latitude
                userLon = userLoc.position.longitude

                refreshCards(userLat, userLon)

                cameraState.animateTo(
                    finalPosition = CameraPosition(target = Position(userLon, userLat), zoom = 14.0),
                    duration = 1000.milliseconds
                )
            }
    }

    // Préparation GeoJSON
    val circleJson = remember(userLat, userLon) { buildCircleGeoJson(userLat, userLon, RANGE_KM) }
    val cardsJson = remember(cards) { buildCardsGeoJson(cards) }

    // Rendu de l'Interface

    // ÉCRAN DE JEU (Si une carte est en cours de capture)
    if (cardToCapture != null) {
        val frozenCard = cardToCapture
        MiniGameHost(
            game = currentGame, // stable ne change pas à chaque recomposition
            onFinished = { score ->
                Log.d("DEBUG_CLICK", "MiniGame finished with score: $score | card=${frozenCard?.id}")
                scope.launch {
                    if (score >= 0 && frozenCard != null) {
                        Log.d("CAPTURE", "Starting capture process for ${frozenCard.id}")
                        val captured = repo.captureCard(frozenCard.id)
                        if (captured) {
                            Toast.makeText(context, "${frozenCard.person_name} capturée !", Toast.LENGTH_LONG).show()
                            refreshCards(userLat, userLon)
                        } else {
                            Log.e("CAPTURE", "Le serveur a refusé la capture de ${frozenCard.id}")
                            Toast.makeText(context, "Erreur serveur, capture échouée", Toast.LENGTH_SHORT).show()
                        }
                    } else if (score < 1) {
                        Toast.makeText(context, "Échec de la capture...", Toast.LENGTH_SHORT).show()
                    }
                    cardToCapture = null // Retour à la carte
                }
            }
        )
        return // Plein écran jeu
    }

    // ÉCRAN DE LA CARTE
    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
        ) {
            MapContent(
                cardsJson = cardsJson,
                circleJson = circleJson,
                locationState = locationState,
                cameraState = cameraState,
                onPointClick = { clickedId ->
                    // On nettoie les éventuels guillemets du JSON et on cherche l'ID
                    val cleanId = clickedId.replace("\"", "")
                    val found = cards.find { it.id == cleanId }

                    if (found != null) {
                        selectedCard = found
                        showSheet = true
                    } else {
                        Log.e("MapClick", "ID inconnu : $cleanId")
                    }
                },
                onUserPuckClick = {
                    // TODO : afficher stats joueur car l'autre était nul
                    showUserStats = true
                    // Action optionnelle au clic sur le joueur
                }
            )
        }

        // Loader
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Blue
            )
        }

        // Overlay stats en haut
        StatsOverlay(
            countCards = cards.size,
            isLoading = isLoading,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Bouton capture (centre bas)
        Button(
            onClick = { showCaptureList = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            Text("Proximité (${cards.size})", color = Color.White, fontSize = 16.sp)
        }

        // Bouton refresh
        Button(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 80.dp),
            onClick = {
                scope.launch { refreshCards(userLat, userLon) }
            }
        ) {
            Text("Refresh", color = Color.White)
        }
    }
    // Stats joueur
    // TODO Vrai stats du joueur /me en wip
    if (showUserStats) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUserStats = false },
            confirmButton = {
                Button(onClick = { showUserStats = false }) { Text("OK") }
            },
            title = { Text("Profil de $userId") },
            text = {
                Column {
                    Text("Captures locales : ${cards.size}") // Juste pour afficher un truc
                    Text("Position : $userLat, $userLon")
                }
            }
        )
    }

    // MODALS (BottomSheet)

    // Info d'un point cliqué sur la carte
    if (showSheet && selectedCard != null) {
        InfoPointSheet(
            card = selectedCard!!,
            onDismiss = { showSheet = false },
            onCaptureClick = {
                currentGame = pickRandomGame()
                cardToCapture = selectedCard
                showSheet = false
            },
            onInstantCapture = {
                val card = selectedCard!!
                showSheet = false
                scope.launch {
                    val captured = repo.captureCard(card.id)
                    if (captured) {
                        Toast.makeText(context, "${card.person_name} capturée !", Toast.LENGTH_LONG).show()
                        refreshCards(userLat, userLon)
                    } else {
                        Log.e("DEBUG_INSTANT", "Capture refusée par le serveur pour ${card.id}")
                        Toast.makeText(context, "Erreur serveur, capture échouée", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Liste des cartes capturables (bouton central)
    if (showCaptureList) {
        CaptureBottom(
            userLat = userLat,
            userLon = userLon,
            cards = cards,
            onDismiss = { showCaptureList = false },
            onCaptureClick = { card ->
                currentGame = pickRandomGame()
                cardToCapture = card
                showCaptureList = false
            }
        )
    }
}
@Preview
@Composable
fun TestMap(){
    OpenStreetMap()
}
