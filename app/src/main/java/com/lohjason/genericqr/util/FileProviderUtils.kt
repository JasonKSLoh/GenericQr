package com.lohjason.genericqr.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.content.FileProvider
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * FileProviderUtils
 * Created by jason on 29/6/18.
 */
class FileProviderUtils {

    companion object {

        private const val PATH = "images"
        private const val FILENAME = "GenericQrBarcode.png"
        private const val FILEEXTENSION = ".png"
        private const val AUTHORITY = "com.lohjason.genericqr.fileprovider"

        fun saveBarcode(context: Context, data: String, barcodeFormat: Int): Boolean{
            val barcodeEncoder = BarcodeEncoder()
            val zxingFormat = ClassificationUtils.getBarcodeFormatZxingType(barcodeFormat)
            val bitmap = barcodeEncoder.encodeBitmap(data,
                                                     zxingFormat,
                                                     512,
                                                     512)
            return saveBitmap(context, bitmap)
        }

        private fun saveBitmap(context: Context, bitmap: Bitmap): Boolean{
            return try{
                clearCache(context)
                val cachePath = File(context.cacheDir, PATH)
                cachePath.mkdirs()
                val fileName = FILENAME + SimpleDateFormat("YYYYMMddHHmmSS", Locale.US).format(Date()) + FILEEXTENSION
                val imageFile = File(cachePath, fileName)
                val fileOutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                Logg.d("+_", "Saved Bitmap with size: ${imageFile.length()} bytes")
                fileOutputStream.flush()
                fileOutputStream.close()
                true
            } catch (e: Exception){
                false
            }
        }

        fun getShareBitmapIntent(context: Context): Intent?{
            val cachePath = File(context.cacheDir, PATH)
//            val imagePath = File(cachePath, FILENAME)
            val imageFiles = cachePath.listFiles()
            if(imageFiles.isEmpty()){
                return null
            }

//            val imageUri = FileProvider.getUriForFile(context, AUTHORITY, imagePath)
            val imageUri = FileProvider.getUriForFile(context, AUTHORITY, imageFiles[0])
            imageUri?.let {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(imageUri, context.contentResolver.getType(imageUri))
                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                return Intent.createChooser(intent, "Share the barcode image")
//                return intent
            }
            return null
        }

        private fun clearCache(context: Context){
            File(context.cacheDir, PATH).deleteRecursively()
        }

    }

}