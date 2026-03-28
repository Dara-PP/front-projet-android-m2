package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    val userId = remember { KtorServer().getUsername(context) ?: "" }

    // Flow Room se met à jour automatiquement après sync ou capture
    val ownedCards by repo.cardHistoryDao.getOwnedCardsFlow(userId).collectAsState(initial = emptyList())
    var syncMessage by remember { mutableStateOf<String?>(null) }

    // Sync automatique au premier chargement
    LaunchedEffect(Unit) {
        repo.syncHistoryFromServer()
            .onSuccess { count -> if (count > 0) syncMessage = "$count carte(s) synchronisée(s)" }
            .onFailure { /* hors ligne on affiche ce qui est déjà en base */ }
    }

    CardList(
        cards = ownedCards,
        repo = repo,
        syncMessage = syncMessage,
        modifier = modifier
    )
}

@Composable
fun CardList(
    cards: List<CardHistory> = emptyList(),
    repo: PlaceRepository? = null,
    syncMessage: String? = null,
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
            modifier = Modifier.padding(bottom = 8.dp)
        )
        syncMessage?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = if (it.startsWith("Erreur")) Color.Red else Color(0xFF388E3C),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (cards.isEmpty()) {
            Text(text = "Aucune carte pour l'instant.", color = Color.Gray)
        } else {
            LazyColumn {
                items(cards) { card ->
                    CaughtCardItem(card = card, repo = repo)
                }
            }
        }
    }
}

@Composable
fun CaughtCardItem(card: CardHistory, repo: PlaceRepository? = null) {
    val nom = card.personName ?: card.cardId
    var expanded by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<CardHistory>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var transferUserId by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 6.dp)
            .clickable {
                expanded = !expanded
                if (expanded && history.isEmpty() && repo != null)
                    scope.launch { history = repo.cardHistoryDao.getHistoryForCard(card.cardId) }
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (expanded) {
                OutlinedTextField(
                    value = transferUserId,
                    onValueChange = { transferUserId = it },
                    label = { Text("Donner à...") },
                    singleLine = true
                )
                Button(onClick = { /* TODO: échange serveur */ }) {
                    Text("Donner")
                }
                Text("Historique", fontSize = 11.sp, color = Color.Gray)
                if (history.isEmpty()) {
                    Text("Aucun événement.", fontSize = 11.sp, color = Color.LightGray)
                } else {
                    history.forEach { event ->
                        val action = when (event.action) {
                            CardHistoryAction.CAPTURED.value -> "Capturée"
                            CardHistoryAction.TRADED.value -> "Échangée"
                            CardHistoryAction.WON_BATTLE.value -> "Gagnée bataille"
                            else -> "?"
                        }
                        val date = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)
                            .format(Date(event.timestamp))
                        Text("$action — $date", fontSize = 11.sp, color = Color.DarkGray)
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
        CardHistory(cardId = "uuid-1", personName = "Victor Hugo", userId = "test", action = CardHistoryAction.CAPTURED.value),
        CardHistory(cardId = "uuid-2", personName = "Marie Curie", userId = "test", action = CardHistoryAction.WON_BATTLE.value)
    )
    CardList(cards = listCards)
}
