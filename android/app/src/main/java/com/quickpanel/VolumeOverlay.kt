package com.quickpanel

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/**
 * Volume popup drawn as an overlay window (not an Activity), so the app underneath keeps
 * playing. Stays for 10s after the last interaction, or until the X is tapped.
 * Touches outside the card pass through to the app and do NOT dismiss it.
 */
object VolumeOverlay {
    private const val TIMEOUT_MS = 10_000L
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { dismiss() }   // single instance so removeCallbacks works
    private var view: View? = null
    private var wm: WindowManager? = null
    private var density = 1f

    private fun dp(v: Int) = (v * density).toInt()

    fun show(app: Context) {
        dismiss()
        density = app.resources.displayMetrics.density
        val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val windowManager = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val prefs = app.getSharedPreferences("quickpanel", Context.MODE_PRIVATE)
        var stream = prefs.getInt("stream", AudioManager.STREAM_MUSIC)
        fun minVol() = audio.getStreamMinVolume(stream)
        fun maxVol() = audio.getStreamMaxVolume(stream)

        val card = LinearLayout(app).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(24))
            background = shape("#F2202124", dp(28).toFloat())
        }

        // Header: Music / Call chips + close
        val header = LinearLayout(app).apply { gravity = Gravity.CENTER_VERTICAL }
        val music = pill(app, "Music")
        val call = pill(app, "Call")
        val spacer = View(app)
        val close = circleButton(app, "✕", 18f)
        header.addView(music); header.addView(call)
        header.addView(spacer, LinearLayout.LayoutParams(0, 1, 1f))
        header.addView(close)
        card.addView(header)

        val label = TextView(app).apply {
            setTextColor(Color.WHITE); textSize = 18f; typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(20), 0, dp(4))
        }
        card.addView(label)

        // Slider row: [-]  ======O======  [+]
        val row = LinearLayout(app).apply { gravity = Gravity.CENTER_VERTICAL }
        val minus = circleButton(app, "−", 26f)
        val plus = circleButton(app, "+", 26f)
        val seek = SeekBar(app).apply {
            progressDrawable = LayerDrawable(arrayOf(
                shape("#5F6368", dp(10).toFloat()).apply { setSize(-1, dp(20)) },
                ClipDrawable(shape("#8AB4F8", dp(10).toFloat()).apply { setSize(-1, dp(20)) },
                    Gravity.START, ClipDrawable.HORIZONTAL)
            )).apply {
                setId(0, android.R.id.background); setId(1, android.R.id.progress)
            }
            thumb = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setColor(Color.WHITE); setSize(dp(40), dp(40))
            }
            splitTrack = false
            minimumHeight = dp(56)
            setPadding(dp(22), 0, dp(22), 0)
        }
        row.addView(minus)
        row.addView(seek, LinearLayout.LayoutParams(0, dp(56), 1f))
        row.addView(plus)
        card.addView(row, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        fun restartTimer() { handler.removeCallbacks(hideRunnable); handler.postDelayed(hideRunnable, TIMEOUT_MS) }
        fun refreshLabel() {
            val n = if (stream == AudioManager.STREAM_VOICE_CALL) "Call volume" else "Music volume"
            label.text = "$n   ${seek.progress + minVol()} / ${maxVol()}"
        }
        fun sync() {
            select(music, stream == AudioManager.STREAM_MUSIC)
            select(call, stream == AudioManager.STREAM_VOICE_CALL)
            seek.max = maxVol() - minVol()
            seek.progress = audio.getStreamVolume(stream) - minVol()
            refreshLabel()
        }
        fun step(delta: Int) {
            val v = (audio.getStreamVolume(stream) + delta).coerceIn(minVol(), maxVol())
            audio.setStreamVolume(stream, v, 0); sync(); restartTimer()
        }
        fun pick(s: Int) { stream = s; prefs.edit().putInt("stream", s).apply(); sync(); restartTimer() }

        music.setOnClickListener { pick(AudioManager.STREAM_MUSIC) }
        call.setOnClickListener { pick(AudioManager.STREAM_VOICE_CALL) }
        minus.setOnClickListener { step(-1) }
        plus.setOnClickListener { step(+1) }
        close.setOnClickListener { dismiss() }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                if (fromUser) audio.setStreamVolume(stream, p + minVol(), 0)
                refreshLabel()
            }
            override fun onStartTrackingTouch(s: SeekBar) { handler.removeCallbacks(hideRunnable) }
            override fun onStopTrackingTouch(s: SeekBar) { restartTimer() }
        })
        sync()

        val width = minOf((app.resources.displayMetrics.widthPixels * 0.94).toInt(), dp(420))
        val lp = WindowManager.LayoutParams(
            width, WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(96) }

        windowManager.addView(card, lp)
        view = card; wm = windowManager
        restartTimer()
    }

    fun dismiss() {
        handler.removeCallbacks(hideRunnable)
        view?.let { v -> runCatching { wm?.removeView(v) } }
        view = null
    }

    private fun shape(color: String, radius: Float) = GradientDrawable().apply {
        setColor(Color.parseColor(color)); cornerRadius = radius
    }

    private fun pill(ctx: Context, text: String) = TextView(ctx).apply {
        this.text = text; textSize = 16f; gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(24), dp(12), dp(24), dp(12))
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { rightMargin = dp(10) }
    }

    private fun circleButton(ctx: Context, text: String, size: Float) = TextView(ctx).apply {
        this.text = text; textSize = size; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
        background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#3C4043")) }
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
    }

    private fun select(v: TextView, on: Boolean) {
        v.setTextColor(if (on) Color.BLACK else Color.WHITE)
        v.background = shape(if (on) "#8AB4F8" else "#3C4043", 100f)
    }
}
