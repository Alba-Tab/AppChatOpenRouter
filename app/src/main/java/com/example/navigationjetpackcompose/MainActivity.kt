package com.example.navigationjetpackcompose

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyScreen()
        }
    }
}

@Composable
fun MyScreen() {
    val coroutineScope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var userInput  by remember { mutableStateOf("") }
    val apiKey = "Bearer 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"

    var isThinking by remember { mutableStateOf(false) } // Estado para "pensando"
    var errorMessage by remember { mutableStateOf<String?>(null) } // Estado para errores


    val context = LocalContext.current


    val activity = context as? Activity


    val speechRecognizer = remember {
        //Creo una instancia de reconocimiento de voz
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    val speechIntent = remember {
        //Activa el reconocimiento de voz
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // Cambia a tu idioma si es necesario
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // Habilita resultados parciales
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1) // Un solo resultado
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName) // Asigna el paquete
        }
    }


    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                userInput = userInput + matches[0]  // Se asigna el texto reconocido al input
            }
        }

        override fun onError(error: Int) {
            Log.e("SpeechRecognizer", "Error al reconocer voz: $error")
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    Scaffold(
        topBar = {TopBarComponent("Chat OpenRouter")},
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(bottom = 3.dp)
                    .heightIn(min = 72.dp),
                backgroundColor = Color.White
            ) {
                MessageInput(
                    message = userInput ,
                    onMessageChange = {userInput  = it},
                    onSendClick = {
                        if (userInput .isNotEmpty()) {
                            val userMessage = ChatMessage("user", userInput)
                            messages = messages + userMessage
                            userInput = ""
                            isThinking = true  // Activar el estado de "pensando"
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val apiResponse = RetrofitInstance.api.getChatCompletion(
                                        apiKey,
                                        ChatRequest(messages = messages)
                                    )

                                    Log.d("API_RESPONSE", "Respuesta recibida: ${apiResponse.choices.firstOrNull()?.message}")

                                    val botMessage = apiResponse.choices.first().message.content
                                    messages = messages + ChatMessage("bot", botMessage)
                                } catch (e: Exception) {
                                    Log.e("API_ERROR", "Error al obtener la respuesta del bot: ${e.localizedMessage}", e)
                                    errorMessage = "Hubo un error al obtener la respuesta del bot."
                                } finally {
                                    isThinking = false  // Desactivar el estado de "pensando"
                                }}

                        }
                    },
                    onMicClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            activity?.let {
                                ActivityCompat.requestPermissions(
                                    it,
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    1
                                )
                            }
                        } else {
                            speechRecognizer.startListening(speechIntent)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        ChatContent(
            messages = messages,
            modifier = Modifier.padding(innerPadding),
            isThinking = isThinking,
            errorMessage = errorMessage
        )
    }
}


@Composable
fun TopBarComponent(title:String){
    TopAppBar(
        title = {
            Text(
                title,
                color = Color(0xFF000000)
            )
        },
        backgroundColor = Color(0xFF9B70EE)
    )

}

@Composable
fun MessageInput(message: String, onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit, onMicClick: () -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Escribe un mensaje...") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier

                    .background(Color.White),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                singleLine = false,  // Permite múltiples líneas
                maxLines = 3,  // Limita el número máximo de filas
                minLines = 1,  // Mínimo 1 fila visible

            )

        IconButton(onClick = onMicClick) {
            Icon(imageVector = Icons.Default.Mic, contentDescription = "Grabar", tint = Color.Black)
        }

        IconButton(onClick = onSendClick) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar", tint = Color.Black)
        }
    }
}

@Composable
fun ChatContent(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier,
    isThinking: Boolean,
    errorMessage: String?
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1) // Se mueve al último mensaje
        }
    }


        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            state = listState
        ) {
            itemsIndexed(messages) { index, message ->
                MessageBubble(message.content, message.role == "user")
            }

            // Si está pensando, muestra una burbuja de "Pensando..."
            if (isThinking) {
                item {
                    MessageBubble("Pensando...", isUser = false)
                }
            }

            // Si hubo un error, muestra un mensaje de error
            if (errorMessage != null) {
                item {
                    MessageBubble(errorMessage, isUser = false)
                }
            }
        }

}

@Composable
fun MessageBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUser) Color(0xFFDCF8C6) else Color(0xFFECECEC)) // Verde claro para usuario, gris claro para receptor
                .padding(12.dp)
        ) {
            Text(text, color = Color.Black)
        }
    }
}




@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyScreen()
}
