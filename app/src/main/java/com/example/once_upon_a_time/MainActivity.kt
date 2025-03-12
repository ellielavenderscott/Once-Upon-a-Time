package com.example.once_upon_a_time

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.once_upon_a_time.ui.theme.Once_Upon_A_TimeTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Once_Upon_A_TimeTheme {
                BarcodeScannerScreen()
            }
        }
    }
}

@Composable
fun BarcodeScannerScreen() {
    val context = LocalContext.current
    var barcodeResult by remember { mutableStateOf("Scan a barcode") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )}

    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val imageBitmap = result.data?.extras?.get("data") as? Bitmap
        if (imageBitmap != null) {
            capturedBitmap = imageBitmap
            scanBarcode(imageBitmap) { resultText ->
                barcodeResult = resultText ?: "No barcode found"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = barcodeResult, style = MaterialTheme.typography.headlineMedium)

        capturedBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        scanBarcode(bitmap) { result ->
                            barcodeResult = result ?: "No barcode found"
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (hasCameraPermission) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureLauncher.launch(intent)
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text("Open Camera")
        }
    }
}

fun scanBarcode(bitmap: Bitmap, onResult: (String?) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val scanner = BarcodeScanning.getClient()

    scanner.process(image)
        .addOnSuccessListener { barcodes: List<Barcode> ->
            val result = barcodes.firstOrNull()?.displayValue
            onResult(result)
        }
        .addOnFailureListener {
            onResult(null)
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewBarcodeScannerScreen() {
    Once_Upon_A_TimeTheme {
        BarcodeScannerScreen()
    }
}
