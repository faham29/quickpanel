package com.quickpanel

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/** Bottom popup with a Music / Call selector and a volume slider. */
class VolumePopupActivity : AppCompatActivity() {
    private lateinit var audio: AudioManager
    private lateinit var seek: SeekBar
    private lateinit var valueText: TextView
    private var stream = AudioManager.STREAM_MUSIC
    private val prefs by lazy { getSharedPreferences("quickpanel", Context.MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        stream = prefs.getInt("stream", AudioManager.STREAM_MUSIC)

        val root = FrameLayout(this).apply { setOnClickListener { finish() } }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(28))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F2202124"))
                cornerRadii = floatArrayOf(dp(28).toFloat(), dp(28).toFloat(), dp(28).toFloat(),
                    dp(28).toFloat(), 0f, 0f, 0f, 0f)
            }
            isClickable = true
        }
        root.addView(card, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM))

        val toggle = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val music = RadioButton(this).apply { text = "Music"; setTextColor(Color.WHITE); id = 1 }
        val call = RadioButton(this).apply { text = "Call"; setTextColor(Color.WHITE); id = 2 }
        toggle.addView(music); toggle.addView(call)
        card.addView(toggle)

        valueText = TextView(this).apply { setTextColor(Color.WHITE); textSize = 16f; setPadding(0, dp(16), 0, 0) }
        card.addView(valueText)

        seek = SeekBar(this)
        card.addView(seek, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                if (fromUser) audio.setStreamVolume(stream, p + minVol(), 0)
                refreshLabel()
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        toggle.setOnCheckedChangeListener { _, id ->
            stream = if (id == 2) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
            prefs.edit().putInt("stream", stream).apply()
            syncSeek()
        }
        toggle.check(if (stream == AudioManager.STREAM_VOICE_CALL) 2 else 1)
        setContentView(root)
    }

    private fun minVol() = if (Build.VERSION.SDK_INT >= 28) audio.getStreamMinVolume(stream) else 0

    private fun syncSeek() {
        seek.max = audio.getStreamMaxVolume(stream) - minVol()
        seek.progress = audio.getStreamVolume(stream) - minVol()
        refreshLabel()
    }

    private fun refreshLabel() {
        val name = if (stream == AudioManager.STREAM_VOICE_CALL) "Call volume" else "Music volume"
        valueText.text = "$name: ${seek.progress + minVol()} / ${audio.getStreamMaxVolume(stream)}"
    }
}
