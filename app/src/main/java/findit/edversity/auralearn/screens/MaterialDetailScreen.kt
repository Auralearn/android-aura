package findit.edversity.auralearn.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import findit.edversity.auralearn.R
import findit.edversity.auralearn.ui.theme.AuralearnTheme
import findit.edversity.auralearn.ui.theme.Black
import findit.edversity.auralearn.ui.theme.CyanTertiary
import findit.edversity.auralearn.ui.theme.PurplePrimary
import findit.edversity.auralearn.ui.theme.PurpleSecondary
import findit.edversity.auralearn.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun MaterialDetailScreen(materialId: String, onBackPressed: () -> Unit) {
    var materialDetail by remember { mutableStateOf<MaterialDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LocalContext.current

    LaunchedEffect(materialId) {
        try {
            val fetchedMaterial = withContext(Dispatchers.IO) {
                fetchMaterialDetail(materialId)
            }
            materialDetail = fetchedMaterial
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PurpleSecondary, Black)
                )
            )
    ) {
        // Background blurred circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        val size = size
                        val circleSize = size.minDimension * 0.35f

                        // Purple circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(0.3f * size.width, 0.7f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.3f * size.width, 0.7f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )

                        // Cyan circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyanTertiary.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(0.7f * size.width, 0.3f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.7f * size.width, 0.3f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )
                    }
                }
                .blur(radius = 32.dp)
        )

        when {
            isLoading -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.75f)
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.app_logo_home),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.fillMaxWidth(0.33f))
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading material details...", color = White)
                    }
                }
            }
            error != null -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.75f)
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.app_logo_home),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.fillMaxWidth(0.33f))
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: $error", color = White)
                    }
                }
            }
            materialDetail == null -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.75f)
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.app_logo_home),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.fillMaxWidth(0.33f))
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Material not found", color = White)
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.75f)
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.app_logo_home),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.fillMaxWidth(0.33f))
                    }

                    Text(
                        text = materialDetail!!.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = White,
                        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    )

                    LazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(materialDetail!!.chapters) { chapter ->
                            ChapterItem(chapter)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.35f)),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium,
                color = White
            )
            chapter.content.forEach { contentItem ->
                Text(
                    text = contentItem.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


data class MaterialDetail(
    val id: String,
    val title: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val id: String,
    val title: String,
    val subtitle: String,
    val content: List<ChapterContent>
)

data class ChapterContent(
    val text: String,
    val images: List<MediaItem>,
    val videos: List<MediaItem>,
    val audios: List<MediaItem>
)

data class MediaItem(
    val url: String,
    val caption: String
)

// API functions
fun fetchMaterialDetail(materialId: String): MaterialDetail {
    val url = URL("http://10.0.2.2:8000/api/materials/$materialId")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 5000
    connection.readTimeout = 5000

    return try {
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = bufferedReader.use { it.readText() }
            val jsonObject = JSONObject(response)

            if (jsonObject.getBoolean("success")) {
                val data = jsonObject.getJSONObject("data")
                val chaptersArray = data.getJSONArray("chapters")
                val chapters = mutableListOf<Chapter>()

                for (i in 0 until chaptersArray.length()) {
                    val chapterObj = chaptersArray.getJSONObject(i)
                    val contentArray = chapterObj.getJSONArray("content")
                    val content = mutableListOf<ChapterContent>()

                    for (j in 0 until contentArray.length()) {
                        val contentObj = contentArray.getJSONObject(j)
                        val imagesArray = contentObj.getJSONArray("images")
                        val videosArray = contentObj.getJSONArray("videos")
                        val audiosArray = contentObj.getJSONArray("audios")

                        val images = mutableListOf<MediaItem>()
                        val videos = mutableListOf<MediaItem>()
                        val audios = mutableListOf<MediaItem>()

                        for (k in 0 until imagesArray.length()) {
                            val imageObj = imagesArray.getJSONObject(k)
                            images.add(MediaItem(imageObj.getString("url"), imageObj.getString("caption")))
                        }

                        for (k in 0 until videosArray.length()) {
                            val videoObj = videosArray.getJSONObject(k)
                            videos.add(MediaItem(videoObj.getString("url"), videoObj.getString("caption")))
                        }

                        for (k in 0 until audiosArray.length()) {
                            val audioObj = audiosArray.getJSONObject(k)
                            audios.add(MediaItem(audioObj.getString("url"), audioObj.getString("caption")))
                        }

                        content.add(
                            ChapterContent(
                                text = contentObj.getString("text"),
                                images = images,
                                videos = videos,
                                audios = audios
                            )
                        )
                    }

                    chapters.add(
                        Chapter(
                            id = chapterObj.getString("id"),
                            title = chapterObj.getString("title"),
                            subtitle = chapterObj.getString("subtitle"),
                            content = content
                        )
                    )
                }

                MaterialDetail(
                    id = data.getString("id"),
                    title = data.getString("title"),
                    chapters = chapters
                )
            } else {
                throw Exception(jsonObject.getString("error"))
            }
        } else {
            throw Exception("HTTP error: $responseCode")
        }
    } finally {
        connection.disconnect()
    }
}

@Preview(showBackground = true)
@Composable
fun MaterialDetailScreenPreview() {
    AuralearnTheme {
        MaterialDetailScreen(
            materialId = "1",
            onBackPressed = {}
        )
    }
}
