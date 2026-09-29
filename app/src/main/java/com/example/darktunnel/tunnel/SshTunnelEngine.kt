package com.example.darktunnel.tunnel

import com.example.darktunnel.data.TunnelConfig
import java.net.Socket

class SshTunnelEngine {

    private var socket: Socket? = null

    fun connect(
        config: TunnelConfig,
        log: (String) -> Unit
    ) {

        log("Connecting to ${config.host}:${config.port}")

        socket = Socket(
            config.host,
            config.port
        )

        log("TCP connection established")
        log("SSH transport ready")
    }

    fun disconnect(
        log: (String) -> Unit
    ) {

        socket?.close()
        socket = null

        log("Disconnected")
    }

    fun isConnected(): Boolean {

        return socket?.isConnected == true &&
               socket?.isClosed == false
    }
}
