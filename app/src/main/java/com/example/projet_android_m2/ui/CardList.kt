package com.example.projet_android_m2.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    val ownedCards by repo.cardHistoryDao.getOwnedCardsFlow(userId).collectAsState(initial = emptyList())
    var syncMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repo.syncHistoryFromServer()
            .onSuccess { count -> if (count > 0) syncMessage = "$count carte(s) synchronisée(s)" }
            .onFailure { }
    }

    CardListContent(
        cards = ownedCards,
        repo = repo,
        syncMessage = syncMessage,
        modifier = modifier
    )
}

@Composable
fun CardListContent(
    cards: List<CardHistory> = emptyList(),
    repo: PlaceRepository? = null,
    syncMessage: String? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp)
    ) {
        Text(
            text = "Ma Collection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )
        Text(
            text = "${cards.size} carte(s) collectée(s)",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        syncMessage?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (cards.isEmpty()) {
            Text(
                text = "Aucune carte pour l'instant. Partez explorer !",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
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
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                expanded = !expanded
                if (expanded && history.isEmpty() && repo != null)
                    scope.launch { history = repo.cardHistoryDao.getHistoryForCard(card.cardId) }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = nom, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = transferUserId,
                    onValueChange = { transferUserId = it },
                    label = { Text("Donner à (ID Joueur)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (transferUserId.isNotBlank() && repo != null) {
                            scope.launch {
                                expanded = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
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
                            CardHistoryAction.CAPTURED.value -> " Capturée"
                            CardHistoryAction.TRADED.value -> "Échangée"
                            CardHistoryAction.WON_BATTLE.value -> " Gagnée"
                            else -> "❓ Action"
                        }
                        val date = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH).format(Date(event.timestamp))
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
    CardListContent(cards = emptyList())
}