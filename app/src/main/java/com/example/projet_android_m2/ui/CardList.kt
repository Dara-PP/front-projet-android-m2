package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.KtorServer
import com.example.projet_android_m2.data.PlaceRepository
import com.example.projet_android_m2.data.db.CardHistory
import com.example.projet_android_m2.data.db.CardHistoryAction
import com.example.projet_android_m2.data.db.PlaceCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    val scope = rememberCoroutineScope()
    var userCards by remember { mutableStateOf<List<PlaceCard>>(emptyList()) }

    suspend fun reload() {
        val userId = KtorServer().getUsername(context) ?: ""
        userCards = repo.getCaughtCards(userId)
    }
    LaunchedEffect(Unit) { reload() }
    CardList(cards = userCards, repo = repo, modifier = modifier,onReload = { scope.launch { reload() } })
}

@Composable
fun CardList(
    cards: List<PlaceCard> = emptyList(),
    repo: PlaceRepository? = null,
    onReload: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)) // 🎨 FOND HARMONISÉ
            .padding(16.dp)
    ) {
        Text(
            text = "Ma Collection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50), // Bleu nuit
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )
        Text(
            text = "${cards.size} carte(s) collectée(s)",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cards.isEmpty()) {
            Text(
                text = "Aucune carte pour l'instant. Partez explorer !",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn {
                items(cards) { card ->
                    CaughtCardItem(card = card, repo = repo, onTransfer = onReload)
                }
            }
        }
    }
}

@Composable
fun CaughtCardItem(card: PlaceCard, repo: PlaceRepository? = null, onTransfer: () -> Unit = {}  ) {
    val nom = card.personNameFr ?: card.personNameEn ?: "?"
    val lieu = card.nameFr ?: card.nameEn ?: "?"
    val type = if (card.zone) "Zone" else "Lieu"
    var expanded by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<CardHistory>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var transferUserId by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                expanded = !expanded
                if (expanded && history.isEmpty() && repo != null)
                    scope.launch { history = repo.getCardHistory(card.id) }
            },
        shape = RoundedCornerShape(16.dp), // Bords arrondis comme le Login
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Légère ombre
        colors = CardDefaults.cardColors(containerColor = Color.White) // Fond blanc pour ressortir
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Plus d'espace intérieur
            Text(text = nom, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = lieu, fontSize = 14.sp, color = Color.DarkGray)
            Text(text = type, fontSize = 12.sp, color = Color.Gray)

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0)) // Ligne de séparation douce
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = transferUserId,
                    onValueChange = { transferUserId = it },
                    label = { Text("Donner à (ID Joueur)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp), // Bords arrondis
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (transferUserId.isNotBlank() && repo != null) {
                            scope.launch {
                                repo.transferCard(card.id, transferUserId)
                                expanded = false
                                onTransfer()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50), // Bouton pilule
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)) // Vert d'action
                ) {
                    Text("DONNER LA CARTE", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Historique", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
                Spacer(modifier = Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Text("Aucun événement.", fontSize = 12.sp, color = Color.LightGray)
                } else {
                    history.forEach { event ->
                        val action = when (event.action) {
                            CardHistoryAction.CAPTURED.value -> "🎯 Capturée"
                            CardHistoryAction.TRADED.value -> "🤝 Échangée"
                            CardHistoryAction.WON_BATTLE.value -> "⚔️ Gagnée bataille"
                            else -> "❓ Action inconnue"
                        }
                        val date = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)
                            .format(Date(event.timestamp))

                        Text("$action — $date", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
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