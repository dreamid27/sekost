package id.my.selo.sekost

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.ThemeMode
import id.my.selo.sekost.ui.SekostApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.themeMode.value == null || viewModel.startWithOnboarding.value == null }

        setContent {
            val mode by viewModel.themeMode.collectAsStateWithLifecycle()
            val startWithOnboarding by viewModel.startWithOnboarding.collectAsStateWithLifecycle()
            val dark = when (mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            SekostTheme(darkTheme = dark) {
                startWithOnboarding?.let { SekostApp(startWithOnboarding = it) }
            }
        }
    }
}
