package com.example.navigationjetpackcompose

import com.google.gson.annotations.SerializedName

// Modelo para el mensaje
data class ChatMessage(
    val role: String,
    val content: String
)

// Modelo de solicitud
data class ChatRequest(
    val model: String = "deepseek/deepseek-r1-zero:free",
    val messages: List<ChatMessage>,
    val stream: Boolean = false
)

// Modelo de respuesta
data class ChatResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
