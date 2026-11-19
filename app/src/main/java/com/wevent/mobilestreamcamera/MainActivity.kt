package com.wevent.mobilestreamcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.content.res.ColorStateList
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.library.base.Camera2Base
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.srt.SrtCamera2
import com.pedro.library.view.OpenGlView

class MainActivity : AppCompatActivity(), ConnectChecker {
    private val navy = 0xFF101A35.toInt()
    private val gold = 0xFFC8A45D.toInt()
    private lateinit var glView: OpenGlView
    private lateinit var status: TextView
    private lateinit var urlInput: EditText
    private lateinit var protocol: Spinner
    private lateinit var resolution: Spinner
    private lateinit var fps: Spinner
    private lateinit var bitrate: SeekBar
    private lateinit var bitrateText: TextView
    private lateinit var streamButton: Button
    private var streamCamera: Camera2Base? = null
    private var streaming = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (hasPermissions()) initializeCamera() else status.text = "AUTORISATIONS CAMÉRA / MICRO REFUSÉES"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        if (hasPermissions()) initializeCamera()
        else permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    private fun hasPermissions() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun buildUi() {
        // ScrollView keeps every setting reachable on small phone screens.
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(navy)
            clipToPadding = false
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(22))
            setBackgroundColor(navy)
        }
        scroll.addView(root, ScrollView.LayoutParams(-1, -2))

        root.addView(TextView(this).apply {
            text = "MOBILE STREAM CAMERA"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(gold)
        }, LinearLayout.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "WEVENT • BROADCAST CAMERA V1.2"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        glView = OpenGlView(this)
        root.addView(glView, LinearLayout.LayoutParams(-1, dp(230)).apply {
            topMargin = dp(4); bottomMargin = dp(10)
        })

        status = TextView(this).apply {
            text = "INITIALISATION…"
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setBackgroundColor(0xFF1B2948.toInt())
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })

        addSection(root, "PARAMÈTRES VIDÉO")
        addLabel(root, "PROTOCOLE")
        protocol = spinner(listOf("RTMP", "SRT"))
        root.addView(protocol, fieldParams())

        addLabel(root, "RÉSOLUTION")
        resolution = spinner(listOf("1920x1080", "1280x720", "854x480"))
        root.addView(resolution, fieldParams())

        addLabel(root, "FRÉQUENCE D'IMAGES")
        fps = spinner(listOf("25 FPS", "30 FPS", "50 FPS", "60 FPS"))
        root.addView(fps, fieldParams())

        addLabel(root, "BITRATE VIDÉO")
        bitrateText = TextView(this).apply {
            text = "6 Mbps"
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(4), dp(4), dp(4), 0)
        }
        root.addView(bitrateText, LinearLayout.LayoutParams(-1, -2))
        bitrate = SeekBar(this).apply {
            max = 195
            progress = 55
            progressTintList = ColorStateList.valueOf(gold)
            thumbTintList = ColorStateList.valueOf(gold)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    bitrateText.text = "${(p * 100 + 500) / 1000.0} Mbps"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        root.addView(bitrate, LinearLayout.LayoutParams(-1, dp(48)))

        addSection(root, "DESTINATION DU STREAM")
        urlInput = EditText(this).apply {
            setText("rtmp://192.168.0.7:1935/live/camera1")
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB8C2D6.toInt())
            setSingleLine(true)
            hint = "URL RTMP ou SRT"
            textSize = 14f
            setSelectAllOnFocus(true)
            backgroundTintList = ColorStateList.valueOf(gold)
        }
        root.addView(urlInput, LinearLayout.LayoutParams(-1, dp(54)))

        addSection(root, "COMMANDES")
        val cameraButton = Button(this).apply {
            text = "BASCULER CAMÉRA AVANT / ARRIÈRE"
            styleActionButton()
            setOnClickListener {
                try {
                    streamCamera?.switchCamera()
                    status.text = "CHANGEMENT DE CAMÉRA DEMANDÉ"
                } catch (e: Exception) { status.text = "CHANGEMENT IMPOSSIBLE : ${e.message}" }
            }
        }
        root.addView(cameraButton, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(4); bottomMargin = dp(8)
        })

        streamButton = Button(this).apply {
            text = "▶  DÉMARRER LE STREAM"
            styleActionButton()
            setOnClickListener { if (streaming) stopStreaming() else startStreaming() }
        }
        root.addView(streamButton, LinearLayout.LayoutParams(-1, dp(58)).apply {
            bottomMargin = dp(12)
        })

        root.addView(TextView(this).apply {
            text = "Conseil : vérifie que le téléphone et le serveur sont sur le même réseau Wi-Fi pour un test local."
            setTextColor(0xFFB8C2D6.toInt())
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }, LinearLayout.LayoutParams(-1, -2))

        setContentView(scroll)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun fieldParams() = LinearLayout.LayoutParams(-1, dp(52)).apply {
        bottomMargin = dp(5)
    }

    private fun addSection(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            setTextColor(gold)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(13), 0, dp(5))
        }, LinearLayout.LayoutParams(-1, -2))
    }

    private fun addLabel(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(3), dp(5), 0, 0)
        }, LinearLayout.LayoutParams(-1, -2))
    }

    private fun Button.styleActionButton() {
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        backgroundTintList = ColorStateList.valueOf(gold)
        minHeight = dp(52)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        stateListAnimator = null
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        setBackgroundColor(0xFF1B2948.toInt())
        val adapter = object : ArrayAdapter<String>(
            this@MainActivity, android.R.layout.simple_spinner_item, items
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(Color.WHITE)
                    textSize = 15f
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                }
            }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(0xFF1B2948.toInt())
                    textSize = 15f
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                }
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
    }

    private fun initializeCamera() {
        try {
            val rtmp = RtmpCamera2(glView, this)
            streamCamera = rtmp
            rtmp.startPreview()
            status.text = "● CAMÉRA PRÊTE — ARRIÈRE"
        } catch (e: Exception) {
            status.text = "ERREUR INITIALISATION : ${e.localizedMessage}"
        }
    }

    private fun startStreaming() {
        if (!hasPermissions()) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            return
        }
        val endpoint = urlInput.text.toString().trim()
        if (endpoint.isBlank()) { status.text = "SAISIS UNE URL DE STREAM"; return }

        try {
            val selectedProtocol = protocol.selectedItem.toString()
            val camera: Camera2Base = when (selectedProtocol) {
                "SRT" -> SrtCamera2(glView, this)
                else -> RtmpCamera2(glView, this)
            }
            streamCamera?.stopPreview()
            streamCamera = camera

            val size = resolution.selectedItem.toString().split("x")
            val width = size[0].toInt()
            val height = size[1].toInt()
            val frameRate = fps.selectedItem.toString().substringBefore(" ").toInt()
            val videoBitrate = (bitrate.progress * 100 + 500) * 1000

            val videoReady = camera.prepareVideo(width, height, frameRate, videoBitrate, 2, 0)
            val audioReady = camera.prepareAudio(128 * 1000, 44100, true)
            if (!videoReady || !audioReady) {
                status.text = "ENCODEUR NON DISPONIBLE POUR CES PARAMÈTRES"
                camera.startPreview()
                return
            }
            camera.startPreview()
            camera.startStream(endpoint)
            streaming = true
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            streamButton.text = "STOP STREAM"
            status.text = "CONNEXION ${selectedProtocol} EN COURS…"
        } catch (e: Exception) {
            status.text = "ERREUR STREAM : ${e.localizedMessage}"
        }
    }

    private fun stopStreaming() {
        try {
            streamCamera?.stopStream()
            streamCamera?.stopPreview()
            streaming = false
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            streamButton.text = "START STREAM"
            status.text = "STREAM ARRÊTÉ"
            streamCamera = null
            initializeCamera()
        } catch (e: Exception) {
            status.text = "ERREUR ARRÊT : ${e.localizedMessage}"
        }
    }

    override fun onConnectionStarted(url: String) { runOnUiThread { status.text = "CONNEXION : $url" } }
    override fun onConnectionSuccess() { runOnUiThread { status.text = "● STREAM CONNECTÉ" } }
    override fun onConnectionFailed(reason: String) {
        runOnUiThread { status.text = "ÉCHEC STREAM : $reason"; streaming = false; streamButton.text = "START STREAM" }
    }
    override fun onDisconnect() {
        runOnUiThread { status.text = "STREAM DÉCONNECTÉ"; streaming = false; streamButton.text = "START STREAM" }
    }
    override fun onAuthError() { runOnUiThread { status.text = "ERREUR AUTHENTIFICATION" } }
    override fun onAuthSuccess() { runOnUiThread { status.text = "AUTHENTIFICATION OK" } }
    override fun onNewBitrate(bitrate: Long) { runOnUiThread { status.text = "● STREAM • ${(bitrate / 1000)} kbps" } }

    override fun onResume() {
        super.onResume()
        if (hasPermissions() && streamCamera == null) initializeCamera()
    }

    override fun onDestroy() {
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        streamCamera?.let {
            if (it.isStreaming) it.stopStream()
            it.stopPreview()
        }
        super.onDestroy()
    }
}
