package com.festfive.app.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by Nhat Vo on 06/12/2019.
 */
class DecreaseImageSizeAsyncTask(
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    var context: Context,
    var listener: (String?) -> Unit
) : AsyncTask<Void, Long, String?>() {

    override fun doInBackground(vararg params: Void?): String? {
        return when {
            uri != null -> {
                decreaseImageSize(imagePath = FileUtils.getRealPathFromURI(context, uri) ?: "")
            }
            bitmap != null -> {
                decreaseImageSize(bitmap = bitmap)
            }
            else -> {
                null
            }
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        listener(result)
    }

    private fun decreaseImageSize(imagePath: String? = null, bitmap: Bitmap? = null): String? {
        val decreaseBitmap: Bitmap?
        try {
            val scaled = bitmap ?: decodeSampledBitmapFromResourceMemOpt(
                FileInputStream(
                    File(
                        imagePath ?: ""
                    )
                ) /*, o.outWidth / scale, o.outHeight / scale*/
            )
            decreaseBitmap = if (scaled != null) {
                val mat = Matrix()
                if (imagePath != null) {
                    mat.postRotate(getOrientationFromExif(imagePath).toFloat())
                }
                Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, mat, true)
            } else {
                return ""
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return ""
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return ""
        }
        return saveImageToFile(bitmap = decreaseBitmap)
    }

    private fun decodeSampledBitmapFromResourceMemOpt(inputStream: InputStream /*, int reqWidth, int reqHeight*/): Bitmap? {
        var byteArr = ByteArray(0)
        val buffer = ByteArray(1024)
        var len = -1
        var count = 0

        return try {
            while (inputStream.read(buffer).also { len = it } > -1) {
                if (len != 0) {
                    if (count + len > byteArr.size) {
                        val newbuf = ByteArray((count + len) * 2)
                        System.arraycopy(byteArr, 0, newbuf, 0, count)
                        byteArr = newbuf
                    }
                    System.arraycopy(buffer, 0, byteArr, count, len)
                    count += len
                }
            }
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArr, 0, count, options)
            options.inSampleSize = calculateInSampleSize(options /*, reqWidth, reqHeight*/)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeByteArray(byteArr, 0, count, options)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options /*, int reqWidth, int reqHeight*/): Int { // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        // The new size we want to scale to
        if (height >= width) {
            if (height > REQUIRED_SIZE) {
                inSampleSize = (height / REQUIRED_SIZE).toFloat().roundToInt()
            }
        } else {
            if (width > REQUIRED_SIZE) {
                inSampleSize = (width / REQUIRED_SIZE).toFloat().roundToInt()
            }
        }
        return inSampleSize
    }

    private fun getOrientationFromExif(imagePath: String): Int {
        var orientation = 0
        try {
            val exif = ExifInterface(imagePath)
            val exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> orientation = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> orientation = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90
                ExifInterface.ORIENTATION_NORMAL -> orientation = 0
                else -> {
                }
            }
        } catch (e: IOException) {
        }
        return orientation
    }

    private fun getTempFolder(): File {
        val directoryFolder =
            File(Environment.getExternalStorageDirectory(), "Keno-staff")
        directoryFolder.mkdirs()
        return directoryFolder
    }

    private fun getTempFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHMMss", Locale.getDefault()).format(Date())
        return File(
            getTempFolder().absolutePath,
            "Image".plus(Calendar.getInstance().timeInMillis).plus(timeStamp).plus(".jpg")
        )
    }

    private fun saveImageToFile(bitmap: Bitmap? = null): String? {
        return try {
            val file = getTempFile()
            file.createNewFile()
            val fOut = FileOutputStream(file)
            bitmap?.apply {
                this.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            }
            fOut.flush()
            fOut.close()
            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val REQUIRED_SIZE = 1024
    }
}