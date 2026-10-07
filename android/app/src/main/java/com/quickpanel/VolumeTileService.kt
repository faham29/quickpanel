package com.quickpanel

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class VolumeTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply { state = Tile.STATE_INACTIVE; label = "Volume"; updateTile() }
    }
    override fun onClick() = launchAndCollapse(VolumePopupActivity::class.java)
}
