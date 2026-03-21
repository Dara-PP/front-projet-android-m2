package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.db.PlaceCard
import com.example.projet_android_m2.data.PlaceRepository

@Composable
fun CardList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    var userCards by remember { mutableStateOf<List<PlaceCard>>(emptyList()) }

    LaunchedEffect(Unit) {
        userCards = repo.getCaughtCards()
    }
    CardList(cards = userCards, modifier = modifier)
}

@Composable
fun CardList(
    cards: List<PlaceCard> = emptyList(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Ma Collection",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "${cards.size} carte(s) collectée(s)",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cards.isEmpty()) {
            Text(
                text = "Aucune carte pour l'instant.",
                color = Color.Gray
            )
        } else {
            LazyColumn {
                items(cards) { card ->
                    CaughtCardItem(card = card)
                }
            }
        }
    }
}

@Composable
fun CaughtCardItem(card: PlaceCard) {
    val nom = card.personNameFr ?: card.personNameEn ?: "?"
    val lieu = card.nameFr ?: card.nameEn ?: "?"
    val type = if (card.zone) "Zone" else "Lieu"

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = lieu, fontSize = 14.sp, color = Color.DarkGray)
            Text(text = type, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestListCard() {
    val listCards = listOf(
        PlaceCard(
            personId = 1L, personNameFr = "Victor Hugo", personNameEn = "Victor Hugo",
            nameFr = "Maison de Victor Hugo", nameEn = "Victor Hugo's House",
            locationLat = 48.854, locationLon = 2.362,
            locationRandomLat = 48.855, locationRandomLon = 2.363,
            zone = false, iscatch = true, id = 101L
        ),
        PlaceCard(
            personId = 2L, personNameFr = "Marie Curie", personNameEn = "Marie Curie",
            nameFr = "Institut Curie", nameEn = "Curie Institute",
            locationLat = 48.844, locationLon = 2.341,
            locationRandomLat = 48.845, locationRandomLon = 2.342,
            zone = true, iscatch = true, id = 202L
        )
    )
    CardList(cards = listCards)
}