package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.PlaceRepository

@Composable
fun JsonDeroulo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }

    // Flow Room s'actualise automatiquement à chaque fois que user_cards change
    val cards by repo.userCardDao.getAll().collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Cartes rencontrées",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "${cards.size} carte(s) rencontrée",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cards.isEmpty()) {
            Text(
                text = "Aucune carte rencontrée",
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            LazyColumn {
                items(cards) { card ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = card.person_name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Power : ${card.power.toInt()}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestJsonDeroulo() {
    JsonDeroulo()
}
