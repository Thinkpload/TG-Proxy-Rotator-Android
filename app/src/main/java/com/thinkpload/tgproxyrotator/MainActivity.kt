package com.thinkpload.tgproxyrotator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProxyViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun refresh() {
        if (_state.value.loading) return
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { ProxyRepository.fetchAndCheck() }
                .onSuccess { list ->
                    val favs = Settings.favorites()
                    val sorted = list.sortedWith(
                        compareByDescending<Proxy> { it.key in favs }
                            .thenBy { it.pingMs ?: Int.MAX_VALUE }
                    )
                    _state.value = UiState(proxies = sorted, favorites = favs)
                }
                .onFailure { _state.value = UiState(error = it.message ?: "Error") }
        }
    }

    fun toggleFavorite(key: String) {
        Settings.toggleFavorite(key)
        _state.value = _state.value.copy(favorites = Settings.favorites())
    }

    data class UiState(
        val loading: Boolean = false,
        val proxies: List<Proxy> = emptyList(),
        val favorites: Set<String> = emptySet(),
        val error: String? = null,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        setContent {
            MaterialTheme {
                var showSettings by remember { mutableStateOf(false) }
                Surface(Modifier.fillMaxSize()) {
                    if (showSettings) {
                        SettingsScreen(onBack = { showSettings = false })
                    } else {
                        ProxyScreen(
                            onApply = ::applyProxy,
                            onOpenSettings = { showSettings = true },
                        )
                    }
                }
            }
        }
    }

    private fun applyProxy(p: Proxy) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.tgLink)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyScreen(
    onApply: (Proxy) -> Unit,
    onOpenSettings: () -> Unit,
    vm: ProxyViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TG Proxy Rotator") },
                actions = { TextButton(onClick = onOpenSettings) { Text("Settings") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.refresh() },
                text = { Text(if (state.loading) "Loading..." else "Refresh") },
                icon = {},
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
            if (state.loading && state.proxies.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.proxies) { p ->
                    val isFav = p.key in state.favorites
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(
                                    "${if (isFav) "★ " else ""}${p.server}:${p.port}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { vm.toggleFavorite(p.key) }) {
                                    Text(if (isFav) "Unfav" else "Fav")
                                }
                            }
                            Text("ping: ${p.pingMs ?: "?"} ms", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { onApply(p) }) { Text("Apply in Telegram") }
                        }
                    }
                }
            }
        }
    }
}
