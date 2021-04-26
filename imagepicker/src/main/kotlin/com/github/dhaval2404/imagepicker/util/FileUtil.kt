package com.github.dhaval2404.imagepicker.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile

import id.dreamfighter.android.utils.CommonUtils
import id.dreamfighter.android.utils.FileUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * File Utility Methods
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2019
 */
object FileUtil {

    /**
     * Get Image File
     *
     * Default it will take Camera folder as it's directory
     *
     * @param dir File Folder in which file needs tobe created.
     * @param extension String Image file extension.
     * @return Return Empty file to store camera image.
     * @throws IOException if permission denied of failed to create new file.
     */
    fun getImageFile(context: Context, dir: File? = null, extension: String? = null): File? {
        try {
            // Create an image file name
            val ext = extension ?: ".jpg"
            val imageFileName = "IMG_${getTimestamp()}$ext"

            // Create File Directory Object
            val storageDir = dir ?: getCameraDirectory()

            // Create Directory If not exist
            if (!storageDir.exists()) storageDir.mkdirs()

            // Create File Object
            val file = File(storageDir, imageFileName)

            /*
            val image = File.createTempFile(
                imageFileName, /* prefix */
                ext, /* suffix */
                storageDir      /* directory */
            )
             */
            Log.d("getImageFile", file.absolutePath)
            createFile(context, file.absolutePath)?.close()
            // Create empty file
            //file.createNewFile()

            return file
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    fun createFile(context: Context, fileName: String?): OutputStream? {
        val f = File(fileName)
        //val dir = CommonUtils.getRealDirectory(context)
        val dir = getCameraDirectory()
        val inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val uriStr =  getCameraDirectory().absolutePath
                    val permissions = context.contentResolver.persistedUriPermissions
                    var uri: Uri? = null
                    for (p in permissions) {
                        //Log.d("Uri",p.getUri().getPath());
                        //Log.d("Uri expected",Uri.fromFile(f.getParentFile()).getPath());
                        if (uriStr != null && uriStr == p.uri.toString()) {
                            uri = p.uri
                            break
                        }
                    }
                    if (uri != null) {
                        val pickedDir: DocumentFile? = DocumentFile.fromTreeUri(context, uri)
                        var file: DocumentFile? = pickedDir?.findFile(f.name)
                        if (file == null) {
                            file = pickedDir?.createFile("*/*", f.name)
                        }
                        outputStream = context.contentResolver.openOutputStream(file?.getUri(), "w")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (outputStream == null) {
                val realFile = File(dir, f.name)
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider", realFile
                )
                outputStream = FileOutputStream(realFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        //sink.flush();
        //sink.close();
        //return Observable.just(f);
        return outputStream
    }

    /**
     * Get Video File
     *
     * Default it will take Camera folder as it's directory
     *
     * @param dir File Folder in which file needs tobe created.
     * @param extension String Image file extension.
     * @return Return Empty file to store camera image.
     * @throws IOException if permission denied of failed to create new file.
     */
    fun getVideoFile(dir: File? = null, extension: String? = null): File? {
        try {
            // Create an image file name
            val ext = extension ?: ".mp4"
            val imageFileName = "VIDEO_${getTimestamp()}$ext"

            // Create File Directory Object
            val storageDir = dir ?: getCameraDirectory()

            // Create Directory If not exist
            if (!storageDir.exists()) storageDir.mkdirs()

            // Create File Object
            val file = File(storageDir, imageFileName)

            // Create empty file
            file.createNewFile()

            return file
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    /**
     * Get Camera Image Directory
     *
     * @return File Camera Image Directory
     */
    private fun getCameraDirectory(): File {

        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(dir, "Camera")
    }

    /**
     * Get Current Time in yyyyMMdd HHmmssSSS format
     *
     * 2019/01/30 10:30:20 000
     * E.g. 20190130_103020000
     */
    private fun getTimestamp(): String {
        val timeFormat = "yyyyMMdd_HHmmssSSS"
        return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }

    /**
     * Get Free Space size
     * @param file directory object to check free space.
     */
    fun getFreeSpace(file: File): Long {
        val stat = StatFs(file.path)
        val availBlocks = stat.availableBlocksLong
        val blockSize = stat.blockSizeLong
        return availBlocks * blockSize
    }

    /**
     * Get Bitmap Compress Format
     *
     * @param extension Image File Extension
     * @return Bitmap CompressFormat
     */
    fun getCompressFormat(extension: String): Bitmap.CompressFormat {
        return when {
            extension.contains("png", ignoreCase = true) -> Bitmap.CompressFormat.PNG
            extension.contains("webp", ignoreCase = true) -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }
}
