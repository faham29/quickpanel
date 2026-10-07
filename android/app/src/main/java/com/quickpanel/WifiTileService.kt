package com.quickpanel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class WifiTileService : TileService() {
    private val cm by lazy { getSystemService(ConnectivityManager::class.java) }
    private val wm by lazy { applicationContext.getSystemService(WifiManager::class.java) }

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(n: Network) = update()
        override fun onLost(n: Network) = update()
        override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) = update()
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) = update()
    }

    override fun onStartListening() {
        update()
        cm.registerNetworkCallback(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
            callback, Handler(Looper.getMainLooper())
        )
        registerReceiver(receiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    }

    override fun onStopListening() {
        runCatching { cm.unregisterNetworkCallback(callback) }
        runCatching { unregisterReceiver(receiver) }
    }

    override fun onClick() = launchAndCollapse(WifiPanelActivity::class.java)

    @Suppress("DEPRECATION")
    private fun update() {
        val tile = qsTile ?: return
        val enabled = wm.isWifiEnabled
        val connected = enabled && cm.allNetworks.any {
            cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
        val ssid = if (connected) {
            wm.connectionInfo?.ssid?.trim('"')?.takeIf { it.isNotEmpty() && it != "<unknown ssid>" }
        } else null

        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Wi-Fi"
        tile.subtitle = when {
            !enabled -> "Off"
            !connected -> "Not connected"
            else -> ssid ?: "Connected"
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_wifi_tile)
        tile.updateTile()
    }
}
