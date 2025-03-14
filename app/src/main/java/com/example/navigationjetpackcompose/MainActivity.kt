package com.example.navigationjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatScreen()
        }
    }
}
@Composable
fun ChatScreen() {
    var message by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Esperando respuesta...") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Respuesta: $response", modifier = Modifier.padding(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Escribe un mensaje") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (message.isNotEmpty()) {
                    isLoading = true
                    // Enviar mensaje a la API
                    coroutineScope.launch {
                        response = sendMessageToApi(message)
                        isLoading = false
                    }
                    message = ""
                }
            }),
            modifier = Modifier.fillMaxWidth()
        )

        // Botón de enviar
        Button(
            onClick = {
                if (message.isNotEmpty()) {
                    isLoading = true
                    // Enviar mensaje a la API
                    coroutineScope.launch {
                        response = sendMessageToApi(message)
                        isLoading = false
                    }
                    message = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Enviar")
        }

        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

suspend fun sendMessageToApi(message: String): String {
    val client = OkHttpClient()
    val url = "https://openrouter.ai/api/v1/chat/completions"
    val mediaType = "application/json".toMediaType()

    val jsonBody = JSONObject().apply {
        put("model", "deepseek/deepseek-r1-zero:free")
        put("messages", listOf(
            JSONObject().apply {
                put("role", "system")
                put("content", "Por favor responde en español.")
            },
            JSONObject().apply {
                put("role", "user")
                put("content", message)
            }
        ))
    }.toString()

    val requestBody = jsonBody.toRequestBody(mediaType)
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", "Bearer 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")
        .addHeader("Content-Type", "application/json")
        .build()

    return try {
        withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()


            val responseBody = response.body?.string() ?: return@withContext "Sin respuesta del servidor"
            println("Respuesta de la API: $responseBody")

            val jsonResponse = JSONObject(responseBody)


            if (jsonResponse.has("choices")) {
                val choicesArray = jsonResponse.optJSONArray("choices")
                if (choicesArray != null && choicesArray.length() > 0) {
                    val messageObject = choicesArray.getJSONObject(0).optJSONObject("message")
                    messageObject?.optString("content", "Respuesta vacía") ?: "Respuesta vacía"
                } else {
                    "No se encontraron respuestas en la API"
                }
            } else {
                // Agregar log si no se encuentran los 'choices'
                println("No se encontró el campo 'choices' en la respuesta de la API")
                "Error en la respuesta de la API: No se encontraron 'choices'."
            }
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {

}
