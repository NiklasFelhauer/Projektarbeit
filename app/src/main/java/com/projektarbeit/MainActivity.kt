package com.projektarbeit

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projektarbeit.home.MQTT.MqttScreen
import com.projektarbeit.home.camera.CameraScreen
import com.projektarbeit.ui.ApplicationTheme
import com.projektarbeit.ui.darkBlue
import com.projektarbeit.ui.logo_background

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel by viewModels { MainViewModel.getFactory(this) }
            val uiState by viewModel.uiState.collectAsState()

            var tabState by remember { mutableStateOf(Tab.Camera) }

            LaunchedEffect(uiState.errorMessage) {
                if (uiState.errorMessage != null) {
                    Toast.makeText(
                        this@MainActivity, "${uiState.errorMessage}", Toast.LENGTH_SHORT
                    ).show()
                    viewModel.errorMessageShown()
                }
            }

            ApplicationTheme {
                Column {
                    Header()
                    Content(
                        uiState = uiState,
                        tab = tabState,
                        onTabChanged = {
                            tabState = it
                            viewModel.stopDetect()
                        },
                        onImageProxyAnalyzed = { imageProxy ->
                            viewModel.detectImageObject(imageProxy)
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Header() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = logo_background,
            ),
            title = {
                Image(
                    modifier = Modifier.size(80.dp),
                    alignment = Alignment.CenterStart,
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                )
            },
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Composable
    fun Content(
        uiState: UiState,
        tab: Tab,
        modifier: Modifier = Modifier,
        onTabChanged: (Tab) -> Unit,
        onImageProxyAnalyzed: (ImageProxy) -> Unit,
    ) {
        val tabs = listOf(Tab.Camera, Tab.Mqtt) // Zwei Tabs
        Column(modifier) {
            TabRow(containerColor = darkBlue, selectedTabIndex = tab.ordinal) {
                tabs.forEach { t ->
                    Tab(
                        text = {
                            Text(
                                text = t.name,
                                color = if (tab == t) Color.Cyan else Color.White,
                                textAlign = TextAlign.Center
                            )
                        },
                        selected = tab == t,
                        onClick = { onTabChanged(t) },
                    )
                }
            }

            when (tab) {
                Tab.Camera -> CameraScreen(
                    uiState = uiState,
                    onImageAnalyzed = { imageProxy ->
                        onImageProxyAnalyzed(imageProxy)
                    },
                )

                Tab.Mqtt -> MqttScreen()
            }
        }
    }

    enum class Tab {
        Camera, Mqtt
    }
}
