package com.example.projet_android_m2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android_m2.data.PlaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoadScreen(onLoadComplete: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { PlaceRepository(context) }
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("Initialisation") }
    var isLoading by remember { mutableStateOf(true) }
    var errorLoad by remember { mutableStateOf(false) }
    var shouldComplete by remember { mutableStateOf(false) }

    // Callback de complétion et laisser le temps au thread UI de finir le rendu du frame actuel
    LaunchedEffect(shouldComplete) {
        if (shouldComplete) {
            delay(150)
            onLoadComplete()
        }
    }

    LaunchedEffect(Unit) {
        // Les cartes sont récupérées en direct live
        statusMessage = "Prêt"
        progress = 100
        isLoading = false
        delay(1000) // Pour faire jolie, peut etre faire d'une autre maniere ?
        shouldComplete = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoading) "Chargement des données" else if (errorLoad) "Erreur" else "Chargement terminé",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Barre de progression circulaire
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF27AE60),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Barre de progression linéaire
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF27AE60),
            trackColor = Color(0xFF2C3E50).copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Status message
        Text(
            text = statusMessage,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(15.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if(!isLoading) {
            if(errorLoad){
                Button(
                    onClick = { shouldComplete = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) {
                    Text(
                        text = "Réessayer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (progress == 100) {
                Button(
                    onClick = { shouldComplete = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) {
                    Text(
                        text = "Continuer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun testLoadScreen(){
    LoadScreen(
        onLoadComplete = {}
    )
}