package com.example.projet_android_m2.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.projet_android_m2.data.KtorServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterPage(navController: NavController) {
    val scope = rememberCoroutineScope()
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Création de compte")
        Spacer(modifier = Modifier.height(7.dp))
        OutlinedTextField(
            value = idInput,
            onValueChange = { idInput = it },
            label = { Text("username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(7.dp))
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(7.dp))
        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                scope.launch {
                    val server = KtorServer()
                    val token = server.register(context, idInput, passwordInput, email = emailInput)
                    // Switch sur le thread main pour le toast
                    withContext(Dispatchers.Main) {
                        if (token != null) {
                            navController.navigate("home"){
                                // pour eviter les missclick bouton retour sur la login page alors que deja login
                                popUpTo ("login" ){inclusive = true }
                            }
                            Toast.makeText(context, "Compte crée avec succes bienvenue", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erreur création de compte", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        ) {Text(text = "Creer un compte")}
        Spacer(modifier = Modifier.height(7.dp))
        Text(text = "Déjà un compte ?")
        // Si deja un compte pour se connecter (retour login page)
        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
        ) { Text(text = "Se connecter") }
    }
}

@Preview(showBackground = true)
@Composable
fun TestRegisterPage(){
    RegisterPage(navController = rememberNavController())
}