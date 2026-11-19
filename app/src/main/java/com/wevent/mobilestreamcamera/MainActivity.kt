package com.wevent.mobilestreamcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.*
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
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(navy)
        }
        root.addView(TextView(this).apply {
            text = "MOBILE STREAM CAMERA • V1.2"
            textSize = 19f; gravity = Gravity.CENTER; setTextColor(gold)
        }, LinearLayout.LayoutParams(-1, -2))

        glView = OpenGlView(this)
        root.addView(glView, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = 8; bottomMargin = 8 })

        status = TextView(this).apply {
            text = "INITIALISATION…"; setTextColor(-1); textSize = 13f; gravity = Gravity.CENTER
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        protocol = spinner(listOf("RTMP", "SRT"))
        resolution = spinner(listOf("1920x1080", "1280x720", "854x480"))
        row1.addView(protocol, LinearLayout.LayoutParams(0, 48, 1f))
        row1.addView(resolution, LinearLayout.LayoutParams(0, 48, 1f))
        root.addView(row1)

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fps = spinner(listOf("25 FPS", "30 FPS", "50 FPS", "60 FPS"))
        row2.addView(fps, LinearLayout.LayoutParams(0, 48, 1f))
        bitrateText = TextView(this).apply {
            text = "6 Mbps"; setTextColor(gold); gravity = Gravity.CENTER
        }
        row2.addView(bitrateText, LinearLayout.LayoutParams(0, 48, 1f))
        root.addView(row2)
        bitrate = SeekBar(this).apply {
            max = 195; progress = 55
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    bitrateText.text = "${(p * 100 + 500) / 1000.0} Mbps"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        root.addView(bitrate, LinearLayout.LayoutParams(-1, -2))

        urlInput = EditText(this).apply {
            setText("rtmp://192.168.0.7:1935/live/camera1")
            setTextColor(-1); setHintTextColor(0xFFB8C2D6.toInt())
            setSingleLine(true); hint = "URL RTMP ou SRT"
        }
        root.addView(urlInput, LinearLayout.LayoutParams(-1, -2))

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(Button(this).apply {
            text = "CAMÉRA AVANT / ARRIÈRE"
            setOnClickListener {
                try {
                    streamCamera?.switchCamera()
                    status.text = "CHANGEMENT DE CAMÉRA DEMANDÉ"
                } catch (e: Exception) { status.text = "CHANGEMENT IMPOSSIBLE : ${e.message}" }
            }
        }, LinearLayout.LayoutParams(0, 52, 1f))
        streamButton = Button(this).apply {
            text = "START STREAM"
            setTextColor(navy); setBackgroundColor(gold)
            setOnClickListener { if (streaming) stopStreaming() else startStreaming() }
        }
        controls.addView(streamButton, LinearLayout.LayoutParams(0, 52, 1f))
        root.addView(controls)
        setContentView(root)
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
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
        streamCamera?.let {
            if (it.isStreaming) it.stopStream()
            it.stopPreview()
        }
        super.onDestroy()
    }
}
