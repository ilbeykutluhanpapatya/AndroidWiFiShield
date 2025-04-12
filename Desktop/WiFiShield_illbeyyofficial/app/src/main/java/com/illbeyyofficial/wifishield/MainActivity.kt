
package com.illbeyyofficial.wifishield

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private val REQUEST_CODE = 1
    private val CHANNEL_ID = "wifishield_alerts"
    private val whiteList = listOf("00:11:22:33:44:55", "AA:BB:CC:DD:EE:FF")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val scanButton = findViewById<Button>(R.id.scanButton)
        val statusText = findViewById<TextView>(R.id.statusText)

        scanButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE
                )
            } else {
                scanDevices(statusText)
            }
        }
    }

    private fun scanDevices(statusText: TextView) {
        val started = wifiManager.startScan()
        if (started) {
            val results: List<ScanResult> = wifiManager.scanResults
            val unknownDevices = results.filter { !whiteList.contains(it.BSSID.uppercase()) }

            if (unknownDevices.isNotEmpty()) {
                statusText.text = "Şüpheli cihazlar tespit edildi!"
                for (device in unknownDevices) {
                    showAlert("Yetkisiz Cihaz", "MAC: ${device.BSSID}")
                }
            } else {
                statusText.text = "Tüm cihazlar güvenli."
                Toast.makeText(this, "Yalnızca tanınan cihazlar bulundu.", Toast.LENGTH_SHORT).show()
            }

        } else {
            statusText.text = "Tarama başlatılamadı."
            Toast.makeText(this, "Tarama başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAlert(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "WiFiShield Alerts"
            val descriptionText = "Bilinmeyen cihazlar için uyarılar"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
