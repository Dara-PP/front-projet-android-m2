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
import com.example.projet_android_m2.data.db.PlaceCard
import kotlin.math.abs

private const val HITBOX_LIEU = 0.05 // 0.05 ~ 5km, variable globale à centraliser qql part
private const val HITBOX_ZONE = 0.05

// Calcul distance simple en degrés
// Retourne true si la carte est capturable depuis la position user
fun estCapturables(card: PlaceCard, userLat: Double, userLon: Double): Boolean {
    val diffLat = abs(card.locationRandomLat - userLat)
    val diffLon = abs(card.locationRandomLon - userLon)
    val seuil = if (card.zone) HITBOX_ZONE else HITBOX_LIEU
    return diffLat < seuil && diffLon < seuil
}
//TODO()Faire la logique de score minimum avec la DB pour enclencher le gg de la carte
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureBottom(
    userLat: Double,
    userLon: Double,
    cardsAround: List<PlaceCard>,   // toutes les cartes chargées autour du joueur
    onDismiss: () -> Unit,
    onCaptureClick: (PlaceCard) -> Unit // callback -> lance le mini-jeu
) {
    val sheetState = rememberModalBottomSheetState()

    // Filtre les cartes capturable selon la distance user
    val capturable = cardsAround.filter { estCapturables(it, userLat, userLon)}

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
    card: PlaceCard,
    onCaptureClick: () -> Unit
) {
    val nom = card.personNameFr?: card.personNameEn ?: "?"
    val lieu = card.nameFr ?: card.nameEn ?: "?"
    val type = if (card.zone) "Zone" else "Lieu"

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(text = nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = lieu, fontSize = 14.sp, color = Color.DarkGray)
        Text(text = type, fontSize = 12.sp, color = Color.Gray)
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
        PlaceCard(
            personId = 1L,
            personNameFr = "Victor Hugo",
            personNameEn = "Victor Hugo",
            nameFr = "Maison de Victor Hugo",
            nameEn = "Victor Hugo's House",
            locationLat = 48.8566,
            locationLon = 2.3522,
            locationRandomLat = 48.857,
            locationRandomLon = 2.353,
            zone = false,
            iscatch = false,
            id = 101L
        ),
        PlaceCard(
            personId = 2L,
            personNameFr = "Napoléon Bonaparte",
            personNameEn = "Napoleon Bonaparte",
            nameFr = "Champ de bataille d'Austerlitz",
            nameEn = "Battle of Austerlitz",
            locationLat = 49.1,
            locationLon = 2.5,
            locationRandomLat = 48.86,
            locationRandomLon = 2.36,
            zone = true,
            iscatch = false,
            id = 202L
        )
    )

    CaptureBottom(
        userLat = 48.857,
        userLon = 2.353,
        cardsAround = listCards,
        onDismiss = {},
        onCaptureClick = {}
    )
}