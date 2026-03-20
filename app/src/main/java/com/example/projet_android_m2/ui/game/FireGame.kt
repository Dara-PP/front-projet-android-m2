package com.example.projet_android_m2.ui.game

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlin.math.abs
// Design color du jeu
// TODO gestion des modes dark-mode de l'application par la suite peut etre...
private val TakeshiCard = Color(0xFF1A1A2E)
private val TakeshiGold = Color(0xDDFFD700)
private val TakeshiGreen = Color(0xD04CAF50)
private val TakeshiRed = Color(0xC4EA2F2F)
private val TakeshiOrange = Color(0xFFFF9800)

// Détecte le souffle / bruits du microphone
private class SouffleDuFeu(val onResult: (isBlow: Boolean, level: Float) -> Unit) {
    private var running = false
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        //  Calcul de la taille du buffer
        val bufSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val rec = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC) // source = le micro du téléphone
            .setAudioFormat(AudioFormat.Builder()
                .setSampleRate(8000) // meme config que bufSize
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build())
            .setBufferSizeInBytes(bufSize)
            .build()
        val buf = ShortArray(bufSize) // tableau qui recevra les données audio brutes
        running = true
        rec.startRecording() // démarre le micro
        Thread {
            while (running) { // Permet de lire en boucle le mic et determiner l'intensité du mic pour le mini jeu
                val read = rec.read(buf, 0, bufSize) // lit les samples dans buf, retourne le nombre de samples lus
                if (read > 0) {
                    val amp = buf.take(read).maxOfOrNull { abs(it.toInt()) } ?: 0
                    onResult(amp > 1500, (amp.toFloat() / 2400f).coerceIn(0f, 1f)) // amplitude du souffle 1500 peut etre trop haut, test sur d'autre telephone
                }
            }
            rec.stop()
            rec.release() // libère les ressources micro
        }.also { it.isDaemon = true; it.start() }
    }

    fun stop() { running = false }
}

// Detecte la luminosité de la caméra
private class BrightnessAnalyzer(val onResult: (brightness: Double) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val buf = image.planes[0].buffer // plan Y de l'image (noir/blanc), plan U(bleu) et V(rouge) pas concerné
        val pixels = ByteArray(buf.remaining()).also { buf.get(it) }
        val brightness = pixels.sumOf { it.toInt() and 0xFF }.toDouble() / pixels.size // calcul lum moyenne
        onResult(brightness)
        image.close()
    }
}

// 3 phases de jeu : intro(regles) -> jeu -> fin(win/lose)
@Composable
fun FireGame(onGameFinished: (Int) -> Unit = {}) {
    val context = LocalContext.current
    // cycle vie camera au composable
    val lifecycle = LocalLifecycleOwner.current
    // permission caméra et micro
    val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    println("DEBUG hasCam=$hasCam | hasMic=$hasMic")
    // Var pour le mini jeu
    var phase by remember { mutableStateOf("intro") }
    var isCovered by remember { mutableStateOf(false) }
    var isBlow by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(255.0) }
    var blowLevel by remember { mutableStateOf(0f) }
    // Temps tenu en continu
    var holdTime by remember { mutableStateOf(0f)}
    var surviveProgress by remember { mutableStateOf(0f) }

    // Setup camera
    if (hasCam) {
        LaunchedEffect(Unit) {
            println("DEBUG Setup caméra")
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), BrightnessAnalyzer { b ->
                brightness = b
                isCovered = b < 40.0
                println("DEBUG luminosité=${"%.1f".format(b)} | isCovered=$isCovered")
            })
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis)
            } catch (e: Exception) {
                println("Camera erreur: ${e.message}")
            }
        }
    }else {
        println("DEBUG Permission cam refusée")
    }

    // Setup micro
    if (hasMic) {
        DisposableEffect(Unit) {
            println("DEBUG Mic")
            val detector = SouffleDuFeu { blow, level ->
                isBlow = blow
                blowLevel = level
                println("DEBUG isBlow=$blow | level=${"%.2f".format(level)}")
            }
            detector.start()
            onDispose { detector.stop() }
        }
    }

    // tenir caméra + souffle pendant 3s d'affilée
    // Si on lache -> holdTime reset 0 par palier sinon animation saccadé,
    LaunchedEffect(phase) {
        println("DEBUG phase : $phase")
        if (phase == "playing") {
            holdTime = 0f
            surviveProgress = 0f
            while (phase == "playing") {
                delay(100)
                if (isCovered && isBlow) {
                    holdTime += 0.1f
                } else {
                    holdTime = (holdTime - 0.05f).coerceAtLeast(0f) // reset petit à petit
                }
                surviveProgress = (holdTime / 3f).coerceIn(0f, 1f)
                if (holdTime >= 3f) {
                    println("DEBUG : win")
                    phase = "win"
                }
            }
        }
        // TODO défaite presque impossible peut etre rajouter un chrono apres ou pas
        if (phase == "win")  { delay(2000); onGameFinished(1) }
        if (phase == "lose") { delay(2000); onGameFinished(0) }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (phase == "intro") {
            TakeshiHomeScreen(onStart = { phase = "playing" })
        } else {
            TakeshiGameScreen(
                phase = phase,
                surviveProgress = surviveProgress,
                isCovered = isCovered,
                isBlow = isBlow,
                brightness = brightness,
                blowLevel = blowLevel,
                hasCam = hasCam,
                hasMic = hasMic
            )
        }
    }
}

// Ecran d'intro/regles
@Composable
fun TakeshiHomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(TakeshiCard, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("COMMENT JOUER", fontSize = 11.sp, color = TakeshiGold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            Text("Couvre la caméra avec ta main\nSouffle fort dans le micro\nFais les deux EN MEME TEMPS pendant 3 secondes pour allumer le feu !", fontSize = 13.sp, color = Color.White)
        }
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TakeshiCard, contentColor = TakeshiGold),
            border = BorderStroke(3.dp, TakeshiGold),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("(ง •̀_•́)ง", fontSize = 20.sp)
                    Text("JOUER !", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Text("APPUYER POUR COMMENCER", fontSize = 11.sp, letterSpacing = 1.sp, color = Color.Gray)
            }
        }
    }
}

// Ecran du jeu
@Composable
fun TakeshiGameScreen(
    phase: String,
    surviveProgress: Float,
    isCovered: Boolean,
    isBlow: Boolean,
    brightness: Double,
    blowLevel: Float,
    hasCam: Boolean,
    hasMic: Boolean
) {
    val borderColor = when (phase) {
        "playing" -> TakeshiOrange
        "win" -> TakeshiGreen
        "lose" -> TakeshiRed
        else -> TakeshiGold
    }

    val statusLabel = when (phase) { "playing" -> "ALLUME !!"; "win" -> "FEU ALLUMÉ !!"; "lose" -> "RATÉ..."; else -> "" }
    val ascii = when (phase) { "playing" -> " ('>o.o)'> "; "win" -> "DU FEUUUU"; "lose" -> "BOUUUUH"; else -> "-_-" }
    val title = when (phase) { "playing" -> "Allume le feu !"; "win" -> "CARTE GAGNÉE !!"; "lose" -> "Le feu s'est éteint..."; else -> "" }
    val subtitle = when (phase) { "playing" -> "COUVRE LA CAMERA\nSOUFFLE FORT !"; "win" -> "Le feu est allumé !"; "lose" -> "Main ou souffle manquant..."; else -> "" }
    val titleColor = when (phase) { "playing" -> TakeshiOrange; "lose" -> TakeshiRed; "win" -> TakeshiGreen; else -> TakeshiGold }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TakeshiCard, RoundedCornerShape(12.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (statusLabel.isNotEmpty())
                    Text(statusLabel, fontSize = 10.sp, color = borderColor, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text(ascii, fontSize = 32.sp, color = Color.White, textAlign = TextAlign.Center)
                Text(title, fontSize = 18.sp, color = titleColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                if (subtitle.isNotEmpty())
                    Text(subtitle, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
            BoxCorner(Modifier.align(Alignment.TopStart).padding(20.dp), borderColor)
            BoxCorner(Modifier.align(Alignment.TopEnd).padding(20.dp).rotate(90f), borderColor)
            BoxCorner(Modifier.align(Alignment.BottomStart).padding(20.dp).rotate(270f), borderColor)
            BoxCorner(Modifier.align(Alignment.BottomEnd).padding(20.dp).rotate(180f), borderColor)
        }

        // Jauges + barre de progression pendant le jeu
        if (phase == "playing") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CameraEyeGauge(isCovered = isCovered, brightness = brightness, modifier = Modifier.weight(1f))
                BlowGauge(blowLevel = blowLevel, isBlow = isBlow, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { surviveProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = TakeshiOrange,
                trackColor = Color.DarkGray
            )
        }

        if (!hasCam) Text("Permission caméra manquante", color = TakeshiRed, fontSize = 11.sp)
        if (!hasMic) Text("Permission micro manquante", color = TakeshiRed, fontSize = 11.sp)
    }
}

// Coins décoratifs
@Composable
fun BoxCorner(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(12.dp)) {
        drawLine(color, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 5f)
        drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 5f)
    }
}

// Etat de la tete de takeshi selon lum sur camera
@Composable
fun CameraEyeGauge(isCovered: Boolean, brightness: Double, modifier: Modifier = Modifier) {
    val eyeOpen = (brightness / 255.0).toFloat().coerceIn(0f, 1f)
    val eyeColor = if (isCovered) TakeshiGreen else TakeshiRed
    val eyeAscii = when {
        eyeOpen < 0.15f -> "^-^"
        eyeOpen < 0.40f -> "(=_=)"
        eyeOpen < 0.70f -> "\\-(o)-(o)-/"
        else -> "@(o),(o)"
    }
    Column(
        modifier = modifier.background(TakeshiCard, RoundedCornerShape(8.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Caméra", fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
        Text(eyeAscii, fontSize = 22.sp, color = eyeColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.DarkGray)) {
            Box(modifier = Modifier.fillMaxWidth(eyeOpen).fillMaxHeight().background(eyeColor))
        }
        Text(if (isCovered) "IL FAIT NOIR ICI ?" else "COUVRE LA CAMERA !!", fontSize = 11.sp, color = eyeColor, fontWeight = FontWeight.Bold)
    }
}

// Jauge de souffle
@Composable
fun BlowGauge(blowLevel: Float, isBlow: Boolean, modifier: Modifier = Modifier) {
    val barColor = if (isBlow) TakeshiGreen else TakeshiRed
    var smoothLevel by remember { mutableFloatStateOf(blowLevel) }
    if (blowLevel > smoothLevel) smoothLevel += 0.05f else smoothLevel -= 0.05f
    smoothLevel = smoothLevel.coerceIn(0f, 1f)

    Column(
        modifier = modifier.background(TakeshiCard, RoundedCornerShape(8.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Souffle", fontSize = 10.sp, color = Color.Gray)
        Box(modifier = Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(3.dp)).background(Color.DarkGray)) {
            Box(modifier = Modifier.fillMaxWidth(smoothLevel).fillMaxHeight().background(barColor))
        }
        Text(
            text = if (isBlow) "YEAHHH !!" else "SOUFFFLLEE !!",
            fontSize = 11.sp,
            color = barColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun PreviewTakeshiHome() {
    TakeshiHomeScreen(onStart = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewFireGame() {
    TakeshiGameScreen(
        phase = "playing",
        surviveProgress = 0.5f,
        isCovered = false, isBlow = true,
        brightness = 180.0, blowLevel = 0.7f,
        hasCam = true, hasMic = true
    )
}

@Preview(showBackground = true)
@Composable
fun TestCameraEye() {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CameraEyeGauge(isCovered = false, brightness = 200.0, modifier = Modifier.weight(1f))
        CameraEyeGauge(isCovered = true, brightness = 10.0, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun TestBlowGauge() {

    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BlowGauge(blowLevel = 0.2f, isBlow = false, modifier = Modifier.weight(1f))
        BlowGauge(blowLevel = 0.8f, isBlow = true, modifier = Modifier.weight(1f))
    }
}