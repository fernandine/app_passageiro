package mobi.audax.tupi.passageiro.bin.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class WriteSDCard(private val context: Context) {

    private var mExternalStorageAvailable = false
    private var mExternalStorageWriteable = false

    private fun checkExternalMedia() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            mExternalStorageWriteable = true
            mExternalStorageAvailable = mExternalStorageWriteable
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            mExternalStorageAvailable = true
            mExternalStorageWriteable = false
        } else {
            mExternalStorageWriteable = false
            mExternalStorageAvailable = mExternalStorageWriteable
        }
    }

    private fun getType(filename: String?): String? {
        if (filename != null) {
            if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".gif")) {
                return Environment.DIRECTORY_PICTURES
            }
        }
        return null
    }

    fun createFilePath(filename: String): File? {
        checkExternalMedia()
        if (!mExternalStorageWriteable && !mExternalStorageAvailable) {
            throw Exception("SD Card does not writable and available")
        } else if (!mExternalStorageWriteable) {
            throw Exception("SD Card does not writable")
        }
        val root = context.getExternalFilesDir(getType(filename))
        val dir = File(root!!.absolutePath)
        dir.mkdirs()
        return File(dir, filename)
    }

    fun writeToSDFile(filename: String, data: ByteArray) {
        checkExternalMedia()
        if (!mExternalStorageWriteable && !mExternalStorageAvailable) {
            throw Exception("SD Card does not writable and available")
        } else if (!mExternalStorageWriteable) {
            throw Exception("SD Card does not writable")
        }
        val root = context.getExternalFilesDir(getType(filename))
        val dir = File(root!!.absolutePath)
        dir.mkdirs()
        val file = File(dir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            if (!file.exists()) {
                file.createNewFile()
            }
            fos.run {
                write(data)
                flush()
                close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getFile(filename: String): File? {
        checkExternalMedia()
        if (!mExternalStorageAvailable) {
            throw Exception("SD Card does not available")
        }
        return File(context.getExternalFilesDir(getType(filename)), filename)
    }

    fun getSDCardPath(): File? {
        return context.getExternalFilesDir(getType(null))
    }

}