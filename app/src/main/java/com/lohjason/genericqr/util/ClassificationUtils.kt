package com.lohjason.genericqr.util

import com.google.android.gms.vision.barcode.Barcode
import com.google.zxing.BarcodeFormat

/**
 * ClassificationUtils
 * Created by jason on 25/6/18.
 */
class ClassificationUtils {


    companion object {
        private const val AZTEC = "AZTEC"
        private const val TEXT = "Text"
        private const val CODABAR = "Codabar"
        private const val CODE_128 = "Code 128"
        private const val CODE_39 = "Code 39"
        private const val CODE_93 = "Code 93"
        private const val DATA_MATRIX = "Data Matrix"
        private const val EAN_13 = "EAN-13"
        private const val EAN_8 = "EAN-8"
        private const val ITF = "ITF"
        private const val PDF_417 = "PDF-417"
        private const val QR_CODE = "QR Code"
        private const val UPC_A = "UPC-A"
        private const val UPC_E = "UPC-E"

        private const val CALENDAR = "Calendar Event"
        private const val CONTACT_INFO = "Contact"
        private const val DRIVER_LICENSE = "Driver's License"
        private const val EMAIL = "Email"
        private const val GEO = "Geo Data"
        private const val ISBN = "ISBN"
        private const val PHONE = "Phone Number"
        private const val PRODUCT = "Product Code"
        private const val SMS = "SMS"
        private const val URL = "URL"
        private const val WIFI = "WiFi"


        fun getBarcodeFormatString(formatId: Int): String {
            return when (formatId) {
                Barcode.AZTEC -> AZTEC
                Barcode.CODABAR -> CODABAR
                Barcode.CODE_128 -> CODE_128
                Barcode.CODE_39 -> CODE_39
                Barcode.CODE_93 -> CODE_93
                Barcode.DATA_MATRIX -> DATA_MATRIX
                Barcode.EAN_13 -> EAN_13
                Barcode.EAN_8 -> EAN_8
                Barcode.ITF -> ITF
                Barcode.PDF417 -> PDF_417
                Barcode.QR_CODE -> QR_CODE
                Barcode.UPC_A -> UPC_A
                Barcode.UPC_E -> UPC_E
                else -> TEXT
            }
        }

        fun getBarcodeDataFormatString(formatId: Int): String {
            return when (formatId) {
                Barcode.SMS -> SMS
                Barcode.CALENDAR_EVENT -> CALENDAR
                Barcode.CONTACT_INFO -> CONTACT_INFO
                Barcode.DRIVER_LICENSE -> DRIVER_LICENSE
                Barcode.EMAIL -> EMAIL
                Barcode.GEO -> GEO
                Barcode.ISBN -> ISBN
                Barcode.PHONE -> PHONE
                Barcode.PRODUCT -> PRODUCT
                Barcode.URL -> URL
                Barcode.WIFI -> WIFI
                else -> TEXT
            }
        }

        fun checkDataType(barcodeData: String): Int{
            return when{
                android.util.Patterns.WEB_URL.matcher(barcodeData).matches() -> {
                    Barcode.EMAIL
                }
                android.util.Patterns.PHONE.matcher(barcodeData).matches() -> {
                    Barcode.PHONE
                }
                else -> {
                    Logg.d("+_", "Checkdatatype: was text?")
                    Barcode.TEXT
                }
            }
        }

        fun getBarcodeFormatZxingType(formatId: Int): BarcodeFormat{
            return when (formatId) {
                Barcode.AZTEC -> BarcodeFormat.AZTEC
                Barcode.QR_CODE -> BarcodeFormat.QR_CODE
                Barcode.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
                Barcode.PDF417 -> BarcodeFormat.PDF_417
                Barcode.CODABAR -> BarcodeFormat.CODABAR
                Barcode.CODE_128 -> BarcodeFormat.CODE_128
                Barcode.CODE_39 -> BarcodeFormat.CODE_39
                Barcode.CODE_93 -> BarcodeFormat.CODE_93
                Barcode.EAN_13 -> BarcodeFormat.EAN_13
                Barcode.EAN_8 -> BarcodeFormat.EAN_8
                Barcode.ITF -> BarcodeFormat.ITF
                Barcode.UPC_A -> BarcodeFormat.UPC_A
                Barcode.UPC_E -> BarcodeFormat.UPC_E
                else -> BarcodeFormat.QR_CODE
            }
        }

    }
}