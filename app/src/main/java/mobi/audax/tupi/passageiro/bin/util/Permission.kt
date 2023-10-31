package mobi.audax.tupi.passageiro.bin.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class Permission(val context: Context) {

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWriteExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun needRequestPermission(): Boolean {
        permissions().forEach {
            Log.v("Permission.kt", "Request: $it")
        }
        return permissions().isNotEmpty()
    }

    fun permissions(): Array<String> {
        val list: MutableList<String> = ArrayList()
        if (!hasLocationPermission()) {
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasCameraPermission()) {
            list.add(Manifest.permission.CAMERA)
        }
        if (!hasWriteExternalStoragePermission()) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return list.toTypedArray()
    }

}