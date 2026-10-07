package com.quickpanel

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/** Invisible host: opens the system Wi-Fi panel (bottom-sheet popup) and closes when it's dismissed. */
class WifiPanelActivity : AppCompatActivity() {
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            launcher.launch(Intent(Settings.Panel.ACTION_WIFI))
        }
    }
}
