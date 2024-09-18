package com.sarvesh.barcodescanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.sarvesh.barcodescanner.databinding.ActivityBarcodeScanBinding

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvCopy.setOnClickListener {
            val textToCopy = binding.tvBarcodeResult.text.toString()
            copyTextToClipboard(textToCopy)
        }

    }

    private fun initCameraAndBarcodeDetector() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource =
            CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true).setFacing(CameraSource.CAMERA_FACING_BACK).build()
        binding.svCamera.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@BarcodeScan, Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this@BarcodeScan,
                            resources.getString(R.string.camera_permission_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    cameraSource.start(binding.svCamera.holder)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
                // Do nothing
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(this@BarcodeScan, "Barcode Scanner has stopped", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>) {
                val barcodes = p0.detectedItems
                if (barcodes.size() > 0) {
                    val barcode = barcodes.valueAt(0)
                    binding.tvBarcodeResult.text = barcode.displayValue
                }
            }

        })
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(resources.getString(R.string.copied_text), text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, resources.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        initCameraAndBarcodeDetector()
    }
}