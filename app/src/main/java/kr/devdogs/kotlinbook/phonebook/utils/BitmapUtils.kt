package kr.devdogs.kotlinbook.phonebook.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment

import java.io.File
import java.io.FileOutputStream


object BitmapUtils {
    fun rotate(bitmap: Bitmap?, degrees: Int): Bitmap? {
        if (degrees != 0 && bitmap != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2,
                    bitmap.height.toFloat() / 2)

            val converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.width, bitmap.height, m, true)
            bitmap.recycle()
            return converted
        }
        return bitmap
    }

    fun saveBitmap(bitmap: Bitmap): String {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val dir = File(Environment.getExternalStorageDirectory().absolutePath
                + "/phonebook/" + fileName)

        if (!dir.parentFile.exists()) {
            dir.parentFile.mkdirs()
        }

        FileOutputStream(dir).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return dir.path
    }
}
