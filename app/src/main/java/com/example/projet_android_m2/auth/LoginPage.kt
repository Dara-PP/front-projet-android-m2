package com.example.projet_android_m2.auth

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
import com.example.projet_android_m2.KtorServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginPage() {
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
                    val loginTest = server.login(idInput, passwordInput)
                    // Switch sur le thread main pour le toast
                    withContext(Dispatchers.Main) {
                        if (loginTest) {
                            Toast.makeText(context, "Bon Login", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mauvais login", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        ) {Text(text = "Se connecter")}
    }
}

@Preview(showBackground = true)
@Composable
fun TestLoginPage(){
    LoginPage()
}