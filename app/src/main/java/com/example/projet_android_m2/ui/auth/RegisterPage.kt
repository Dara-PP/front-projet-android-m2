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
fun RegisterPage(navController: NavController) {
    // 1. Les mémoires (États) et outils
    val scope = rememberCoroutineScope()
    var idInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // 2. Le fond d'écran
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50)), // Même bleu nuit que le Login
        contentAlignment = Alignment.Center
    ) {
        // 3. La carte centrale
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // 4. Le contenu du formulaire
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre
                Text(
                    text = "Nouveau Collectionneur",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Champ Nom
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("Nom du collectionneur") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Email
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Adresse Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Mot de passe (caché)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), // Les petits points noirs
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Le bouton principal (Inscription)
                Button(
                    onClick = {
                        scope.launch {
                            val server = KtorServer()
                            val token = server.register(context, idInput, passwordInput, email = emailInput)

                            withContext(Dispatchers.Main) {
                                if (token != null) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50), // Bouton "pilule"
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)) // Vert sympa
                ) {
                    Text("CRÉER MON COMPTE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Le bouton secondaire (Retour Login)
                TextButton(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                ) {
                    Text("Déjà un compte ? Se connecter", color = Color.Gray)
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun TestRegisterPage() {
    RegisterPage(navController = rememberNavController())
}