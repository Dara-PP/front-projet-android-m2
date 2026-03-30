package com.example.projet_android_m2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ApplicationsScreen(
    onDavidAppClick: () -> Unit = {},
    onFrancoisAppClick: () -> Unit = {},
    onDaraAppClick: () -> Unit = {},
    onFlorianAppClick: () -> Unit = {},
    onAxylAppClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Applications du Groupe",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val membresDuGroupe = listOf("David", "Florian", "Axyl", "Dara", "François")

        membresDuGroupe.forEach { prenom ->
            Button(
                onClick = {
                    when (prenom) {
                        "David"    -> onDavidAppClick()
                        "François" -> onFrancoisAppClick()
                        "Dara"     -> onDaraAppClick()
                        "Florian"  -> onFlorianAppClick()
                        "Axyl"     -> onAxylAppClick()
                        else       -> println("Clic sur l'application de $prenom")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
            ) {
                Text(
                    text = "Application de $prenom",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApplicationsScreen() {
    ApplicationsScreen()
}