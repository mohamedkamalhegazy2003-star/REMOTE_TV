package com.remotetv.app

import android.content.Context
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException

sealed interface ConnState {
    data object Disconnected : ConnState
    data object Connecting : ConnState
    data class Connected(val name: String, val host: String) : ConnState
    data class Failed(val reason: String) : ConnState
}

/**
 * Talks to the Android TV through ADB-over-network (port 5555).
 * - All commands are serialized through one queue (no races, order preserved).
 * - A watchdog pings the TV and auto-reconnects when the link drops.
 * - Every blocking call is guarded by a timeout so the UI never freezes.
 */
class TvController(private val ctx: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()
    private val prefs = ctx.getSharedPreferences("remote", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<ConnState>(ConnState.Disconnected)
    val state: StateFlow<ConnState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val queue = Channel<suspend (Dadb) -> Unit>(64)
    private var dadb: Dadb? = null
    private var installed: List<String> = emptyList()
    @Volatile private var wantConnected = false

    var savedHost: String
        get() = prefs.getString("host", "") ?: ""
        set(v) { prefs.edit().putString("host", v).apply() }
    var savedName: String
        get() = prefs.getString("name", "") ?: ""
        set(v) { prefs.edit().putString("name", v).apply() }

    init {
        scope.launch {
            for (job in queue) {
                val d = dadb ?: continue
                if (_state.value !is ConnState.Connected) continue
                lock.withLock {
                    try {
                        guarded(15_000) { job(d) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        linkLost(e.message ?: "Connection lost")
                    }
                }
            }
        }
        scope.launch {
            while (true) {
                delay(4_000)
                try { watchdog() } catch (e: CancellationException) { throw e } catch (_: Throwable) {}
            }
        }
    }

    // ---------------------------------------------------------------- public API

    fun connect(host: String, name: String = "") {
        val h = host.trim()
        if (h.isEmpty()) return
        if (_state.value == ConnState.Connecting) return
        savedHost = h
        savedName = name.trim()
        wantConnected = true
        scope.launch { lock.withLock { doConnect(h, notify = true) } }
    }

    fun autoConnect() {
        if (savedHost.isNotEmpty() && _state.value !is ConnState.Connected && _state.value != ConnState.Connecting) {
            connect(savedHost, savedName)
        }
    }

    fun disconnect() {
        wantConnected = false
        scope.launch { lock.withLock { closeQuiet(); _state.value = ConnState.Disconnected } }
    }

    fun key(code: Int) = enqueue { it.shell("input keyevent $code") }

    fun sendText(text: String) {
        if (text.isEmpty()) return
        val safe = text.replace(" ", "%s").replace("'", "'\\''")
        enqueue { it.shell("input text '$safe'") }
    }

    fun openAllApps() = key(Key.ALL_APPS)

    fun launch(app: AppTarget) = enqueue { d ->
        if (installed.isEmpty()) installed = loadInstalled(d)
        val pkg = app.exact.firstOrNull { it in installed }
            ?: installed.firstOrNull { p -> app.keywords.any { p.contains(it, ignoreCase = true) } }
        if (pkg == null) {
            _messages.tryEmit("${app.label} is not installed on the TV")
            return@enqueue
        }
        var r = d.shell("monkey -p $pkg -c android.intent.category.LEANBACK_LAUNCHER 1")
        if (r.exitCode != 0 || r.allOutput.contains("No activities found")) {
            r = d.shell("monkey -p $pkg -c android.intent.category.LAUNCHER 1")
        }
        if (r.exitCode != 0 || r.allOutput.contains("No activities found")) {
            _messages.tryEmit("Could not open ${app.label}")
        }
    }

    /** Powers off the machine that runs Android TV (your laptop). */
    fun shutdown() {
        if (_state.value !is ConnState.Connected) { _messages.tryEmit("Not connected — tap the status to connect"); return }
        wantConnected = false
        _messages.tryEmit("Shutting down…")
        enqueue { d ->
            d.shell(
                "reboot -p || svc power shutdown || " +
                    "am start -a com.android.internal.intent.action.REQUEST_SHUTDOWN " +
                    "--ez android.intent.extra.KEY_CONFIRM false --activity-clear-task"
            )
        }
    }

    fun sleep() = key(Key.SLEEP)

    // ---------------------------------------------------------------- internals

    private fun enqueue(block: suspend (Dadb) -> Unit) {
        if (_state.value !is ConnState.Connected) {
            _messages.tryEmit("Not connected — tap the status to connect")
            return
        }
        queue.trySend(block)
    }

    private suspend fun <T> guarded(ms: Long, block: suspend () -> T): T {
        val job = scope.async { block() }
        return try {
            withTimeout(ms) { job.await() }
        } catch (e: TimeoutCancellationException) {
            closeQuiet()
            job.cancel()
            throw IOException("Timed out")
        }
    }

    private fun keys(): AdbKeyPair {
        val priv = File(ctx.filesDir, "adbkey")
        val pub = File(ctx.filesDir, "adbkey.pub")
        if (!priv.exists() || !pub.exists()) AdbKeyPair.generate(priv, pub)
        return AdbKeyPair.read(priv, pub)
    }

    private fun closeQuiet() {
        runCatching { dadb?.close() }
        dadb = null
    }

    private fun linkLost(reason: String) {
        closeQuiet()
        _state.value = ConnState.Failed(reason)
    }

    private fun fail(msg: String, notify: Boolean) {
        closeQuiet()
        _state.value = ConnState.Failed(msg)
        if (notify) _messages.tryEmit(msg)
    }

    private fun loadInstalled(d: Dadb): List<String> =
        d.shell("pm list packages").output.lines()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:") }

    private suspend fun doConnect(host: String, notify: Boolean) {
        _state.value = ConnState.Connecting
        closeQuiet()
        while (queue.tryReceive().isSuccess) { /* drop stale presses */ }
        try {
            if (!NetScan.portOpen(host, NetScan.ADB_PORT, 2_500)) {
                fail("Cannot reach $host:${NetScan.ADB_PORT}. Enable ADB over network on the TV.", notify)
                return
            }
            val d = Dadb.create(host, NetScan.ADB_PORT, keys())
            dadb = d
            // First call performs the handshake; the TV may show an "Allow debugging" prompt.
            val model = guarded(35_000) { d.shell("getprop ro.product.model").output.trim() }
            installed = guarded(15_000) { loadInstalled(d) }
            val name = savedName.ifBlank { model.ifBlank { "Android TV" } }
            _state.value = ConnState.Connected(name, host)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            fail(e.message?.takeIf { it.isNotBlank() } ?: "Connection failed", notify)
        }
    }

    private suspend fun watchdog() {
        when (_state.value) {
            is ConnState.Connected -> lock.withLock {
                val d = dadb ?: return
                try {
                    guarded(6_000) { d.shell("echo ok") }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    linkLost("Connection lost")
                }
            }
            is ConnState.Failed, ConnState.Disconnected -> {
                if (wantConnected && savedHost.isNotBlank()) {
                    lock.withLock {
                        if (_state.value !is ConnState.Connected) doConnect(savedHost, notify = false)
                    }
                }
            }
            else -> {}
        }
    }
}
