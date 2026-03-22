package com.example.projet_android_m2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.data.KtorServer
import com.example.projet_android_m2.data.PantheonPlayerResponse

@Composable
fun HomePage(navController: NavController) {
    val context = LocalContext.current
    val server = remember { KtorServer() }
    val username = server.getUsername(context) ?: "Joueur"

    // states pr la vraie data
    var leaderboard by remember { mutableStateOf<List<PantheonPlayerResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // fetch les donnees au demarrage
    LaunchedEffect(Unit) {
        isLoading = true
        leaderboard = server.getPantheon(context)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(20.dp)
    ) {
        // header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salut,\n$username ! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1E2E),
                lineHeight = 36.sp
            )
            Button(
                onClick = {
                    server.logout(context)
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Quitter", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "🏆 LE PANTHÉON 🏆",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color(0xFFF57C00),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // affichage dynamique (loader, vide ou liste)
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF57C00))
            }
        } else if (leaderboard.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("aucun score pour l'instant 😢", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaderboard) { player ->
                    val (medalEmoji, rankColor) = when (player.rank) {
                        1 -> "🥇" to Color(0xFFFBBF24)
                        2 -> "🥈" to Color(0xFF9CA3AF)
                        3 -> "🥉" to Color(0xFFB45309)
                        else -> " ${player.rank}." to Color(0xFF374151)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(medalEmoji, fontSize = 24.sp, modifier = Modifier.width(40.dp))
                        Text(
                            text = player.username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = rankColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${player.score} pts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestHomePage() {
    HomePage(navController = rememberNavController())
}