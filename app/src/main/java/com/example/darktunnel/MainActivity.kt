package com.example.darktunnel

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.darktunnel.data.TunnelConfig
import com.example.darktunnel.databinding.ActivityMainBinding
import com.example.darktunnel.service.TunnelService
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var binding:
        ActivityMainBinding

    private var connected = false

    private val importConfig =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri == null) return@registerForActivityResult

            try {

                val json =
                    contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }

                if (json != null) {

                    val config =
                        Gson().fromJson(
                            json,
                            TunnelConfig::class.java
                        )

                    binding.hostInput
                        .setText(config.host)

                    binding.portInput
                        .setText(
                            config.port.toString()
                        )

                    binding.userInput
                        .setText(config.username)

                    binding.passwordInput
                        .setText(config.password)

                    log("Config imported")
                }

            } catch (e: Exception) {

                log(
                    "Import error: ${e.message}"
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {

        val types =
            arrayOf(
                "SSH",
                "DIRECT"
            )

        binding.typeSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout
                    .simple_spinner_dropdown_item,
                types
            )

        binding.portInput.setText("22")

        binding.importButton
            .setOnClickListener {

                importConfig.launch(
                    arrayOf(
                        "application/json",
                        "text/plain"
                    )
                )
            }

        binding.connectButton
            .setOnClickListener {

                if (connected) {
                    disconnect()
                } else {
                    connect()
                }
            }
    }

    private fun connect() {

        val host =
            binding.hostInput
                .text
                .toString()
                .trim()

        val port =
            binding.portInput
                .text
                .toString()
                .toIntOrNull()
                ?: 22

        if (host.isEmpty()) {

            log("ERROR: Host required")
            return
        }

        val intent =
            Intent(
                this,
                TunnelService::class.java
            )

        intent.action =
            TunnelService.ACTION_START

        intent.putExtra(
            TunnelService.EXTRA_HOST,
            host
        )

        intent.putExtra(
            TunnelService.EXTRA_PORT,
            port
        )

        startForegroundService(intent)

        connected = true

        binding.connectButton
            .text = "DISCONNECT"

        binding.statusText
            .text = "CONNECTING"

        log(
            "Starting tunnel..."
        )
    }

    private fun disconnect() {

        val intent =
            Intent(
                this,
                TunnelService::class.java
            )

        intent.action =
            TunnelService.ACTION_STOP

        startService(intent)

        connected = false

        binding.connectButton
            .text = "CONNECT"

        binding.statusText
            .text = "DISCONNECTED"

        log("Disconnect requested")
    }

    private fun log(
        message: String
    ) {

        binding.logView.append(
            "\n$message"
        )
    }
}
