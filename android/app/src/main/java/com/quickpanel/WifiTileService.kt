package com.quickpanel

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class WifiTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply { state = Tile.STATE_INACTIVE; label = "Wi-Fi"; updateTile() }
    }
    override fun onClick() = launchAndCollapse(WifiPanelActivity::class.java)
}
