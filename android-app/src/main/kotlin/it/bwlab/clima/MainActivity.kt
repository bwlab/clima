package it.bwlab.clima

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import it.bwlab.clima.data.ThemePreferences
import it.bwlab.clima.navigation.ClimaNavHost
import it.bwlab.clima.ui.theme.ClimaTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[AppViewModel::class.java]
        setContent {
            ClimaTheme(theme = ThemePreferences.currentTheme.collectAsState().value) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ClimaNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
