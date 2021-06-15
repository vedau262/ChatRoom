package com.festfive.app.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.IOException

/**
 * Created by Nhat Vo on 07/12/2020.
 */
object FileUtils {
    private const val DEFAULT_IMAGE_FOLDER = "fest_five_image"
    const val DEFAULT_DOWNLOAD_FOLDER = "fest_five_download"
    private const val TMP_FILE_NAME = "ImageFile"
    private const val fileFormatter = ".jpg"

    /**
     * Check file is exist or not
     */
    fun isExistFile(context: Context, fileName: String): File {
        val path = FileUtils.getExternalFolder(context, DEFAULT_DOWNLOAD_FOLDER)
        val file = File(path, fileName)
        return file
    }

    /**
     * Get real path of file from uri
     */
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var result: String? = null
        try {
            val project = arrayOf(MediaStore.Images.Media.DATA)
            val cr = context.contentResolver
            val cursor = cr.query(contentUri, project, null, null, null)
            if (cursor != null && !cursor.isClosed) {
                val columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                result = cursor.getString(columnIndex)
                cursor.close()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result
    }

    fun getTempFilePath(context: Context): File? {
        var file: File? = null
        file = File(getExternalFolder(context), TMP_FILE_NAME + fileFormatter)
        if (file.exists()) {
            file.delete()
        }
        file = File(getExternalFolder(context), TMP_FILE_NAME + fileFormatter)
        return file
    }

    /**
     * Get External Folder
     */
    fun getExternalFolder(context: Context, folder: String = DEFAULT_IMAGE_FOLDER): File? {
        try {
            val state = Environment.getExternalStorageState()
            return when {
                Environment.MEDIA_MOUNTED == state -> {
                    val file = File(
                        Environment.getExternalStorageDirectory(),
                        folder
                    )
                    file.mkdir()
                    file
                }
                Environment.MEDIA_MOUNTED_READ_ONLY == state -> {
                    Toast.makeText(
                        context, "Can not write on external storage.",
                        Toast.LENGTH_LONG
                    ).show()
                    null
                }
                else -> {
                    null
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Delete draft image file
     */

    fun deleteImageFile() {
        val directoryFolder = File(
            Environment.getExternalStorageDirectory(),
            DEFAULT_IMAGE_FOLDER
        )
        if (directoryFolder.exists()) {
            val deleteCmd = "rm -r $directoryFolder"
            val runtime = Runtime.getRuntime()
            try {
                runtime.exec(deleteCmd)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}