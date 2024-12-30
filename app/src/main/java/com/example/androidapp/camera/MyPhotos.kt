package com.example.androidapp.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyPhotos(
    modifier: Modifier = Modifier,
    onImageUriChanged: (String?) -> Unit // <--- Callback pentru a notifica părinte
) {
    var imageUri by remember { mutableStateOf(EMPTY_IMAGE_URI) }

    // Notifică părinte atunci când imageUri se schimbă
    LaunchedEffect(imageUri) {
        onImageUriChanged((if (imageUri == EMPTY_IMAGE_URI) null else imageUri).toString())
    }

    if (imageUri != EMPTY_IMAGE_URI) {
        // Afișăm imaginea capturată sau selectată
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(imageUri),
                contentDescription = "Captured image"
            )
            Button(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    imageUri = EMPTY_IMAGE_URI
                }
            ) {
                Text("Remove image")
            }
        }
    } else {
        var showGallerySelect by remember { mutableStateOf(false) }
        if (showGallerySelect) {
            // Afișăm selecția din galerie
            GallerySelect(
                modifier = modifier,
                onImageUri = { uri ->
                    showGallerySelect = false
                    imageUri = uri
                }
            )
        } else {
            // Afișăm camera
            Box(modifier = modifier) {
                CameraCapture(
                    modifier = modifier,
                    onImageFile = { file ->
                        imageUri = file.toUri()// Asigură-te că este String
                    }
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(4.dp),
                    onClick = {
                        showGallerySelect = true
                    }
                ) {
                    Text("Select from Gallery")
                }
            }
        }
    }
}
