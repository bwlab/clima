package it.bwlab.clima

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val theme by ThemePreferences.currentTheme.collectAsState()
            ClimaTheme(theme = theme) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ClimaNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
