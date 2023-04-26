package jp.co.xpower.app.stw

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.common.Barcode
import jp.co.xpower.app.stw.databinding.ActivityCameraBinding
import jp.co.xpower.app.stw.databinding.CameraOverlayBinding


class CameraActivity : AppCompatActivity(), PermissionDialog.OnCancelListener {

    private lateinit var cameraOverlayBinding: CameraOverlayBinding
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

            val intentSub = Intent()
            intentSub.putExtra(MainActivity.EXTRA_MESSAGE, value)
            setResult(Activity.RESULT_OK, intentSub)

            // 検知でスキャナーとカメラを終了
            codeScanner.close()
            super.finish()
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
        codeScanner.start()
    }

    private fun toastPermissionError() {
        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_LONG).show()
    }

    private fun finishByError() {
        super.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraOverlayBinding = CameraOverlayBinding.inflate(layoutInflater)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        previewView = viewBinding.viewFinder

        val imageView = ImageView(this)
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10, 10, 0,0)
        imageView.setImageResource(R.drawable.close)

        cameraOverlayBinding.imageClose.setOnClickListener {
            codeScanner.close()
            super.finish()
        }
        cameraOverlayBinding.imageFlash.setOnClickListener {
            codeScanner.toggleTorch()

            if(codeScanner.getState()){
                cameraOverlayBinding.imageFlash.setImageResource(R.drawable.flash_on)
            }
            else {
                cameraOverlayBinding.imageFlash.setImageResource(R.drawable.flash_off)
            }
        }

        addContentView(cameraOverlayBinding.root, params)

        codeScanner = CodeScanner(this, viewBinding.viewFinder, ::onDetectCode)
        if (CameraPermission.hasPermission(this)) {
            startCamera()
        }
        else {
            launcher.launch(Unit)
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
}
