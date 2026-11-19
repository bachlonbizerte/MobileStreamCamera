package com.wevent.mobilestreamcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val navy = 0xFF101A35.toInt()
    private val gold = 0xFFC8A45D.toInt()
    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var switchButton: Button
    private var cameraProvider: ProcessCameraProvider? = null
    private var useFrontCamera = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val cameraGranted = grants[Manifest.permission.CAMERA] == true
            if (cameraGranted) startCamera()
            else status.text = "AUTORISATION CAMÉRA REFUSÉE"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        ensureCameraPermission()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(navy)
            setPadding(14, 12, 14, 12)
        }

        val header = TextView(this).apply {
            text = "MOBILE STREAM CAMERA  •  V1.1"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(gold)
            setPadding(4, 4, 4, 10)
        }
        root.addView(header, LinearLayout.LayoutParams(-1, -2))

        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        root.addView(previewView, LinearLayout.LayoutParams(-1, 0, 1f))

        status = TextView(this).apply {
            text = "INITIALISATION CAMÉRA…"
            textSize = 13f
            setTextColor(-1)
            gravity = Gravity.CENTER
            setPadding(4, 8, 4, 8)
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        switchButton = Button(this).apply {
            text = "BASCULER AVANT / ARRIÈRE"
            setTextColor(navy)
            setBackgroundColor(gold)
            setOnClickListener {
                useFrontCamera = !useFrontCamera
                startCamera()
            }
        }
        controls.addView(switchButton, LinearLayout.LayoutParams(0, 54, 1f))

        val info = Button(this).apply {
            text = "INFO CAMÉRA"
            setOnClickListener { showCameraInfo() }
        }
        controls.addView(info, LinearLayout.LayoutParams(0, 54, 1f))
        root.addView(controls, LinearLayout.LayoutParams(-1, -2))

        val note = TextView(this).apply {
            text = "V1.1 : aperçu caméra et basculement. Streaming RTMP/SRT en cours d'intégration."
            textSize = 11f
            setTextColor(0xFFB8C2D6.toInt())
            gravity = Gravity.CENTER
            setPadding(4, 8, 4, 2)
        }
        root.addView(note, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private fun ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) return

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                               else CameraSelector.DEFAULT_BACK_CAMERA

                if (!provider.hasCamera(selector)) {
                    useFrontCamera = !useFrontCamera
                    status.text = "Caméra demandée indisponible — autre caméra sélectionnée"
                    startCamera()
                    return@addListener
                }

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                provider.unbindAll()
                provider.bindToLifecycle(this, selector, preview)
                status.text = if (useFrontCamera) "● CAMÉRA AVANT ACTIVE"
                              else "● CAMÉRA ARRIÈRE ACTIVE"
            } catch (e: Exception) {
                status.text = "ERREUR CAMÉRA : ${e.localizedMessage ?: "inconnue"}"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun showCameraInfo() {
        val provider = cameraProvider
        val text = if (provider == null) "Caméra non initialisée"
        else {
            val back = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            val front = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            "Arrière : ${if (back) "disponible" else "indisponible"}\nAvant : ${if (front) "disponible" else "indisponible"}"
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        super.onDestroy()
    }
}
