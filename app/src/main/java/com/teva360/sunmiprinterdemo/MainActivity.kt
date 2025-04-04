package com.teva360.sunmiprinterdemo

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.sunmi.printerx.PrinterSdk
import com.teva360.sunmiprinterdemo.databinding.ActivityMainBinding
import com.teva360.sunmiprinterdemo.sunmi_printer.PrinterService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var printerService: PrinterService

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar PrinterService
        printerService = PrinterService(this)

        // Conectar al servicio de impresión
        printerService.connectPrinterService { connected ->
            if (connected) {
                showToast("Servicio de impresión conectado")
                binding.txtStatus.text = getString(R.string.labelPrintStatus)
            } else {
                showToast("Error al conectar el servicio de impresión")
                binding.txtStatus.text = getString(R.string.labelPrintLabel)
            }
        }

        // Configurar botones para diferentes tipos de impresión
        binding.btnPrintText.setOnClickListener {
            printerService.printText("Impresión de texto simple\n")
        }

        binding.btnPrintBarcode.setOnClickListener {
            printerService.printBarcode("0123456789ABCDEFG")
        }

        binding.btnPrintQR.setOnClickListener {
            printerService.printQr("https://www.google.com")
        }

        binding.btnPrintImage.setOnClickListener {
            val option: BitmapFactory.Options = BitmapFactory.Options().apply {
                inScaled = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo, option)
            if (bitmap != null) {
                printerService.printImage(bitmap)
            } else {
                showToast("Error al cargar la imagen")
            }
        }

        binding.btnPrintReceipt.setOnClickListener {
            printerService.printReceipt(binding.main)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar el servicio de impresión
        printerService.disconnectPrinterService()
    }



    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Date())
    }

    private fun showToast(message: String) {
        Snackbar.make(binding.main, message, Snackbar.LENGTH_LONG).show()
    }
}