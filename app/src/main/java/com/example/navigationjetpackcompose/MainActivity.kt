package com.example.navigationjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var message by remember { mutableStateOf("") }
    // Pair(mensaje, esUsuario)
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    var response by remember { mutableStateOf("Esperando respuesta...") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {TopBarComponent("Chat De Leyes")},
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(8.dp),
                backgroundColor = Color.White
            ) {
                MessageInput(
                    message = message,
                    onMessageChange = {message = it},
                    onSendClick = {
                        if (message.isNotEmpty()) {
                            messages.add(Pair(message, true))
                            message = ""
                            coroutineScope.launch {
                                response = sendMessageToApi(message).toString()
                                messages.add(Pair("Respuesta automática", false))
                            }
                        }
                    },
                    onMicClick = {  }
                )
            }
        }
    ) { innerPadding ->
        ChatContent(
            messages = messages,
            modifier = Modifier.padding(innerPadding)
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
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Escribe un mensaje...") },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .weight(1f)
                .background(Color.White),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendClick() })
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
fun ChatContent(messages: List<Pair<String, Boolean>>, modifier: Modifier = Modifier) {
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
            items(messages.size) { index ->
                val (message, isUser) = messages[index]
                MessageBubble(message, isUser)
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

fun sendMessageToApi(message:String){
    
}




@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyScreen()
}
