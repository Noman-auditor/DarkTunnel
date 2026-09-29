package com.example.darktunnel.data

data class TunnelConfig(
    val name: String = "DarkTunnel",
    val type: String = "SSH",
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String = ""
)
