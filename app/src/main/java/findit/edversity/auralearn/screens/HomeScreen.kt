package findit.edversity.auralearn.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import findit.edversity.auralearn.R
import findit.edversity.auralearn.ui.theme.Black
import findit.edversity.auralearn.ui.theme.CyanTertiary
import findit.edversity.auralearn.ui.theme.PurplePrimary
import findit.edversity.auralearn.ui.theme.PurpleSecondary
import findit.edversity.auralearn.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var isListening by remember { mutableStateOf(false) }
    var transcribedText by remember { mutableStateOf(
        """
            Selamat datang di Auralearn! 
            Tekan layar untuk memberikan perintah.
        """.trimIndent()
    ) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun fetchResponse(userText: String, tts: TextToSpeech?, transcribedText: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("http://10.0.2.2:8000/api/interact")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            try {
                // Create request body
                val requestBody = JSONObject().apply {
                    put("user_text", userText)
                    put("current_context", JSONObject())
                }.toString()

                // Write request body
                val outputStream = connection.outputStream
                outputStream.write(requestBody.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = bufferedReader.use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val data = jsonObject.getJSONObject("data")
                    var responseText = data.getString("text_audio")

                    // Clean the response text
                    responseText = responseText
                        .replace("\\*\\*".toRegex(), "") // Remove **bold** markers
                        .replace("\\*".toRegex(), "") // Remove *italic* markers

                    // Update UI and speak response
                    withContext(Dispatchers.Main) {
                        transcribedText(responseText)
                        tts?.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, "api_response")
                    }
                } else {
                    throw Exception("HTTP error: $responseCode")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = "Maaf, terjadi kesalahan: ${e.message}"
                    transcribedText(errorMessage)
                    tts?.speak(errorMessage, TextToSpeech.QUEUE_FLUSH, null, "api_error")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    // Voice command handler
    fun processVoiceCommand(command: String) {
        val normalized = command.lowercase(Locale.getDefault())
        when {
            normalized.contains("buka materi") || normalized.contains("daftar materi") -> {
                tts?.speak("Membuka daftar materi", TextToSpeech.QUEUE_FLUSH, null, "navigate_material")
                isTtsSpeaking = true
                transcribedText = "Membuka daftar materi"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        activity.runOnUiThread {
                            navController.navigate("materialList")
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                })
            }
            normalized.contains("keluar") || normalized.contains("tutup aplikasi") -> {
                tts?.speak("Menutup aplikasi", TextToSpeech.QUEUE_FLUSH, null, "exit")
                isTtsSpeaking = true
                transcribedText = "Menutup aplikasi"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        activity.runOnUiThread {
                            tts?.stop()
                            tts?.shutdown()
                            activity.finishAffinity()
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                })
            }
            else -> {
                transcribedText = "Memproses permintaan..."
                fetchResponse(
                    userText = normalized,
                    tts = tts,
                    transcribedText = { text ->
                        transcribedText = text
                    }
                )
            }
        }
    }

    fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                transcribedText = "Aura mendengarkan..."
            }

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                if (!isTtsSpeaking) {
                    transcribedText = "Tidak ada input terdeteksi"
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                text?.let {
                    processVoiceCommand(it)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        showPermissionRationale = false
    }

    // TTS Initialization
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isTtsSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isTtsSpeaking = false
                    }

                    @Deprecated("Deprecated in Java", ReplaceWith("isTtsSpeaking = false"))
                    override fun onError(utteranceId: String?) {
                        isTtsSpeaking = false
                    }
                })
                tts?.speak(transcribedText, TextToSpeech.QUEUE_FLUSH, null, "welcome")
            }
        }

        if (!hasMicPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
                showPermissionRationale = true
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            speechRecognizer.destroy()
            tts?.stop()
            tts?.shutdown()
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Izin Mikrofon Diperlukan") },
            text = { Text("Aplikasi membutuhkan akses mikrofon untuk perintah suara.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Izinkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Tolak")
                }
            }
        )
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleSecondary, Black)))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // Mute the TTS if it's speaking
                        if (isTtsSpeaking) {
                            tts?.stop()
                            isTtsSpeaking = false
                        }
                        // Start listening for voice commands
                        if (hasMicPermission) {
                            speechRecognizer.setRecognitionListener(createRecognitionListener())
                            speechRecognizer.startListening(speechIntent)
                        }
                        tryAwaitRelease()
                        speechRecognizer.stopListening()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        val size = size
                        val circleSize = size.minDimension * 0.35f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(PurplePrimary.copy(0.35f), Color.Transparent),
                                center = Offset(0.3f * size.width, 0.7f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.3f * size.width, 0.7f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(CyanTertiary.copy(0.35f), Color.Transparent),
                                center = Offset(0.7f * size.width, 0.3f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.7f * size.width, 0.3f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )
                    }
                }
                .blur(32.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo_home),
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp)
            )

            Text(
                text = "Selamat Datang!",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            VoiceWaveAnimation(isActive = isListening || isTtsSpeaking)

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Text(
                    text = transcribedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("materialList")
                },
                colors = ButtonDefaults.buttonColors(White, Black),
                modifier = Modifier
                    .padding(top = 32.dp)
                    .width(250.dp)
            ) {
                Text(
                    text = "Daftar Materi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VoiceWaveAnimation(isActive: Boolean) {
    val waveAnim = remember { Animatable(0f) }
    val waveHeights = remember { List(20) { Random.nextFloat() * 50f } } // Increased wave height range

    // Animate the wave size and the active state
    LaunchedEffect(isActive) {
        if (isActive) {
            waveAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing), // Make the animation smoother
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            // Smoothly transition to stop
            waveAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(600, easing = LinearEasing) // Smoother transition
            )
        }
    }

    // Horizontal gradient color (from #E43694 to #FF6E68)
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFFE43694), Color(0xFFFF6E68))
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(100.dp) // Increased the height of the wave
    ) {
        waveHeights.forEachIndexed { index, height ->
            // Animate the height of the wave
            val animatedHeight = if (isActive) {
                height * (0.5f + abs(sin(waveAnim.value * 10f + index * 0.5f)) * 0.5f)
            } else {
                2f // Minimized height when inactive
            }

            // Apply the animated height and the gradient color
            Spacer(
                modifier = Modifier
                    .width(8.dp) // Increased width for larger wave
                    .height(animatedHeight.dp)
                    .background(
                        brush = gradient,
                        shape = RoundedCornerShape(4.dp) // Slightly rounded corners
                    )
            )

            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
