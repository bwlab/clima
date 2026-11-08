package it.bwlab.clima

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import it.bwlab.gree.core.*

@Composable
fun ClimateScreen(
    viewModel: ClimateViewModel,
    currentIp: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Climatizzatore Gree",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var ip by remember { mutableStateOf(currentIp) }
        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("IP del climatizzatore") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.discover() },
                enabled = !viewModel.loading.value
            ) {
                Text("Scan rete")
            }
            Button(
                onClick = { viewModel.bind(ip) },
                enabled = !viewModel.loading.value
            ) {
                Text("Bind")
            }
            Button(
                onClick = { viewModel.loadStatus(ip) },
                enabled = !viewModel.loading.value
            ) {
                Text("Stato")
            }
        }

        val msg by viewModel.message.collectAsState()
        val loading by viewModel.loading.collectAsState()
        if (msg.isNotEmpty()) {
            Text(
                text = msg,
                style = MaterialTheme.typography.body2,
                color = if (loading) Color.Gray else Color.Blue,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (loading) {
            CircularProgressIndicator(Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        val state = viewModel.state
        if (state != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.pow) "ACCESO" else "SPENTO",
                            style = MaterialTheme.typography.h6,
                            color = if (state.pow) Color.Green else Color.Red,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${state.temSenCorrected()} °C",
                            style = MaterialTheme.typography.h4,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Modalità: ${state.mode}", style = MaterialTheme.typography.body1)
                    Text(text = "Temperatura impostata: ${state.setTem} °C", style = MaterialTheme.typography.body1)
                    Text(text = "Ventola: ${state.wdSpd}", style = MaterialTheme.typography.body1)
                    Text(
                        text = "Ambiente: ${state.temSenCorrected()} °C",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.power(ip, true) },
                    enabled = !viewModel.loading.value
                ) {
                    Text("ACCENDI")
                }
                Button(
                    onClick = { viewModel.power(ip, false) },
                    enabled = !viewModel.loading.value
                ) {
                    Text("SPEGNI")
                }
            }

            var temp by remember { mutableStateOf(state.setTem.coerceIn(16, 30)) }
            Slider(
                value = temp.toFloat(),
                onValueChange = { temp = it.toInt() },
                valueRange = 16f..30f,
                onValueChangeFinished = {
                    viewModel.temperature(ip, temp)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = "Temperatura: $temp °C",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(text = "Modalità", style = MaterialTheme.typography.subtitle1)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                for (m in Mode.values()) {
                    Button(
                        onClick = { viewModel.mode(ip, m) },
                        enabled = !viewModel.loading.value
                    ) {
                        Text(m.name)
                    }
                }
            }

            Text(text = "Ventola", style = MaterialTheme.typography.subtitle1)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                for (f in FanSpeed.values()) {
                    Button(
                        onClick = { viewModel.fan(ip, f) },
                        enabled = !viewModel.loading.value
                    ) {
                        Text(f.name)
                    }
                }
            }
        } else {
            Text(
                text = "Nessuno stato disponibile. Esegui Bind o Stato.",
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}
