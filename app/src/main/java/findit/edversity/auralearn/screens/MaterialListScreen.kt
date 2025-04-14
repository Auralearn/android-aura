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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
fun MaterialListScreen(
    onBackPressed: () -> Unit,
    onMaterialClick: (String) -> Unit
) {
    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch materials when the screen is first displayed
    LaunchedEffect(Unit) {
        try {
            val fetchedMaterials = withContext(Dispatchers.IO) {
                fetchMaterials()
            }
            materials = fetchedMaterials
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filterLevel by remember { mutableStateOf("ALL") }

    val filteredMaterials = materials
        .filter {
            val matchesSearch = it.title.contains(searchQuery.text, ignoreCase = true)
            val matchesFilter = when (filterLevel) {
                "SD" -> it.title.contains("SD")
                "SMP" -> it.title.contains("SMP")
                "SMA" -> it.title.contains("SMA")
                else -> true
            }
            matchesSearch && matchesFilter
        }

    val sdMaterials = filteredMaterials.filter { it.title.contains("SD") }
    val smpMaterials = filteredMaterials.filter { it.title.contains("SMP") }
    val smaMaterials = filteredMaterials.filter { it.title.contains("SMA") }

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

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            // Top Bar
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
                text = "Daftar Materi",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar and Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                    modifier = Modifier
                        .weight(1f)
                        .background(White.copy(alpha = 0.35f), shape = MaterialTheme.shapes.medium)
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    filterLevel = when (filterLevel) {
                        "ALL" -> "SD"
                        "SD" -> "SMP"
                        "SMP" -> "SMA"
                        "SMA" -> "ALL"
                        else -> "ALL"
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(0.75f)
                    )
                }
            }

            Text(
                text = if (filterLevel == "ALL") "Filter: Semua" else "Filter: $filterLevel",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.End,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading materials...", color = White)
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: $error", color = White)
                    }
                }
                filteredMaterials.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No materials found", color = White)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (sdMaterials.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Sekolah Dasar",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(sdMaterials) { material ->
                                MaterialItem(material.title) {
                                    onMaterialClick(material.id)
                                }
                            }
                        }

                        if (smpMaterials.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Sekolah Menengah Pertama",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(smpMaterials) { material ->
                                MaterialItem(material.title) {
                                    onMaterialClick(material.id)
                                }
                            }
                        }

                        if (smaMaterials.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Sekolah Menengah Atas",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(smaMaterials) { material ->
                                MaterialItem(material.title) {
                                    onMaterialClick(material.id)
                                }
                            }
                        }

                        // Add buffer space at the end of the list
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialItem(material: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.35f)),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        onClick = onClick
    ) {
        Text(
            text = material,
            style = MaterialTheme.typography.bodyLarge.copy(color = White),
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Data classes for API responses
data class Material(
    val id: String,
    val title: String
)

// API functions
fun fetchMaterials(): List<Material> {
    val url = URL("http://10.0.2.2:8000/api/materials/")
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
                val dataArray = jsonObject.getJSONArray("data")
                val materials = mutableListOf<Material>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    materials.add(
                        Material(
                            id = item.getString("id"),
                            title = item.getString("title")
                        )
                    )
                }
                materials
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
fun MaterialListScreenPreview() {
    AuralearnTheme {
        MaterialListScreen(
            onBackPressed = {},
            onMaterialClick = {}
        )
    }
}