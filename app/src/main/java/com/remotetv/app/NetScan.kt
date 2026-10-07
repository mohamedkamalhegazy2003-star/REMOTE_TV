package com.remotetv.app

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

object NetScan {
    const val ADB_PORT = 5555

    fun portOpen(host: String, port: Int, timeoutMs: Int): Boolean = try {
        Socket().use { it.connect(InetSocketAddress(host, port), timeoutMs); true }
    } catch (e: Exception) {
        false
    }

    private fun localPrefix(): String? {
        val ifaces = NetworkInterface.getNetworkInterfaces() ?: return null
        for (ni in ifaces) {
            if (!ni.isUp || ni.isLoopback) continue
            for (a in ni.inetAddresses) {
                if (a is Inet4Address && a.isSiteLocalAddress) {
                    return a.hostAddress?.substringBeforeLast('.')
                }
            }
        }
        return null
    }

    /** Scans the local /24 for devices that have ADB over network (port 5555) open. */
    suspend fun scan(): List<String> = withContext(Dispatchers.IO) {
        val prefix = localPrefix() ?: return@withContext emptyList<String>()
        val sem = Semaphore(64)
        coroutineScope {
            (1..254).map { i ->
                async {
                    sem.withPermit {
                        val h = "$prefix.$i"
                        if (portOpen(h, ADB_PORT, 400)) h else null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
}
