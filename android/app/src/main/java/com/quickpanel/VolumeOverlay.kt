package com.quickpanel

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/**
 * Volume popup drawn as an overlay window (not an Activity), so the app underneath
 * (YouTube, Instagram...) keeps focus and keeps playing. Appears near the top, clear of nav buttons.
 */
object VolumeOverlay {
    private val handler = Handler(Looper.getMainLooper())
    private var view: View? = null
    private var wm: WindowManager? = null
    private const val TIMEOUT_MS = 4000L

    fun show(app: Context) {
        dismiss()
        val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val windowManager = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val prefs = app.getSharedPreferences("quickpanel", Context.MODE_PRIVATE)
        val d = app.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        var stream = prefs.getInt("stream", AudioManager.STREAM_MUSIC)

        fun minVol() = audio.getStreamMinVolume(stream)

        val card = LinearLayout(app).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F2202124")); cornerRadius = dp(24).toFloat()
            }
        }
        val chips = LinearLayout(app).apply { orientation = LinearLayout.HORIZONTAL }
        val music = chip(app, "Music", ::dp)
        val call = chip(app, "Call", ::dp)
        chips.addView(music); chips.addView(call)
        card.addView(chips)

        val label = TextView(app).apply { setTextColor(Color.WHITE); textSize = 15f; setPadding(0, dp(12), 0, 0) }
        val seek = SeekBar(app)
        card.addView(label); card.addView(seek, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        fun refreshLabel() {
            val n = if (stream == AudioManager.STREAM_VOICE_CALL) "Call volume" else "Music volume"
            label.text = "$n: ${seek.progress + minVol()} / ${audio.getStreamMaxVolume(stream)}"
        }
        fun sync() {
            setSelected(music, stream == AudioManager.STREAM_MUSIC)
            setSelected(call, stream == AudioManager.STREAM_VOICE_CALL)
            seek.max = audio.getStreamMaxVolume(stream) - minVol()
            seek.progress = audio.getStreamVolume(stream) - minVol()
            refreshLabel()
        }
        fun restartTimer() { handler.removeCallbacks(::dismiss); handler.postDelayed(::dismiss, TIMEOUT_MS) }

        music.setOnClickListener { stream = AudioManager.STREAM_MUSIC; prefs.edit().putInt("stream", stream).apply(); sync(); restartTimer() }
        call.setOnClickListener { stream = AudioManager.STREAM_VOICE_CALL; prefs.edit().putInt("stream", stream).apply(); sync(); restartTimer() }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                if (fromUser) { audio.setStreamVolume(stream, p + minVol(), 0); restartTimer() }
                refreshLabel()
            }
            override fun onStartTrackingTouch(s: SeekBar) { handler.removeCallbacks(::dismiss) }
            override fun onStopTrackingTouch(s: SeekBar) { restartTimer() }
        })
        card.setOnTouchListener { _, e -> if (e.action == MotionEvent.ACTION_OUTSIDE) { dismiss(); true } else false }
        sync()

        val width = minOf((app.resources.displayMetrics.widthPixels * 0.92).toInt(), dp(380))
        val lp = WindowManager.LayoutParams(
            width, WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(96) }

        windowManager.addView(card, lp)
        view = card; wm = windowManager
        restartTimer()
    }

    fun dismiss() {
        handler.removeCallbacks(::dismiss)
        view?.let { runCatching { wm?.removeView(it) } }
        view = null
    }

    private fun chip(ctx: Context, text: String, dp: (Int) -> Int) = TextView(ctx).apply {
        this.text = text; textSize = 14f; gravity = Gravity.CENTER
        setPadding(dp(20), dp(8), dp(20), dp(8))
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { rightMargin = dp(8) }
    }

    private fun setSelected(v: TextView, on: Boolean) {
        v.setTextColor(if (on) Color.BLACK else Color.WHITE)
        v.background = GradientDrawable().apply {
            cornerRadius = 100f
            setColor(if (on) Color.parseColor("#8AB4F8") else Color.parseColor("#3C4043"))
        }
    }
}
