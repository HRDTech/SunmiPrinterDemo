package com.teva360.sunmiprinterdemo.sunmi_printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.enums.DividingLine
import com.sunmi.printerx.enums.ErrorLevel
import com.sunmi.printerx.enums.HumanReadable
import com.sunmi.printerx.enums.ImageAlgorithm
import com.sunmi.printerx.style.BarcodeStyle
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.BitmapStyle
import com.sunmi.printerx.style.QrStyle
import com.sunmi.printerx.style.TextStyle
import com.teva360.sunmiprinterdemo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrinterService(private val context: Context) {
    private var sunmiPrinterService: PrinterSdk.Printer? = null
    var showPrinters = MutableLiveData<MutableList<PrinterSdk.Printer>?>()
    private var count = MutableLiveData("1")

    // Conectar al servicio de impresión
    fun connectPrinterService(callback: (Boolean) -> Unit) {
        try {
            PrinterSdk.getInstance().getPrinter(context, object : PrinterSdk.PrinterListen {

                override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                    if(sunmiPrinterService == null) {
                        sunmiPrinterService = printer
                        callback(true)
                    }
                }

                override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                    showPrinters.postValue(printers)
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

    // Desconectar del servicio de impresión
    fun disconnectPrinterService() {
        PrinterSdk.getInstance().destroy()
    }

    // Imprimir texto simple
    fun printText(text: String) {
        sunmiPrinterService?.lineApi()?.run {
            initLine(BaseStyle.getStyle())
            printText(text, TextStyle.getStyle())
            autoOut()
        }
    }

    // Imprimir una imagen
    fun printImage(bitmap: Bitmap) {
        sunmiPrinterService?.lineApi()?.run {
            printBitmap(bitmap, BitmapStyle.getStyle().setAlign(Align.CENTER).setAlgorithm(ImageAlgorithm.BINARIZATION).setValue(120).setWidth(196).setHeight(150))
            printBitmap(bitmap, BitmapStyle.getStyle().setAlign(Align.CENTER).setAlgorithm(
                ImageAlgorithm.DITHERING).setWidth(196).setHeight(150))
            autoOut()
        }
    }

    // Imprimir un código de barras
    fun printBarcode(data: String) {
        sunmiPrinterService?.lineApi()?.run {
            val barcodeStyle = BarcodeStyle.getStyle().setAlign(Align.CENTER).setDotWidth(2).setBarHeight(100).setReadable(
                HumanReadable.POS_TWO)
            printBarCode(data, barcodeStyle)
            printBarCode(data, barcodeStyle)
            barcodeStyle.setWidth(384)
            printBarCode(data, barcodeStyle)
            autoOut()
        }
    }

    // Imprimir un Qr
    fun printQr(data: String) {
        sunmiPrinterService?.lineApi()?.run {
            printQrCode(data, QrStyle.getStyle().setAlign(Align.CENTER).setDot(9).setErrorLevel(ErrorLevel.L))
            autoOut()
        }
    }

    // Imprimir un recibo completo
    @RequiresApi(Build.VERSION_CODES.O)
    fun printReceipt(view: View) {
        sunmiPrinterService?.lineApi()?.run {
            initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            val option: BitmapFactory.Options = BitmapFactory.Options().apply {
                inScaled = false
            }
            val bitmap = BitmapFactory.decodeResource(view.context.resources, R.drawable.logo, option)
            printBitmap(bitmap, BitmapStyle.getStyle().setAlign(Align.CENTER).setAlgorithm(ImageAlgorithm.DITHERING).setValue(120).setWidth(196).setHeight(150))
            printDividingLine(DividingLine.EMPTY, 30)
            printDividingLine(DividingLine.DOTTED, 2)
            printDividingLine(DividingLine.EMPTY, 30)
            addText("My Favorite Store\n", TextStyle.getStyle().enableBold(true))
            addText("Address: Calle Falsa 123\n", TextStyle.getStyle().enableBold(true))
            addText("Phone: 123-456-7890\n", TextStyle.getStyle().enableBold(true))
            printDividingLine(DividingLine.EMPTY, 30)
            printDividingLine(DividingLine.DOTTED, 2)
            val textStyle = TextStyle.getStyle().setAlign(Align.LEFT)
            printTexts(arrayOf("Product","Cant","Price"), intArrayOf(1, 1, 1), arrayOf(textStyle, textStyle, textStyle))
            printTexts(arrayOf("Apples","2","$1.00"), intArrayOf(1, 1, 1), arrayOf(textStyle, textStyle, textStyle))
            printTexts(arrayOf("Bananas","3","$0.50"), intArrayOf(1, 1, 1), arrayOf(textStyle, textStyle, textStyle))
            printText("-------------------------------\n", TextStyle.getStyle())
            initLine(BaseStyle.getStyle().setAlign(Align.RIGHT))
            printText("Subtotal:           $3.50\n", TextStyle.getStyle().setTextSize(18))
            printText("IVA (16%):          $0.56\n", TextStyle.getStyle().setTextSize(18))
            printText("TOTAL:              $2,94\n", TextStyle.getStyle().setTextSize(22))
            initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
            printDividingLine(DividingLine.EMPTY, 30)
            printText("Thank you for shopping!", TextStyle.getStyle().enableBold(true).setTextSize(24))
            printText(getCurrentDateTime(), TextStyle.getStyle())
            printDividingLine(DividingLine.EMPTY, 30)
            printQrCode("Order:00001;TOTAL: $2.94; Date:${getCurrentDate()}", QrStyle.getStyle().setDot(9).setAlign(Align.CENTER))
            printDividingLine(DividingLine.EMPTY, 10)
            printText("Order:00001;TOTAL: \$2.94; Date:${getCurrentDate()}", TextStyle.getStyle().setTextSize(24))
            autoOut()
        }

    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}