package com.minimal.launcher

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

// Zeigt oben am Bildschirmrand einen schwebenden Countdown, waehrend ein
// Zeitlimit laeuft. Braucht die "Ueber anderen Apps anzeigen"-Berechtigung -
// falls die fehlt, wird einfach kein Overlay gezeigt (Kernfunktion des
// Zeitlimits - der Alarm, der zum Home-Screen zurueckholt - laeuft trotzdem).
class CountdownOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra("minutes", 0) ?: 0
        if (minutes <= 0) {
            stopSelf()
            return START_NOT_STICKY
        }
        showOverlay()
        startCountdown(minutes * 60_000L)
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = TextView(this).apply {
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xD9000000.toInt())
            textSize = 13f
            typeface = FontFamilies.buildTypeface(this@CountdownOverlayService, "space_mono:bold")
            setPadding(28, 10, 28, 10)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 48

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            overlayView = null
        }
    }

    private fun startCountdown(totalMillis: Long) {
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = (millisUntilFinished / 1000).toInt()
                val m = totalSeconds / 60
                val s = totalSeconds % 60
                overlayView?.text = String.format("%02d:%02d", m, s)
            }

            override fun onFinish() {
                removeOverlay()
                stopSelf()
            }
        }.start()
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) { /* bereits entfernt */ }
        }
        overlayView = null
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        removeOverlay()
        super.onDestroy()
    }
}
