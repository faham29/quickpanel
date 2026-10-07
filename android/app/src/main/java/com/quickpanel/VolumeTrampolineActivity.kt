package com.quickpanel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

/** No-UI activity: only exists so the tile can collapse the shade. Shows the overlay and closes at once. */
class VolumeTrampolineActivity : Activity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.canDrawOverlays(this)) {
            VolumeOverlay.show(applicationContext)
        } else {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        finish()
        overridePendingTransition(0, 0)
    }
}
