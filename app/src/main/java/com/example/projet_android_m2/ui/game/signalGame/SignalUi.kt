package com.example.projet_android_m2.ui.game.signalGame

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.projet_android_m2.ui.game.signalGame.SignalGame.Companion.isWin
import com.example.projet_android_m2.ui.game.signalGame.SignalGame.Companion.maxTime
import com.example.projet_android_m2.ui.game.signalGame.SignalGame.Companion.timeLeft
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import com.example.projet_android_m2.R
import androidx.camera.view.PreviewView





@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SignalGameScreen(
    signalGame: SignalGame,
    useCamera: Boolean,
    onToggleCamera: () -> Unit
) {
    var showStartMessage by remember { mutableStateOf(true) }
    // Filtre passe bas pour eviter les vacillements
    val alpha = 0.12f
    val threshold = 5f // Ne bouge pas si le changement est inférieur à 0.05 degrés
    var smoothedAzimuth by remember { mutableFloatStateOf(SignalGame.azimuth) }
    val rawAzimuth = SignalGame.azimuth

// On utilise un LaunchedEffect pour lisser de manière fluide à chaque changement de rawAzimuth
    LaunchedEffect(rawAzimuth) {
        val delta = ((rawAzimuth - smoothedAzimuth + 540f) % 360f) - 180f
        if (kotlin.math.abs(delta) > threshold) {
            smoothedAzimuth = (smoothedAzimuth + alpha * delta + 360f) % 360f
        }
    }

    LaunchedEffect(Unit) {
        delay(3000) // 3 secondes
        showStartMessage = false
    }

    val isPlaying = SignalGame.isPlaying

    // Permission caméra
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = Modifier.fillMaxSize()) {

        // Fonds ecrans
        if (useCamera) {
            if (cameraPermission.status.isGranted) {
                CameraPreview()
            } else {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text     = "Permission caméra requise",
                            color    = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                            Text("Autoriser la caméra")
                        }
                    }
                }
            }
        } else {
            PanoramiqueMap(azimuth = smoothedAzimuth)
        }

        // Affichage du message
        if (showStartMessage) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trouvez la carte dans le temps imparti !",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        if (!isPlaying && !isWin) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vous avez Perdu !",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        // Affichage du signal
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "Signal : ${signalGame.signalStrength}",
                color    = Color.Green,
                fontSize = 32.sp
            )
            LinearProgressIndicator(
                progress   = { (signalGame.signalStrength / 100f).coerceIn(0f, 1f) },
                modifier   = Modifier.width(200.dp).padding(top = 8.dp),
                color      = Color.Cyan,
                trackColor = Color.DarkGray,
                strokeCap  = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }


        // Boussole en haut a droite
        Text(
            text     = "↑ ${smoothedAzimuth.toInt()}°",
            color    = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 16.dp)
        )


        val progress by animateFloatAsState(
            targetValue = (timeLeft.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
            animationSpec = tween(500)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 45.dp, end = 16.dp)
                .size(30.dp)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Fond gris
                    drawArc(
                        color = Color.DarkGray,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = true
                    )

                    // Camembert coloré
                    drawArc(
                        color = if (timeLeft <= 15) Color.Red else if(timeLeft <= 30) Color.Yellow else Color.Green,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = true
                    )
                    // Contour noir
                    drawArc(
                        color = Color.Black,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }

                /*Text(
                    text = "${timeLeft}s",
                    color = Color.White,
                    fontSize = 14.sp
                )*/
            }
        }

        //------------------------------------

        // Animation carte débloquée
        if (isWin) {
            CarteAvecAnimation()
        }

        // Bouton switch caméra / 360°
        Box(
            modifier         = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(onClick = onToggleCamera) {
                Text(if (useCamera) "Mode 360°" else "Mode Caméra")
            }
        }
    }
}
@Composable
fun CameraPreview() {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory  = { ctx ->
            val previewView = PreviewView(ctx)
            startCamera(ctx, previewView, lifecycleOwner)
            previewView
        }
    )
}

private fun startCamera(
    context:        Context,
    previewView:    PreviewView,
    lifecycleOwner: LifecycleOwner
) {
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        val provider = future.get()
        val preview = Preview.Builder().build()
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

//affichage de l'image panomarique 360°
@Composable
fun PanoramiqueMap(azimuth: Float) {
    val context: Context = LocalContext.current.applicationContext

    val bitmap: ImageBitmap = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.drawable.img)
            .asImageBitmap()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val screenW = size.width
        val screenH = size.height
        val imgW    = bitmap.width.toFloat()
        val imgH    = bitmap.height.toFloat()

        val scale = screenH / imgH

        // azimuth [0°;360°] → colonne de départ dans l'image (en px natifs)
        val scrollX = ((azimuth / 360f) * imgW) % imgW

        drawIntoCanvas { canvas ->
            val paint    = Paint()
            var coveredX = 0f
            var srcStart = scrollX

            // Remplit l'écran avec des tranches de l'image en loop
            while (coveredX < screenW) {
                val srcW = (imgW - srcStart)
                    .coerceAtMost((screenW - coveredX) / scale)
                val dstW = srcW * scale

                canvas.drawImageRect(
                    image     = bitmap,
                    srcOffset = IntOffset(srcStart.toInt(), 0),
                    srcSize   = IntSize(srcW.toInt().coerceAtLeast(1), imgH.toInt()),
                    dstOffset = IntOffset(coveredX.toInt(), 0),
                    dstSize   = IntSize(dstW.toInt().coerceAtLeast(1), screenH.toInt()),
                    paint     = paint
                )

                coveredX += dstW
                srcStart  = 0f  // loop : tranche suivante depuis le début
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animation de la carte débloquée
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CarteAvecAnimation() {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2_000)
        isVisible = false
    }

    val scale by animateFloatAsState(
        targetValue   = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label         = "carte_scale"
    )
    val rotation by animateFloatAsState(
        targetValue   = if (isVisible) 15f else 360f,
        animationSpec = tween(durationMillis = 800),
        label         = "carte_rotation"
    )

    if (scale > 0f) {
        Image(
            painter            = painterResource(id = R.drawable.unlockcard),
            contentDescription = "Carte débloquée",
            modifier           = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .graphicsLayer(
                    scaleX    = scale,
                    scaleY    = scale,
                    rotationZ = rotation
                ),
            contentScale = ContentScale.Crop
        )
    }
}
