package com.example.taskmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanager.utils.md_theme_light_primary
import com.example.taskmanager.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val colorList = listOf(
        md_theme_light_primary, Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan,
        Color.Yellow,
        Color(0xFFFFA500), // Orange
        Color(0xFF8B4513), // Brown
        Color(0xFF4B0082), // Indigo
        Color(0xFF00FFFF), // Aqua
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFF7FFF00), // Chartreuse
        Color(0xFF4682B4), // Steel Blue
        Color(0xFF800000), // Maroon
        Color(0xFFDC143C), // Crimson
        Color(0xFF32CD32), // Lime Green
        Color(0xFFFF4500), // Orange Red
        Color(0xFF00BFFF), // Deep Sky Blue
        Color(0xFF9370DB), // Medium Purple
        Color(0xFF20B2AA), // Light Sea Green
        Color(0xFFFF6347), // Tomato
        Color(0xFFB22222)  // Firebrick
    )



    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val selectedColor by viewModel.primaryColor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            // Dark Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleDarkTheme(!darkTheme) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme(it) } // ✅ FIXED: Uses actual state
                )
            }

            Divider()

            // Primary Color Picker
            Text("Primary Color", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Horizontal spacing
                modifier = Modifier.padding(vertical = 8.dp) // Vertical spacing
            ) {
                items(colorList) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { viewModel.setPrimaryColor(color) }
                            .border(2.dp, if (color == selectedColor) Color.Black else Color.Transparent, CircleShape)
                            .padding(4.dp) // Internal padding for better spacing
                    )
                }
            }
        }
    }
}

