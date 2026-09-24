package com.minimal.launcher

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TimeLimitDurationsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    private fun s(key: String) = Strings.get(this, key)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        container = findViewById(R.id.settingsContainer)
        build()
    }

    private fun build() {
        val title = TextView(this).apply {
            text = s("row_timelimit_durations")
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = FontFamilies.buildTypeface(this@TimeLimitDurationsActivity, "space_mono:bold")
            setPadding(0, dp(8), 0, dp(24))
        }
        container.addView(title)

        val durations = Prefs.getTimeLimitOptions(this)
        durations.forEachIndexed { index, minutes ->
            addSliderRow("${s("row_timelimit_option")} ${index + 1}", 1, 60, minutes) {
                Prefs.setTimeLimitOption(this, index, it)
            }
        }
    }

    private fun addSliderRow(label: String, min: Int, max: Int, initial: Int, onChange: (Int) -> Unit) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, dp(14))
        }
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 15f
            typeface = android.graphics.Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvValue = TextView(this).apply {
            text = "$initial min"
            setTextColor(0xFF888888.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        headerRow.addView(tvLabel)
        headerRow.addView(tvValue)

        val seekBar = SeekBar(this).apply {
            this.max = max - min
            progress = (initial - min).coerceIn(0, max - min)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + min
                    tvValue.text = "$value min"
                    if (fromUser) onChange(value)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        wrapper.addView(headerRow)
        wrapper.addView(seekBar)
        container.addView(wrapper)
        addDivider()
    }

    private fun addDivider() {
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1) / 2)
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        container.addView(divider)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
