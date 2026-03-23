package com.example.projet_android_m2.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.projet_android_m2.ui.game.FireGame
import com.example.projet_android_m2.ui.game.ShakeTreeGame
import com.example.projet_android_m2.ui.game.signalGame.SignalGame
import com.example.projet_android_m2.ui.minigames.BombDefuseMiniGame

@Composable
fun MiniGameHost(
    game: MiniGame,
    onFinished: (score: Int) -> Unit,
) {
    val context = LocalContext.current
    when (game) {
        MiniGame.SHAKE_TREE    -> ShakeTreeGame(onGameFinished = onFinished)
        MiniGame.BOMB_DEFUSE   -> BombDefuseMiniGame(onGameFinished = onFinished)
        MiniGame.HIDE_BLOW     -> FireGame(onGameFinished = onFinished)
        MiniGame.SIGNAL_FINDER -> SignalGame(onGameFinished = onFinished)
    }
}


enum class MiniGame { SHAKE_TREE, BOMB_DEFUSE, HIDE_BLOW, SIGNAL_FINDER }

fun scoreMinimum(game: MiniGame): Int = when (game) {
    MiniGame.SHAKE_TREE    -> 0
    MiniGame.BOMB_DEFUSE   -> 1
    MiniGame.HIDE_BLOW     -> 1
    MiniGame.SIGNAL_FINDER -> 1
}

fun pickRandomGame(): MiniGame = MiniGame.entries.random()