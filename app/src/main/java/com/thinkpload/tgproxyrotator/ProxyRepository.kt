package com.thinkpload.tgproxyrotator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.util.concurrent.TimeUnit

data class Proxy(
    val server: String,
    val port: Int,
    val secret: String,
    val pingMs: Int? = null,
) {
    val key: String get() = "$server:$port"
    val tgLink: String get() = "tg://proxy?server=$server&port=$port&secret=$secret"
}

object ProxyRepository {

    private val ipv4Re = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchAndCheck(maxAlive: Int = 30): List<Proxy> = withContext(Dispatchers.IO) {
        val sources = Settings.enabledSources()
        val maxPing = Settings.maxPingMs
        val all = mutableListOf<Proxy>()
        val seen = mutableSetOf<String>()
        for (url in sources) {
            val text = runCatching {
                http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                    if (resp.isSuccessful) resp.body?.string() else null
                }
            }.getOrNull() ?: continue

            for (line in text.lineSequence()) {
                val p = parse(line.trim()) ?: continue
                if (seen.add(p.key)) all.add(p)
            }
            if (all.size >= 200) break
        }

        coroutineScope {
            all.shuffled().take(150).map { p ->
                async { check(p) }
            }.mapNotNull { it.await() }
                .filter { (it.pingMs ?: Int.MAX_VALUE) <= maxPing }
                .sortedBy { it.pingMs ?: Int.MAX_VALUE }
                .take(maxAlive)
        }
    }

    private fun parse(line: String): Proxy? {
        val normalized = when {
            line.startsWith("tg://proxy?") -> line
            line.startsWith("https://t.me/proxy?") ->
                "tg://proxy?" + line.substringAfter("?")
            else -> return null
        }
        val query = URI(normalized.replace("tg://", "https://x/")).query ?: return null
        val params = query.split("&").mapNotNull {
            val eq = it.indexOf('='); if (eq > 0) it.substring(0, eq) to it.substring(eq + 1) else null
        }.toMap()
        val server = params["server"] ?: return null
        val port = params["port"]?.toIntOrNull() ?: return null
        val secret = params["secret"] ?: return null
        if (ipv4Re.matches(server)) return null
        return Proxy(server, port, secret)
    }

    private fun check(p: Proxy): Proxy? {
        val start = System.currentTimeMillis()
        return runCatching {
            Socket().use { s ->
                s.connect(InetSocketAddress(p.server, p.port), 2500)
                p.copy(pingMs = (System.currentTimeMillis() - start).toInt())
            }
        }.getOrNull()
    }
}
