package com.example.darktunnel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.darktunnel.tunnel.SshTunnelEngine
import com.example.darktunnel.data.TunnelConfig
import kotlinx.coroutines.*

class TunnelService : Service() {

    companion object {

        const val ACTION_START =
            "com.example.darktunnel.START"

        const val ACTION_STOP =
            "com.example.darktunnel.STOP"

        const val EXTRA_HOST = "host"
        const val EXTRA_PORT = "port"

        const val CHANNEL_ID =
            "darktunnel_channel"

        const val NOTIFICATION_ID = 1001
    }

    private val scope =
        CoroutineScope(
            SupervisorJob() +
            Dispatchers.IO
        )

    private var engine:
        SshTunnelEngine? = null

    override fun onCreate() {

        super.onCreate()

        createChannel()

        startForeground(
            NOTIFICATION_ID,
            notification("Starting...")
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_START -> {

                val host =
                    intent.getStringExtra(EXTRA_HOST)
                        ?: return START_NOT_STICKY

                val port =
                    intent.getIntExtra(
                        EXTRA_PORT,
                        22
                    )

                startTunnel(host, port)
            }

            ACTION_STOP -> {

                stopTunnel()
            }
        }

        return START_STICKY
    }

    private fun startTunnel(
        host: String,
        port: Int
    ) {

        scope.launch {

            try {

                val config =
                    TunnelConfig(
                        host = host,
                        port = port
                    )

                engine =
                    SshTunnelEngine()

                updateNotification(
                    "Connecting..."
                )

                engine?.connect(config) {
                    updateNotification(it)
                }

                updateNotification(
                    "Connected"
                )

            } catch (e: Exception) {

                updateNotification(
                    "ERROR: ${e.message}"
                )

                stopSelf()
            }
        }
    }

    private fun stopTunnel() {

        scope.launch {

            engine?.disconnect {
                updateNotification(it)
            }

            engine = null

            stopForeground(
                STOP_FOREGROUND_REMOVE
            )

            stopSelf()
        }
    }

    private fun updateNotification(
        message: String
    ) {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            notification(message)
        )
    }

    private fun notification(
        message: String
    ): Notification {

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("DarkTunnel")
            .setContentText(message)
            .setSmallIcon(
                android.R.drawable
                    .stat_sys_data_bluetooth
            )
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "DarkTunnel Tunnel",
                NotificationManager
                    .IMPORTANCE_LOW
            )

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    override fun onDestroy() {

        scope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}
