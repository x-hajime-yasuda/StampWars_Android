package jp.co.xpower.cotamp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.co.xpower.cotamp.R

class PermissionDialog : DialogFragment() {
    interface OnCancelListener {
        fun onPermissionCancel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_permission)
            .setMessage(R.string.dialog_message_camera_permission)
            .setPositiveButton(R.string.ok) { _, _ ->
                startAppInfo(context)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        (context as? OnCancelListener)?.onPermissionCancel()
    }

    private fun startAppInfo(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "PermissionDialog"

        fun show(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            PermissionDialog().show(manager, TAG)
        }
    }
}
