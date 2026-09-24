package com.minimal.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2

// Host-Activity: haelt nur noch das ViewPager2-"Wischband" mit den drei Seiten
// Notizen (0) - Home (1) - Drawer (2). Das eigentliche Wischen (nahtlos,
// mit dem Finger mitgehend) uebernimmt ViewPager2 automatisch.
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applySystemBarsVisibility()
        applyBackgroundColor()

        if (!Prefs.hasSeenSetup(this)) {
            startActivity(Intent(this, SetupActivity::class.java))
        }

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = LauncherPagerAdapter(this)
        viewPager.offscreenPageLimit = 1
        viewPager.setCurrentItem(1, false) // Start immer auf dem Home-Screen

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING && viewPager.currentItem == 0) {
                    hideKeyboard()
                }
            }
            override fun onPageSelected(position: Int) {
                if (position != 0) hideKeyboard()
            }
        })
    }

    private fun hideKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Zurueck-Taste bringt zum Home-Screen statt den Launcher zu verlassen
        if (viewPager.currentItem != 1) {
            viewPager.setCurrentItem(1, true)
        }
    }

    fun goToPage(position: Int) {
        viewPager.setCurrentItem(position, true)
    }

    override fun onResume() {
        super.onResume()
        applySystemBarsVisibility()
        applyBackgroundColor()
        AppLauncher.hideOverlayOnly(this)
    }

    // Ohne das hier greift das Ausblenden der Systemleisten manchmal nicht
    // zuverlaessig - ein bekannter Android-Stolperstein: der Aufruf muss
    // auch (nochmal) passieren, sobald das Fenster wirklich den Fokus hat,
    // nicht nur in onCreate/onResume.
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySystemBarsVisibility()
    }

    private fun applyBackgroundColor() {
        val color = Palettes.current(this).background
        window.decorView.setBackgroundColor(color)
        findViewById<ViewPager2>(R.id.viewPager)?.setBackgroundColor(color)

        // Ohne das hier behalten Status-/Navigationsleiste ihre eigene
        // (schwarze) Systemfarbe, unabhaengig vom App-Hintergrund - erst
        // "transparent" laesst wirklich unsere Farbe durchscheinen.
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun applySystemBarsVisibility() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        val mode = Prefs.getCurrentMode(this)
        val modeOverride = if (mode != "Standard") Prefs.getModeStatusBarHidden(this, mode) else null
        val hidden = modeOverride ?: Prefs.getStatusBarHidden(this)

        if (hidden) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
