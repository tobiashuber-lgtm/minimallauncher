package com.minimal.launcher

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

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = LauncherPagerAdapter(this)
        viewPager.offscreenPageLimit = 1
        viewPager.setCurrentItem(1, false) // Start immer auf dem Home-Screen
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
        AppLauncher.hideOverlayOnly(this)
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
