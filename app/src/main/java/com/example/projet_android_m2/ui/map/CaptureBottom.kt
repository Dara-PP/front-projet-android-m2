package com.example.projet_android_m2.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.NearCard
import com.example.projet_android_m2.data.db.PlaceCard
import kotlin.math.abs

private const val HITBOX_LIEU = 0.05 // 0.05 ~ 5km, variable globale à centraliser qql part
private const val HITBOX_ZONE = 0.05
private const val HITBOX_KM   = 5.0   // Seuil en km pour les NearCard (API)

// Calcul distance simple en degrés
// Retourne true si la carte est capturable depuis la position user
fun estCapturables(card: PlaceCard, userLat: Double, userLon: Double): Boolean {
    val diffLat = abs(card.locationRandomLat - userLat)
    val diffLon = abs(card.locationRandomLon - userLon)
    val seuil = if (card.zone) HITBOX_ZONE else HITBOX_LIEU
    return diffLat < seuil && diffLon < seuil
}
//TODO()Faire la logique de score minimum avec la DB pour enclencher le gg de la carte
//  API Utilisée quand les cartes viennent du backend (NearCard)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureBottom(
    userLat: Double,
    userLon: Double,
    cards: List<NearCard>, // toutes les cartes chargées autour du joueur
    onDismiss: () -> Unit,
    onCaptureClick: (NearCard) -> Unit // callback -> lance le mini-jeu
) {
    val sheetState = rememberModalBottomSheetState()
    // L'API retourne déjà distance_km on filtre directement
    val capturable = cards.filter { it.distance_km <= HITBOX_KM }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

            Text(
                text = "Cartes à portée",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${capturable.size} carte(s) capturable(s)",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (capturable.isEmpty()) {
                Text(
                    text = "Aucune carte à portée !",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn {
                    items(capturable) { card ->
                        CaptureCardItem(
                            card = card,
                            onCaptureClick = {
                                onCaptureClick(card)
                                onDismiss()
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
fun CaptureCardItem(
    card: NearCard,
    onCaptureClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(text = card.person_name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = "%.0f m".format(card.distance_km * 1000),
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Text(
            text = "Power : ${card.power}",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onCaptureClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Attraper !", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestCaptureBottom() {
    val listCards = listOf(
        NearCard(
            id = "101",
            wikidata_id = "Q535",
            person_name = "Victor Hugo",
            lat = 48.857,
            lon = 2.353,
            power = 42,
            distance_km = 0.3
        ),
        NearCard(
            id = "202",
            wikidata_id = "Q517",
            person_name = "Napoléon Bonaparte",
            lat = 48.86,
            lon = 2.36,
            power = 78,
            distance_km = 1.2
        )
    )
    CaptureBottom(
        userLat = 48.857,
        userLon = 2.353,
        cards = listCards,
        onDismiss = {},
        onCaptureClick = {}
    )
}