package com.example.stw_b1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.example.stw_b1.databinding.ActivityCameraBinding
import com.google.mlkit.vision.barcode.common.Barcode

class CameraActivity : AppCompatActivity(), PermissionDialog.OnCancelListener {

    private lateinit var viewBinding: ActivityCameraBinding
    private var previewView: PreviewView? = null
    private lateinit var codeScanner: CodeScanner

    private val launcher = registerForActivityResult(
        CameraPermission.RequestContract(), ::onPermissionResult
    )

    // QRコード検知
    private fun onDetectCode(codes: List<Barcode>) {
        codes.forEach {
            val value = it.rawValue
            Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionCancel() {
        finishByError()
    }
    private fun onPermissionResult(granted: Boolean) {
        when {
            granted ->
                startCamera()
            CameraPermission.deniedWithoutShowDialog(this) ->
                PermissionDialog.show(this)
            else -> {
                toastPermissionError()
                finishByError()
            }
        }
    }

    private fun startCamera(){

    }

    private fun toastPermissionError() {
        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_LONG).show()
    }

    private fun finishByError() {
        super.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        previewView = viewBinding.viewFinder

        codeScanner = CodeScanner(this, viewBinding.viewFinder, ::onDetectCode)
        if (CameraPermission.hasPermission(this)) {
            codeScanner.start()
        }
        else {
            launcher.launch(Unit)
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
}