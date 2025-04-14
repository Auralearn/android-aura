package findit.edversity.auralearn.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class MaterialListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuralearnTheme {
                MaterialListScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun MaterialListScreen(onBackPressed: () -> Unit) {
    val allMaterials = remember {
        listOf(
            "Matematika Kelas 1 SD", "Matematika Kelas 2 SD", "Matematika Kelas 3 SD",
            "Matematika Kelas 4 SD", "Matematika Kelas 5 SD", "Matematika Kelas 6 SD",
            "IPA Kelas 1 SMP", "IPA Kelas 2 SMP", "IPA Kelas 3 SMP",
            "Bahasa Indonesia Kelas 1 SMA", "Bahasa Indonesia Kelas 2 SMA", "Bahasa Indonesia Kelas 3 SMA"
        )
    }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filterLevel by remember { mutableStateOf("ALL") }

    val filteredMaterials = allMaterials
        .filter {
            val matchesSearch = it.contains(searchQuery.text, ignoreCase = true)
            val matchesFilter = when (filterLevel) {
                "SD" -> it.contains("SD")
                "SMP" -> it.contains("SMP")
                "SMA" -> it.contains("SMA")
                else -> true
            }
            matchesSearch && matchesFilter
        }

    val sdMaterials = filteredMaterials.filter { it.contains("SD") }
    val smpMaterials = filteredMaterials.filter { it.contains("SMP") }
    val smaMaterials = filteredMaterials.filter { it.contains("SMA") }

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
            TopBar(onBackPressed)

            Text(
                text = "Daftar Materi",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBarAndFilter(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterChange = {
                    filterLevel = when (filterLevel) {
                        "ALL" -> "SD"
                        "SD" -> "SMP"
                        "SMP" -> "SMA"
                        "SMA" -> "ALL"
                        else -> "ALL"
                    }
                },
                currentFilter = filterLevel
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (sdMaterials.isNotEmpty()) {
                    item {
                        SectionHeader("Sekolah Dasar")
                    }
                    items(sdMaterials) { MaterialItem(it) }
                }

                if (smpMaterials.isNotEmpty()) {
                    item {
                        SectionHeader("Sekolah Menengah Pertama")
                    }
                    items(smpMaterials) { MaterialItem(it) }
                }

                if (smaMaterials.isNotEmpty()) {
                    item {
                        SectionHeader("Sekolah Menengah Atas")
                    }
                    items(smaMaterials) { MaterialItem(it) }
                }

                // Add buffer space at the end of the list
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TopBar(onBackPressed: () -> Unit) {
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
}

@Composable
fun SearchBarAndFilter(
    searchQuery: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    onFilterChange: () -> Unit,
    currentFilter: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
            modifier = Modifier
                .weight(1f)
                .background(White.copy(alpha = 0.35f), shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onFilterChange) {
            Icon(
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = "Filter",
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.75f)
            )
        }
    }

    Text(
        text = if (currentFilter == "ALL") "Filter: Semua" else "Filter: $currentFilter",
        color = Color.White,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        textAlign = TextAlign.End,
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun MaterialItem(material: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.35f)),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Text(
            text = material,
            style = MaterialTheme.typography.bodyLarge.copy(color = White),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaterialListScreenPreview() {
    AuralearnTheme {
        MaterialListScreen(onBackPressed = {})
    }
}
