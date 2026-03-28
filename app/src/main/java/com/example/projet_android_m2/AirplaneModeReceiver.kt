package com.example.projet_android_m2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AirplaneModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val isAirplaneModeOn = intent.getBooleanExtra("state", false) // mode avion true ou false
            if (isAirplaneModeOn) {
                Toast.makeText(
                    context,
                    "✈Mode avion activé, le serveur Ktor est injoignable",
                    Toast.LENGTH_LONG).show()
            }

            else {
                Toast.makeText(
                    context,
                    "Connexion rétablie ! GG",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}