package com.example.miguel_benavides_lab12

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageClassifierScreen()
                }
            }
        }
    }
}

@Composable
fun ImageClassifierScreen() {
    val context = LocalContext.current
    // Estado para guardar el texto del resultado
    var resultText by remember { mutableStateOf("Presiona el botón para clasificar") }

    // Cargamos la imagen desde assets al iniciar
    // Usamos 'remember' para no cargarla cada vez que se redibuja la pantalla
    val bitmap: Bitmap? = remember {
        try {
            val inputStream = context.assets.open("flower1.jpg")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Hacemos la pantalla scrolleable
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 1. Mostrar la imagen
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen a clasificar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Altura fija o adaptable
            )
        } else {
            Text("Error al cargar la imagen 'flower1.jpg' de assets")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Botón para activar ML Kit
        Button(
            onClick = {
                if (bitmap != null) {
                    classifyImage(bitmap) { result ->
                        resultText = result
                    }
                }
            }
        ) {
            Text(text = "Etiquetar Imagen")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Mostrar Resultados
        Text(
            text = "Resultados:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = resultText,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Función auxiliar para la lógica de ML Kit
fun classifyImage(bitmap: Bitmap, onResult: (String) -> Unit) {
    // Preparar la imagen para ML Kit
    val image = InputImage.fromBitmap(bitmap, 0)

    // Obtener el cliente de etiquetado (usando opciones por defecto)
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    // Procesar
    labeler.process(image)
        .addOnSuccessListener { labels ->
            // Éxito: Formatear los resultados
            val builder = StringBuilder()
            for (label in labels) {
                val text = label.text
                val confidence = label.confidence
                builder.append("$text: ${(confidence * 100).toInt()}%\n")
            }

            if (labels.isEmpty()) {
                onResult("No se detectaron etiquetas.")
            } else {
                onResult(builder.toString())
            }
        }
        .addOnFailureListener { e ->
            // Error
            onResult("Error: ${e.message}")
        }
}