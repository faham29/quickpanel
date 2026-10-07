package com.quickpanel

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class VolumeTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = "Volume"
            subtitle = "Music / Call"
            icon = Icon.createWithResource(this@VolumeTileService, R.drawable.ic_volume_tile)
            updateTile()
        }
    }
    override fun onClick() = launchAndCollapse(VolumeTrampolineActivity::class.java)
}
