package com.example.projet_android_m2.ui.minigame

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.KtorServer
import androidx.compose.ui.platform.LocalContext
import com.example.projet_android_m2.data.Card

@Composable
fun CapScreen() {

    val context = LocalContext.current
    val server = KtorServer() // ton serveur

    var resultMessage by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }
    var gameKey by remember { mutableStateOf(0) }
    var capturedCard by remember { mutableStateOf<Card?>(null) } // Stocke la carte capturée

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        key(gameKey) {
            gameShake(
                onSuccess = {
                    resultMessage = "Réussi ! 🎉 Carte capturée"
                    showResult = true

                    // Génération de la carte capturée
                    val card = Card(
                        name = "Mystic Card",
                        rarity = "Rare",
                        type = "Feu",
                        power = (50..100).random(),
                        ownerId = server.getUsername(context) ?: "unknown"
                    )
                    capturedCard = card
                    showCard = true

                    // Sauvegarde automatique sur le serveur
                    server.saveCapturedCard(context, card)
                },
                onFail = {
                    resultMessage = "Échec 😢 Essaie encore"
                    showResult = true
                }
            )
        }

        if (showResult) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(resultMessage, fontSize = 22.sp)

                Spacer(Modifier.height(16.dp))

                if (showCard && capturedCard != null) {
                    CardCapture(
                        card = capturedCard!!,
                        onSave = {
                            resultMessage = "Carte ajoutée 🎴"
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    // Réinitialisation du mini-jeu
                    showResult = false
                    showCard = false
                    resultMessage = ""
                    capturedCard = null
                    gameKey++ // force le recomposable de gameShake
                }) {
                    Text("Rejouer")
                }
            }
        }
    }
}
@Composable
fun CardCapture(card: Card, onSave: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Carte : ${card.name}", fontSize = 20.sp)
        Text("Type : ${card.type}", fontSize = 16.sp)
        Text("Rareté : ${card.rarity}", fontSize = 16.sp)
        Text("Puissance : ${card.power}", fontSize = 16.sp)

        Spacer(Modifier.height(8.dp))

        Button(onClick = { onSave() }) {
            Text("Ajouter à la collection")
        }
    }
}