package com.example.projet_android_m2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ApplicationsScreen(
    onDavidAppClick: () -> Unit = {},
    onFrancoisAppClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Applications du Groupe",
            fontSize = 24.sp,
            fontWeight = FontWeight.Companion.Bold,
            modifier = Modifier.Companion.padding(bottom = 32.dp)
        )

        val membresDuGroupe = listOf("David", "Florian", "Axyl", "Dara", "François")

        membresDuGroupe.forEach { prenom ->
            Button(
                onClick = {
                    when (prenom) {
                        "David"    -> onDavidAppClick()
                        "François" -> onFrancoisAppClick()
                        else       -> println("Clic sur l'application de $prenom")
                    }
                },
                modifier = Modifier.Companion
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Application de $prenom")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApplicationsScreen() {
    ApplicationsScreen()
}