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
fun LoginPage(navController: NavController) {
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Login Page")
        Spacer(modifier = Modifier.height(7.dp))
        OutlinedTextField(
            value = idInput,
            onValueChange = {idInput = it},
            label = {Text("id user")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(7.dp))
        OutlinedTextField(
            value = passwordInput,
            onValueChange = {passwordInput = it},
            label = {Text("password")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(7.dp))

        Button(
            onClick = {
                scope.launch {
                    val server = KtorServer()
                    val token = server.login(context, username = idInput, mdp = passwordInput)
                    // Switch sur le thread main pour le toast
                    withContext(Dispatchers.Main) {
                        if (token != null) {
                            navController.navigate("home"){
                                // pour eviter les missclick bouton retour sur la login page alors que deja login
                                popUpTo ("login" ){inclusive = true }
                            }
                            Toast.makeText(context, "Bon Login", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mauvais login", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        ) {Text(text = "Se connecter")}
        Spacer(modifier = Modifier.height(7.dp))
        Text(text = "Pas de compte ?")
        Button(onClick = {
            navController.navigate("register")
        }) {Text(text = "Créer un compte")}
    }
}

@Preview(showBackground = true)
@Composable
fun TestLoginPage(){
    LoginPage(navController = rememberNavController())
}