package com.thinkpload.tgproxyrotator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var maxPing by remember { mutableIntStateOf(Settings.maxPingMs) }
    val sources = remember { Settings.DEFAULT_SOURCES }
    val sourceEnabled = remember {
        mutableStateMapOf<String, Boolean>().apply {
            sources.forEach { put(it, Settings.isSourceEnabled(it)) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Max ping: $maxPing ms", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = maxPing.toFloat(),
                onValueChange = { maxPing = it.toInt() },
                onValueChangeFinished = { Settings.maxPingMs = maxPing },
                valueRange = 100f..3000f,
                steps = 28,
            )

            HorizontalDivider()
            Text("Proxy sources", style = MaterialTheme.typography.titleMedium)
            sources.forEach { url ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(
                        checked = sourceEnabled[url] == true,
                        onCheckedChange = {
                            sourceEnabled[url] = it
                            Settings.setSourceEnabled(url, it)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        url.substringAfterLast('/'),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
