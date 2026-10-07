package com.quickpanel

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService

/** Launches an activity from a tile and collapses the shade (API 34+ needs PendingIntent). */
fun TileService.launchAndCollapse(target: Class<*>) {
    val intent = Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= 34) {
        val pi = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        startActivityAndCollapse(pi)
    } else {
        @Suppress("DEPRECATION")
        startActivityAndCollapse(intent)
    }
}
