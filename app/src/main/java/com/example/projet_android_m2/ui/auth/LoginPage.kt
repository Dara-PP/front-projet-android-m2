package com.example.projet_android_m2.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.data.KtorServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginPage(navController: NavController) {
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)),
        contentAlignment = Alignment.Center
    ) {

        // 2. Le formulaire dans une Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Marge sur les côtés
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Petite ombre
        ) {

            // 3. Le contenu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Espace à l'intérieur de la carte
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre
                Text(
                    text = "CONNEXION",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50) // Le texte reste foncé pour bien se lire !
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Champ Identifiant
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("Nom du joueur") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp), // Arrondit le champ de texte
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Mot de passe
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bouton principal (plus gros et arrondi)
                Button(
                    onClick = {
                        scope.launch {
                            val server = KtorServer()
                            val token = server.login(context, username = idInput, mdp = passwordInput)

                            withContext(Dispatchers.Main) {
                                if (token != null) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    Toast.makeText(context, "Bon Login", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Mauvais login", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50), // 50 = forme de "pilule"
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) {
                    Text("JOUER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton secondaire
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Pas de compte ? S'inscrire", color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestLoginPage() {
    LoginPage(navController = rememberNavController())
}