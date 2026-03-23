package com.example.projet_android_m2.ui.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.projet_android_m2.data.KtorServer
import com.example.projet_android_m2.data.NearCard
import com.example.projet_android_m2.data.getCardsNear
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val RANGE_KM = 3.0

@Composable
fun OpenStreetMap() {
    val context = LocalContext.current
    val server = remember { KtorServer() }
    val scope = rememberCoroutineScope()

    // Localisation & Caméra
    val locationProvider = rememberDefaultLocationProvider()
    val locationState = rememberUserLocationState(locationProvider)
    val cameraState = rememberCameraState()

    var userLat by remember { mutableDoubleStateOf(-1.0) }
    var userLon by remember { mutableDoubleStateOf(-1.0) }

    // Données
    var cards by remember { mutableStateOf<List<NearCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // États des Sélections (Sheet d'info)
    var selectedCard by remember { mutableStateOf<NearCard?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    //  États du Jeu (Capture)
    var showCaptureList by remember { mutableStateOf(false) }
    var cardToCapture by remember { mutableStateOf<NearCard?>(null) }

    // Fonction de rafraîchissement
    suspend fun refreshCards(lat: Double, lon: Double) {
        if (lat == -1.0 || lon == -1.0) return
        isLoading = true
        try {
            val result = server.getCardsNear(context, lat, lon, RANGE_KM)
            cards = result
        } catch (e: Exception) {
            Log.e("API", "Erreur réseau : ${e.message}")
            Toast.makeText(context, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
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

    // Préparation du GeoJSON
    val circleJson = remember(userLat, userLon) { buildCircleGeoJson(userLat, userLon, RANGE_KM) }
    val cardsJson = remember(cards) { buildCardsGeoJson(cards) }

    // Rendu de l'Interface

    // ÉCRAN DE JEU (Si une carte est en cours de capture)
    if (cardToCapture != null) {
        MiniGameHost(
            game = pickRandomGame(),
            onFinished = { score ->
                scope.launch {
                    if (score >= 1) {
                        Toast.makeText(context, "Félicitations ! Carte capturée.", Toast.LENGTH_LONG).show()
                        refreshCards(userLat, userLon)
                    } else {
                        Toast.makeText(context, "Échec de la capture...", Toast.LENGTH_SHORT).show()
                    }
                    cardToCapture = null // Retour à la carte
                }
            }
        )
    } else {
        // ÉCRAN DE LA CARTE
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(MAP_STYLE),
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
                        // Action optionnelle au clic sur le joueur
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Blue
                )
            }

            Button(
                onClick = { showCaptureList = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                Text("Proximité (${cards.size})", color = Color.White)
            }

            StatsOverlay(
                countCards = cards.size,
                isLoading = isLoading,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            )
        }
    }

    // MODALS (BottomSheet)

    // Info d'un point cliqué sur la carte
    if (showSheet && selectedCard != null) {
        InfoPointSheet(
            card = selectedCard!!,
            onDismiss = { showSheet = false },
            onCaptureClick = {
                cardToCapture = selectedCard
                showSheet = false
            }
        )
    }

    // Liste des cartes capturables (Bouton central)
    if (showCaptureList) {
        CaptureBottom(
            userLat = userLat,
            userLon = userLon,
            cards = cards,
            onDismiss = { showCaptureList = false },
            onCaptureClick = { card ->
                cardToCapture = card
                showCaptureList = false
            }
        )
    }
}