package com.toolbox.core.ui.background

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.toolbox.core.datastore.DataStoreManager
import java.io.File
import javax.inject.Inject

/**
 * Manages custom background images for the app.
 */
class BackgroundManager @Inject constructor(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val backgroundsDir = File(context.filesDir, "backgrounds").apply { mkdirs() }

    /**
     * Save a background image from URI.
     */
    suspend fun saveBackground(uri: Uri): String {
        val fileName = "bg_${System.currentTimeMillis()}.jpg"
        val file = File(backgroundsDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        dataStoreManager.setBackgroundImage(file.absolutePath)
        return file.absolutePath
    }

    /**
     * Remove the current background.
     */
    suspend fun removeBackground() {
        val current = dataStoreManager.backgroundImage
        dataStoreManager.setBackgroundImage(null)
    }

    /**
     * Get the current background file path.
     */
    suspend fun getCurrentBackground(): String? {
        return dataStoreManager.backgroundImage.let { flow ->
            var path: String? = null
            flow.collect { path = it }
            path
        }
    }
}

/**
 * Composable that displays a background image if set.
 */
@Composable
fun BackgroundContainer(
    backgroundPath: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        backgroundPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f // Semi-transparent background
                    )
                }
            }
        }
        content()
    }
}
