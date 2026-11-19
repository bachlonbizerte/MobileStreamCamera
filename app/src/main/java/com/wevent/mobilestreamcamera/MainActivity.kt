package com.wevent.mobilestreamcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val navy = 0xFF101A35.toInt()
    private val gold = 0xFFC8A45D.toInt()
    private lateinit var status: TextView
    private lateinit var resolution: Spinner
    private lateinit var fps: Spinner
    private lateinit var bitrate: SeekBar
    private lateinit var bitrateLabel: TextView
    private lateinit var protocol: Spinner
    private lateinit var url: EditText
    private lateinit var doubleStream: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsIfNeeded()
        buildUi()
    }

    private fun requestPermissionsIfNeeded() {
        val missing = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(navy)
            setPadding(20, 22, 20, 18)
        }
        val title = TextView(this).apply {
            text = "MOBILE STREAM CAMERA"
            textSize = 22f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWrap())
        val subtitle = TextView(this).apply {
            text = "WEVENT • BROADCAST CAMERA V1"
            textSize = 11f
            setTextColor(0xFFCBD2E2.toInt())
            gravity = Gravity.CENTER
        }
        root.addView(subtitle, matchWrap())

        val preview = FrameLayout(this).apply {
            setBackgroundColor(0xFF080D19.toInt())
        }
        val previewText = TextView(this).apply {
            text = "APERÇU CAMÉRA\n\nLa prévisualisation caméra sera activée dans le module Camera2."
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }
        preview.addView(previewText, FrameLayout.LayoutParams(-1, -1))
        val previewParams = LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = 18; bottomMargin = 12 }
        root.addView(preview, previewParams)

        status = TextView(this).apply {
            text = "● PRÊT — configuration non démarrée"
            setTextColor(gold)
            textSize = 13f
        }
        root.addView(status, matchWrap())

        resolution = spinner(listOf("1920 × 1080", "1280 × 720", "854 × 480", "3840 × 2160 (si supporté)"))
        addField(root, "RÉSOLUTION", resolution)
        fps = spinner(listOf("25 FPS", "30 FPS", "50 FPS", "60 FPS"))
        addField(root, "FRÉQUENCE", fps)

        bitrateLabel = TextView(this).apply { setTextColor(-1); text = "BITRATE VIDÉO : 6 000 kbps" }
        root.addView(bitrateLabel, matchWrap())
        bitrate = SeekBar(this).apply {
            max = 195
            progress = 55
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                    bitrateLabel.text = "BITRATE VIDÉO : ${(p * 100 + 500)} kbps"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        root.addView(bitrate, matchWrap())

        protocol = spinner(listOf("RTMP", "SRT"))
        addField(root, "PROTOCOLE PRINCIPAL", protocol)
        url = EditText(this).apply {
            setSingleLine(true)
            setTextColor(-1)
            setHintTextColor(0xFF9AA5B8.toInt())
            hint = "rtmp://serveur:1935/live/camera1"
            setText("rtmp://192.168.0.7:1935/live/camera1")
        }
        root.addView(url, matchWrap())
        doubleStream = CheckBox(this).apply {
            text = "DOUBLE STREAMING (serveur V5 + destination externe)"
            setTextColor(-1)
            buttonTintList = android.content.res.ColorStateList.valueOf(gold)
        }
        root.addView(doubleStream, matchWrap())

        val start = Button(this).apply {
            text = "CONFIGURER LE STREAM"
            setBackgroundColor(gold)
            setTextColor(navy)
            setOnClickListener {
                status.text = "Configuration prête — moteur de streaming à connecter"
                Toast.makeText(this@MainActivity,
                    "V1 foundation: caméra/encodeur/transport à finaliser et tester.", Toast.LENGTH_LONG).show()
            }
        }
        root.addView(start, LinearLayout.LayoutParams(-1, 54).apply { topMargin = 10 })
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
    }

    private fun addField(root: LinearLayout, label: String, view: android.view.View) {
        val text = TextView(this).apply { this.text = label; setTextColor(gold); textSize = 11f }
        root.addView(text, matchWrap())
        root.addView(view, matchWrap())
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
}
