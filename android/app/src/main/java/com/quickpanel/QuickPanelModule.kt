package com.quickpanel

import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ViewManager

class QuickPanelModule(private val ctx: ReactApplicationContext) : ReactContextBaseJavaModule(ctx) {
    override fun getName() = "QuickPanel"

    /** Android 13+: shows the system "Add tile?" prompt. Resolves "unsupported" on Android 12. */
    @ReactMethod
    fun requestAddTile(type: String, promise: Promise) {
        if (Build.VERSION.SDK_INT < 33) { promise.resolve("unsupported"); return }
        val wifi = type == "wifi"
        val cls = if (wifi) WifiTileService::class.java else VolumeTileService::class.java
        val sbm = ctx.getSystemService(StatusBarManager::class.java)
        sbm.requestAddTileService(
            ComponentName(ctx, cls),
            if (wifi) "Wi-Fi" else "Volume",
            Icon.createWithResource(ctx, if (wifi) R.drawable.ic_wifi_tile else R.drawable.ic_volume_tile),
            ctx.mainExecutor
        ) { result -> promise.resolve(result.toString()) }
    }
}

class QuickPanelPackage : ReactPackage {
    override fun createNativeModules(c: ReactApplicationContext): List<NativeModule> = listOf(QuickPanelModule(c))
    override fun createViewManagers(c: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}
