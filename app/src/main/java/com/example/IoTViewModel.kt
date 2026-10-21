package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class DeviceRole(val title: String, val badge: String) {
    SENSOR_NODE("NODO SENSOR / VEHÍCULO (Servidor TCP)", "NODO SENSOR"),
    CONTROL_STATION("ESTACIÓN DE CONTROL (Cliente)", "ESTACIÓN DE CONTROL")
}

enum class ConnectionStatus(val label: String) {
    DISCONNECTED("DESCONECTADO"),
    CONNECTED("CONECTADO"),
    RECONECTANDO("RECONECTANDO")
}

enum class ControlConnectionMode(val title: String, val subtitle: String) {
    ANDROID_P2P_TCP(
        "Modo Par-a-Par Android (Socket TCP :8888)",
        "Sincronización en vivo cada 200 ms con otro smartphone en Rol Nodo Sensor"
    ),
    ESP32_HTTP(
        "Modo Microcontrolador ESP32 (HTTP 192.168.4.1)",
        "Peticiones GET /adelante, /atras, /parar y telemetría /status o /"
    )
}

data class TelemetryLogEntry(
    val id: Long,
    val timestamp: String,
    val tag: String,
    val message: String,
    val isWarning: Boolean = false
)

data class IoTUiState(
    // Estado de Sesión e ISO 27400
    val isAuthenticated: Boolean = false,
    val activeOperator: String = "",
    val registeredOperator: String = SecurityManager.DEFAULT_OPERATOR_USER,
    val storedHashPreview: String = "",
    val lastAuthTimestamp: String = "",
    val lastSecurityAudit: String = "",
    val authErrorMessage: String? = null,
    val authSuccessMessage: String? = null,

    // Permisos en Runtime
    val permissionsGranted: Boolean = false,
    val missingPermissionsList: List<String> = emptyList(),
    val permissionRationaleDismissed: Boolean = false,

    // Rol y Conectividad Inalámbrica
    val activeRole: DeviceRole = DeviceRole.CONTROL_STATION,
    val connectionMode: ControlConnectionMode = ControlConnectionMode.ANDROID_P2P_TCP,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val statusDetailMessage: String = "Listo para iniciar enlace inalámbrico local",
    val localIpAddress: String = "0.0.0.0",
    val targetIpAddress: String = "192.168.4.1",
    val tcpPort: Int = IoTViewModel.DEFAULT_TCP_PORT,
    val isNetworkLoopActive: Boolean = false,
    val connectedClientsCount: Int = 0,

    // Telemetría del Vehículo IoT ("Auto_IoT_Control")
    val distanciaCm: Int = 65,
    val estadoMotor: String = IoTViewModel.MOTOR_DETENIDO,
    val frenoEmergenciaActivo: Boolean = false,
    val ledAlertaEncendido: Boolean = false,
    val autoUltrasonicSim: Boolean = false,
    val packetsCount: Long = 0L,
    val lastLatencyMs: Long = 0L,

    // Bitácora en vivo
    val logs: List<TelemetryLogEntry> = emptyList()
)

class IoTViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val DISTANCIA_SEGURIDAD = 15 // Regla crítica: <= 15 cm activa freno de emergencia
        const val MIN_DISTANCIA_CM = 2
        const val MAX_DISTANCIA_CM = 400
        const val DEFAULT_TCP_PORT = 8888
        const val DEFAULT_ESP32_IP = "192.168.4.1"

        const val MOTOR_AVANZANDO = "AVANZANDO"
        const val MOTOR_RETROCEDIENDO = "RETROCEDIENDO"
        const val MOTOR_DETENIDO = "DETENIDO"

        const val CMD_ADELANTE = "ADELANTE"
        const val CMD_DETENER = "DETENER"
        const val CMD_ATRAS = "ATRAS"
    }

    private val securityManager = SecurityManager(application)

    private val _uiState = MutableStateFlow(IoTUiState())
    val uiState: StateFlow<IoTUiState> = _uiState.asStateFlow()

    // Recursos de Red / Concurrencia en Dispatchers.IO
    @Volatile
    private var serverSocket: ServerSocket? = null
    private val activeClientWriters = CopyOnWriteArrayList<PrintWriter>()
    private val activeClientSockets = CopyOnWriteArrayList<Socket>()

    @Volatile
    private var clientSocket: Socket? = null
    @Volatile
    private var clientWriter: PrintWriter? = null

    private var serverJob: Job? = null
    private var clientJob: Job? = null
    private var ultrasonicSimJob: Job? = null
    private var logSequenceId = 0L

    init {
        refreshSecurityMetadata()
        refreshLocalIpAddress()
        applySafetyBrakeRule(initialDistance = 65, currentMotor = MOTOR_DETENIDO)
        appendLog("ISO-27400", "Almacén EncryptedSharedPreferences inicializado con SHA-256.")
    }

    fun refreshSecurityMetadata() {
        val hash = securityManager.getStoredPasswordHash()
        val preview = if (hash.length >= 16) "${hash.take(10)}...${hash.takeLast(6)}" else hash
        _uiState.update { state ->
            state.copy(
                isAuthenticated = securityManager.isSessionActive(),
                activeOperator = securityManager.getActiveUser(),
                registeredOperator = securityManager.getRegisteredUser(),
                storedHashPreview = preview,
                lastAuthTimestamp = securityManager.getLastAuthTime(),
                lastSecurityAudit = securityManager.getLastAuditEvent()
            )
        }
    }

    fun login(username: String, plainPassword: String) {
        val success = securityManager.authenticate(username, plainPassword)
        if (success) {
            refreshSecurityMetadata()
            _uiState.update {
                it.copy(
                    authErrorMessage = null,
                    authSuccessMessage = "Sesión autenticada con SHA-256"
                )
            }
            appendLog("AUTH", "Operador '${securityManager.getActiveUser()}' autenticado mediante hash SHA-256.")
        } else {
            refreshSecurityMetadata()
            _uiState.update {
                it.copy(
                    authErrorMessage = "Credenciales inválidas. El hash SHA-256 no coincide con el registro cifrado.",
                    authSuccessMessage = null
                )
            }
            appendLog("AUTH-WARN", "Intento de inicio de sesión rechazado para '$username'.", isWarning = true)
        }
    }

    fun registerNewOperator(username: String, plainPassword: String) {
        val result = securityManager.registerOperator(username, plainPassword)
        result.onSuccess { sha256Hash ->
            refreshSecurityMetadata()
            _uiState.update {
                it.copy(
                    authErrorMessage = null,
                    authSuccessMessage = "Operador registrado en EncryptedSharedPreferences (SHA-256: ${sha256Hash.take(12)}...)"
                )
            }
            appendLog("ISO-27400", "Nuevas credenciales cifradas guardadas para '$username'.")
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    authErrorMessage = error.message ?: "Error al registrar credenciales",
                    authSuccessMessage = null
                )
            }
        }
    }

    fun clearAuthFeedback() {
        _uiState.update { it.copy(authErrorMessage = null, authSuccessMessage = null) }
    }

    fun logout() {
        stopAllNetworkOperations()
        securityManager.logout()
        refreshSecurityMetadata()
        appendLog("AUTH", "Sesión cerrada de forma segura. Sockets liberados.")
    }

    fun updatePermissionsState(allGranted: Boolean, missing: List<String>) {
        _uiState.update {
            it.copy(
                permissionsGranted = allGranted,
                missingPermissionsList = missing
            )
        }
        if (allGranted) {
            appendLog("PERMISOS", "Permisos de red y cercanía IoT concedidos en tiempo de ejecución.")
            refreshLocalIpAddress()
        } else if (missing.isNotEmpty()) {
            appendLog(
                "PERMISOS",
                "Permisos denegados (${missing.size}). Modo degradado sin bloqueo activo.",
                isWarning = true
            )
        }
    }

    fun refreshLocalIpAddress() {
        viewModelScope.launch(Dispatchers.IO) {
            val detectedIp = obtainDeviceLocalIpAddress()
            _uiState.update { state ->
                val updatedTarget = if (state.targetIpAddress.isBlank()) detectedIp else state.targetIpAddress
                state.copy(
                    localIpAddress = detectedIp,
                    targetIpAddress = updatedTarget
                )
            }
        }
    }

    /**
     * Cambia entre ROL A (Nodo Sensor / Vehículo) y ROL B (Estación de Control).
     */
    fun selectDeviceRole(role: DeviceRole) {
        if (_uiState.value.activeRole == role) return
        stopAllNetworkOperations()
        refreshLocalIpAddress()

        val defaultStatusMsg = when (role) {
            DeviceRole.SENSOR_NODE -> "Modo Nodo Sensor listo. Inicie el Servidor TCP en puerto ${_uiState.value.tcpPort}."
            DeviceRole.CONTROL_STATION -> "Modo Estación de Control listo. Ingrese IP destino y conecte."
        }

        _uiState.update {
            it.copy(
                activeRole = role,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                statusDetailMessage = defaultStatusMsg,
                isNetworkLoopActive = false,
                connectedClientsCount = 0
            )
        }
        appendLog("ROL", "Rol cambiado a ${role.badge}.")
    }

    fun setControlConnectionMode(mode: ControlConnectionMode) {
        if (_uiState.value.connectionMode == mode) return
        stopClientConnection()
        val suggestedIp = when (mode) {
            ControlConnectionMode.ESP32_HTTP -> DEFAULT_ESP32_IP
            ControlConnectionMode.ANDROID_P2P_TCP -> {
                val current = _uiState.value.targetIpAddress
                if (current == DEFAULT_ESP32_IP && _uiState.value.localIpAddress != "127.0.0.1") {
                    _uiState.value.localIpAddress
                } else {
                    current
                }
            }
        }
        _uiState.update {
            it.copy(
                connectionMode = mode,
                targetIpAddress = suggestedIp,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                statusDetailMessage = "Modo seleccionado: ${mode.title}"
            )
        }
        appendLog("MODO", "Estación configurada en ${mode.title}.")
    }

    fun updateTargetIpAddress(newIp: String) {
        _uiState.update { it.copy(targetIpAddress = newIp.trim()) }
    }

    fun useLocalhostForLoopbackTest() {
        _uiState.update {
            it.copy(
                targetIpAddress = "127.0.0.1",
                connectionMode = ControlConnectionMode.ANDROID_P2P_TCP
            )
        }
        appendLog("RED", "IP destino ajustada a 127.0.0.1 (Loopback local). Ideal para probar en 1 equipo.")
    }

    /**
     * Regla crítica de negocio (Freno de seguridad a <= 15 cm).
     * Si distancia <= 15 cm:
     *  - Freno de emergencia automático: estadoMotor = "DETENIDO" (si estaba avanzando).
     *  - Bloquea comando de avanzar.
     *  - Enciende alerta visual "⚠️ FRENO ACTIVO" y LED de alerta.
     */
    fun updateUltrasonicDistance(newDistanceCm: Int) {
        val clamped = newDistanceCm.coerceIn(MIN_DISTANCIA_CM, MAX_DISTANCIA_CM)
        val previousState = _uiState.value
        val isEmergency = clamped <= DISTANCIA_SEGURIDAD

        val updatedMotor = if (isEmergency && previousState.estadoMotor == MOTOR_AVANZANDO) {
            appendLog(
                "FRENO-EMERGENCIA",
                "¡Obstáculo a $clamped cm (<= $DISTANCIA_SEGURIDAD cm)! Motor detenido automáticamente.",
                isWarning = true
            )
            MOTOR_DETENIDO
        } else {
            previousState.estadoMotor
        }

        _uiState.update {
            it.copy(
                distanciaCm = clamped,
                estadoMotor = updatedMotor,
                frenoEmergenciaActivo = isEmergency,
                ledAlertaEncendido = isEmergency
            )
        }
    }

    private fun applySafetyBrakeRule(initialDistance: Int, currentMotor: String) {
        val clamped = initialDistance.coerceIn(MIN_DISTANCIA_CM, MAX_DISTANCIA_CM)
        val isEmergency = clamped <= DISTANCIA_SEGURIDAD
        val safeMotor = if (isEmergency && currentMotor == MOTOR_AVANZANDO) MOTOR_DETENIDO else currentMotor
        _uiState.update {
            it.copy(
                distanciaCm = clamped,
                estadoMotor = safeMotor,
                frenoEmergenciaActivo = isEmergency,
                ledAlertaEncendido = isEmergency
            )
        }
    }

    /**
     * Simulación automática de lectura del sensor ultrasónico HC-SR04 (para demostración en vivo).
     */
    fun toggleAutoUltrasonicSimulation(enabled: Boolean) {
        _uiState.update { it.copy(autoUltrasonicSim = enabled) }
        ultrasonicSimJob?.cancel()
        if (!enabled) {
            appendLog("HC-SR04", "Lectura ultrasónica manual mediante Slider activada.")
            return
        }

        appendLog("HC-SR04", "Simulación automática de barrido ultrasónico iniciada.")
        ultrasonicSimJob = viewModelScope.launch(Dispatchers.Default) {
            var direction = -1
            while (isActive && _uiState.value.autoUltrasonicSim) {
                val current = _uiState.value.distanciaCm
                val step = if (current <= 30) 3 else 12
                var next = current + (direction * step)
                if (next <= 5) {
                    next = 5
                    direction = 1
                } else if (next >= 180) {
                    next = 180
                    direction = -1
                }
                updateUltrasonicDistance(next)
                delay(350L)
            }
        }
    }

    /**
     * Ejecuta un comando sobre el motor L298N:
     * - En ROL NODO SENSOR: aplica el comando localmente validando el freno de emergencia a 15 cm.
     * - En ROL ESTACIÓN DE CONTROL: envía el comando por Socket TCP o petición HTTP al ESP32.
     */
    fun sendMotorCommand(command: String) {
        val state = _uiState.value
        val normalizedCmd = command.uppercase(Locale.ROOT).trim()

        // Validación local de la regla crítica de seguridad (15 cm)
        if (normalizedCmd == CMD_ADELANTE && state.distanciaCm <= DISTANCIA_SEGURIDAD) {
            appendLog(
                "SEGURIDAD",
                "Comando ADELANTE bloqueado: Freno activo (${state.distanciaCm} cm <= $DISTANCIA_SEGURIDAD cm).",
                isWarning = true
            )
            _uiState.update {
                it.copy(
                    estadoMotor = MOTOR_DETENIDO,
                    frenoEmergenciaActivo = true,
                    ledAlertaEncendido = true,
                    statusDetailMessage = "⚠️ Comando ADELANTE bloqueado por freno de seguridad (<= 15 cm)"
                )
            }
            return
        }

        if (state.activeRole == DeviceRole.SENSOR_NODE) {
            applyLocalNodeCommand(normalizedCmd, origin = "Panel Local")
        } else {
            // Estamos en ROL B: Estación de Control
            when (state.connectionMode) {
                ControlConnectionMode.ANDROID_P2P_TCP -> sendTcpCommandFromStation(normalizedCmd)
                ControlConnectionMode.ESP32_HTTP -> sendHttpCommandToEsp32(normalizedCmd)
            }
        }
    }

    private fun applyLocalNodeCommand(command: String, origin: String) {
        val currentDist = _uiState.value.distanciaCm
        val isEmergency = currentDist <= DISTANCIA_SEGURIDAD

        val newMotorState = when (command) {
            CMD_ADELANTE, "FORWARD", "AVANZAR" -> {
                if (isEmergency) {
                    appendLog(
                        "FRENO-ACTIVO",
                        "Rechazado comando ADELANTE desde $origin (Distancia: $currentDist cm <= 15 cm).",
                        isWarning = true
                    )
                    MOTOR_DETENIDO
                } else {
                    appendLog("MOTOR-L298N", "[$origin] Motor -> AVANZANDO")
                    MOTOR_AVANZANDO
                }
            }
            CMD_ATRAS, "REVERSE", "RETROCEDER" -> {
                appendLog("MOTOR-L298N", "[$origin] Motor -> RETROCEDIENDO")
                MOTOR_RETROCEDIENDO
            }
            else -> {
                appendLog("MOTOR-L298N", "[$origin] Motor -> DETENIDO")
                MOTOR_DETENIDO
            }
        }

        _uiState.update {
            it.copy(
                estadoMotor = newMotorState,
                frenoEmergenciaActivo = isEmergency,
                ledAlertaEncendido = isEmergency
            )
        }
    }

    // =========================================================================
    // ROL A: SERVIDOR TCP (NODO SENSOR / VEHÍCULO) EN DISPATCHERS.IO
    // =========================================================================

    fun toggleSensorNodeServer() {
        if (_uiState.value.isNetworkLoopActive) {
            stopSensorNodeServer()
        } else {
            startSensorNodeServer()
        }
    }

    fun startSensorNodeServer() {
        stopAllNetworkOperations()
        refreshLocalIpAddress()

        val port = _uiState.value.tcpPort
        _uiState.update {
            it.copy(
                activeRole = DeviceRole.SENSOR_NODE,
                isNetworkLoopActive = true,
                connectionStatus = ConnectionStatus.RECONECTANDO,
                statusDetailMessage = "Iniciando ServerSocket TCP en puerto $port..."
            )
        }

        serverJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val server = ServerSocket()
                server.reuseAddress = true
                server.bind(InetSocketAddress(port))
                serverSocket = server

                val localIp = obtainDeviceLocalIpAddress()
                _uiState.update {
                    it.copy(
                        localIpAddress = localIp,
                        connectionStatus = ConnectionStatus.RECONECTANDO,
                        statusDetailMessage = "Esperando Estación de Control en $localIp:$port..."
                    )
                }
                appendLog("TCP-SERVER", "Servidor Nodo Sensor escuchando en $localIp:$port (Dispatchers.IO).")

                // Corrutina emisora de telemetría en tiempo real cada 200 ms a todas las estaciones conectadas
                val broadcastJob = launch(Dispatchers.IO) {
                    while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                        if (activeClientWriters.isNotEmpty()) {
                            val telemetryJson = buildTelemetryJsonPayload()
                            val failedWriters = mutableListOf<PrintWriter>()
                            for (writer in activeClientWriters) {
                                try {
                                    writer.println(telemetryJson)
                                    writer.flush()
                                    if (writer.checkError()) {
                                        failedWriters.add(writer)
                                    } else {
                                        _uiState.update { state ->
                                            state.copy(packetsCount = state.packetsCount + 1)
                                        }
                                    }
                                } catch (e: Exception) {
                                    failedWriters.add(writer)
                                }
                            }
                            if (failedWriters.isNotEmpty()) {
                                activeClientWriters.removeAll(failedWriters.toSet())
                                updateServerClientsStatus()
                            }
                        }
                        delay(200L)
                    }
                }

                // Bucle principal de aceptación de clientes TCP
                while (isActive && !server.isClosed) {
                    val client = server.accept()
                    client.tcpNoDelay = true
                    client.keepAlive = true
                    activeClientSockets.add(client)

                    val remoteAddress = client.inetAddress?.hostAddress ?: "Estación Remota"
                    appendLog("TCP-SERVER", "Estación de Control conectada desde $remoteAddress.")

                    launch(Dispatchers.IO) {
                        handleConnectedControlStationClient(client, remoteAddress)
                    }
                }

                broadcastJob.cancel()
            } catch (e: SocketException) {
                if (isActive && _uiState.value.isNetworkLoopActive) {
                    appendLog("TCP-SERVER", "Socket de servidor cerrado o interrumpido: ${e.message}", isWarning = true)
                }
            } catch (e: IOException) {
                if (isActive) {
                    appendLog("TCP-ERROR", "Error E/S en ServerSocket: ${e.message}", isWarning = true)
                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.DISCONNECTED,
                            isNetworkLoopActive = false,
                            statusDetailMessage = "Error al abrir puerto $port: ${e.localizedMessage}"
                        )
                    }
                }
            } finally {
                closeServerResourcesSafely()
            }
        }
    }

    private suspend fun handleConnectedControlStationClient(socket: Socket, remoteIp: String) =
        withContext(Dispatchers.IO) {
            var writer: PrintWriter? = null
            try {
                writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

                activeClientWriters.add(writer)
                updateServerClientsStatus(lastRemoteIp = remoteIp)

                // Envía trama inicial inmediata al conectar
                writer.println(buildTelemetryJsonPayload())

                while (isActive && !socket.isClosed) {
                    val rawLine = reader.readLine() ?: break
                    val line = rawLine.trim()
                    if (line.isEmpty()) continue

                    // Soporte híbrido: tramas de comando directo TCP o petición HTTP GET al Nodo
                    when {
                        line.startsWith("GET /adelante", ignoreCase = true) ||
                            line.equals(CMD_ADELANTE, ignoreCase = true) ||
                            line.contains("\"cmd\":\"ADELANTE\"", ignoreCase = true) -> {
                            applyLocalNodeCommand(CMD_ADELANTE, origin = remoteIp)
                            writer.println(buildTelemetryJsonPayload())
                        }
                        line.startsWith("GET /atras", ignoreCase = true) ||
                            line.equals(CMD_ATRAS, ignoreCase = true) ||
                            line.contains("\"cmd\":\"ATRAS\"", ignoreCase = true) -> {
                            applyLocalNodeCommand(CMD_ATRAS, origin = remoteIp)
                            writer.println(buildTelemetryJsonPayload())
                        }
                        line.startsWith("GET /parar", ignoreCase = true) ||
                            line.equals(CMD_DETENER, ignoreCase = true) ||
                            line.equals("PARAR", ignoreCase = true) ||
                            line.contains("\"cmd\":\"DETENER\"", ignoreCase = true) -> {
                            applyLocalNodeCommand(CMD_DETENER, origin = remoteIp)
                            writer.println(buildTelemetryJsonPayload())
                        }
                        line.startsWith("GET /status", ignoreCase = true) ||
                            line.startsWith("GET / ", ignoreCase = true) -> {
                            val json = buildTelemetryJsonPayload()
                            writer.print("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nConnection: close\r\n\r\n$json")
                            writer.flush()
                            break
                        }
                        line.equals("PING", ignoreCase = true) -> {
                            writer.println(buildTelemetryJsonPayload())
                        }
                    }
                }
            } catch (e: IOException) {
                appendLog(
                    "TOLERANCIA-FALLOS",
                    "Desconexión detectada de Estación ($remoteIp): ${e.message ?: "EOF"}. Esperando reconexión...",
                    isWarning = true
                )
            } finally {
                writer?.let { activeClientWriters.remove(it) }
                activeClientSockets.remove(socket)
                runCatching { socket.close() }
                updateServerClientsStatus()
            }
        }

    private fun updateServerClientsStatus(lastRemoteIp: String? = null) {
        val count = activeClientWriters.size
        val state = _uiState.value
        if (!state.isNetworkLoopActive || state.activeRole != DeviceRole.SENSOR_NODE) return

        if (count > 0) {
            _uiState.update {
                it.copy(
                    connectedClientsCount = count,
                    connectionStatus = ConnectionStatus.CONNECTED,
                    statusDetailMessage = "Estación conectada (${lastRemoteIp ?: "Activa"}) • Telemetría 200ms"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    connectedClientsCount = 0,
                    connectionStatus = ConnectionStatus.RECONECTANDO,
                    statusDetailMessage = "Desconectado - Reintentando / Esperando cliente en ${state.localIpAddress}:${state.tcpPort}..."
                )
            }
        }
    }

    fun stopSensorNodeServer() {
        _uiState.update {
            it.copy(
                isNetworkLoopActive = false,
                connectedClientsCount = 0,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                statusDetailMessage = "Servidor TCP detenido por el operador."
            )
        }
        serverJob?.cancel()
        serverJob = null
        viewModelScope.launch(Dispatchers.IO) {
            closeServerResourcesSafely()
        }
        appendLog("TCP-SERVER", "Servidor TCP cerrado limpiamente.")
    }

    private fun closeServerResourcesSafely() {
        activeClientWriters.clear()
        for (sock in activeClientSockets) {
            runCatching { sock.close() }
        }
        activeClientSockets.clear()
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    private fun buildTelemetryJsonPayload(): String {
        val s = _uiState.value
        val freno = s.distanciaCm <= DISTANCIA_SEGURIDAD
        val safeMotor = if (freno && s.estadoMotor == MOTOR_AVANZANDO) MOTOR_DETENIDO else s.estadoMotor
        return JSONObject().apply {
            put("distancia", s.distanciaCm)
            put("estado", safeMotor)
            put("alerta", freno)
            put("frenoActivo", freno)
            put("mensajeSeguridad", if (freno) "⚠️ FRENO ACTIVO" else "VÍA DESPEJADA")
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    // =========================================================================
    // ROL B: CLIENTE ESTACIÓN DE CONTROL (TCP P2P 8888 Y HTTP ESP32 192.168.4.1)
    // =========================================================================

    fun toggleControlStationConnection() {
        if (_uiState.value.isNetworkLoopActive) {
            stopClientConnection()
        } else {
            startControlStationConnection()
        }
    }

    fun startControlStationConnection() {
        val targetIp = _uiState.value.targetIpAddress.trim()
        if (targetIp.isEmpty()) {
            _uiState.update {
                it.copy(statusDetailMessage = "⚠️ Ingrese una dirección IP válida antes de conectar.")
            }
            return
        }

        stopAllNetworkOperations()
        _uiState.update {
            it.copy(
                activeRole = DeviceRole.CONTROL_STATION,
                isNetworkLoopActive = true,
                connectionStatus = ConnectionStatus.RECONECTANDO,
                statusDetailMessage = "Conectando a $targetIp..."
            )
        }

        when (_uiState.value.connectionMode) {
            ControlConnectionMode.ANDROID_P2P_TCP -> startTcpP2PClientLoop(targetIp, _uiState.value.tcpPort)
            ControlConnectionMode.ESP32_HTTP -> startEsp32HttpPollingLoop(targetIp)
        }
    }

    /**
     * Modo 1: Cliente Socket TCP Par-a-Par (Puerto 8888) en Dispatchers.IO.
     * Incluye tolerancia a fallos y reconexión automática si el Nodo Sensor se desconecta.
     */
    private fun startTcpP2PClientLoop(targetIp: String, port: Int) {
        clientJob = viewModelScope.launch(Dispatchers.IO) {
            var attempt = 1
            while (isActive && _uiState.value.isNetworkLoopActive) {
                var socket: Socket? = null
                try {
                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.RECONECTANDO,
                            statusDetailMessage = if (attempt == 1) {
                                "Abriendo Socket TCP hacia $targetIp:$port..."
                            } else {
                                "Desconectado - Reintentando conexión TCP ($attempt) a $targetIp:$port..."
                            }
                        )
                    }

                    val connectStart = System.currentTimeMillis()
                    socket = Socket()
                    socket.tcpNoDelay = true
                    socket.keepAlive = true
                    socket.soTimeout = 3500 // Timeout de lectura para detectar caída de Wi-Fi
                    socket.connect(InetSocketAddress(targetIp, port), 3000)

                    val writer = PrintWriter(
                        OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                        true
                    )
                    val reader = BufferedReader(
                        InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                    )

                    clientSocket = socket
                    clientWriter = writer
                    attempt = 1

                    val initialRtt = System.currentTimeMillis() - connectStart
                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.CONNECTED,
                            lastLatencyMs = initialRtt,
                            statusDetailMessage = "Sincronizado en vivo con Nodo $targetIp:$port (TCP 200ms)"
                        )
                    }
                    appendLog("TCP-CLIENT", "Conectado exitosamente a Nodo Sensor $targetIp:$port.")

                    // Lectura continua de tramas JSON enviadas cada 200ms por el Nodo Sensor
                    while (isActive && _uiState.value.isNetworkLoopActive && !socket.isClosed) {
                        val frameStart = System.currentTimeMillis()
                        val line = reader.readLine()
                            ?: throw IOException("Fin de flujo TCP (El Nodo Sensor cerró el socket)")

                        parseAndApplyTelemetryPayload(
                            rawPayload = line,
                            latencyMs = (System.currentTimeMillis() - frameStart).coerceAtLeast(1L)
                        )
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    // Tolerancia a fallos (ISO/IEC 27400): Captura IOException / SocketException sin crashear UI
                    if (isActive && _uiState.value.isNetworkLoopActive) {
                        appendLog(
                            "RECONEXIÓN-TCP",
                            "Enlace interrumpido ($targetIp:$port): ${e.javaClass.simpleName}. Reintentando...",
                            isWarning = true
                        )
                        _uiState.update {
                            it.copy(
                                connectionStatus = ConnectionStatus.RECONECTANDO,
                                statusDetailMessage = "Desconectado - Reintentando conexión a $targetIp:$port..."
                            )
                        }
                    }
                } finally {
                    // Cierre limpio garantizado de recursos de red
                    clientWriter = null
                    clientSocket = null
                    runCatching { socket?.close() }
                }

                if (isActive && _uiState.value.isNetworkLoopActive) {
                    attempt++
                    delay(2200L)
                }
            }
        }
    }

    private fun sendTcpCommandFromStation(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val writer = clientWriter
            val socket = clientSocket
            if (writer == null || socket == null || socket.isClosed || !socket.isConnected) {
                appendLog(
                    "TCP-WARN",
                    "No hay socket activo para enviar '$command'. Reconectando...",
                    isWarning = true
                )
                if (!_uiState.value.isNetworkLoopActive) {
                    startControlStationConnection()
                }
                return@launch
            }

            try {
                writer.println(command)
                writer.flush()
                if (writer.checkError()) {
                    throw IOException("Error de escritura en stream TCP")
                }
                appendLog("CMD-TCP", "Comando '$command' enviado al Nodo Sensor.")
            } catch (e: IOException) {
                appendLog("TCP-ERROR", "Fallo al enviar '$command': ${e.message}", isWarning = true)
                runCatching { socket.close() }
            }
        }
    }

    /**
     * Modo 2: Cliente HTTP para Microcontrolador ESP32 (IP 192.168.4.1 puerto 80).
     * Consulta /status (JSON) o / (HTML original de la Unidad 1) en Dispatchers.IO.
     */
    private fun startEsp32HttpPollingLoop(espIp: String) {
        clientJob = viewModelScope.launch(Dispatchers.IO) {
            var consecutiveFailures = 0
            appendLog("ESP32-HTTP", "Iniciando monitoreo HTTP hacia http://$espIp ...")

            while (isActive && _uiState.value.isNetworkLoopActive) {
                val startMs = System.currentTimeMillis()
                val statusResult = fetchEsp32Telemetry(espIp)

                if (statusResult.isSuccess) {
                    consecutiveFailures = 0
                    val rtt = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                    val payload = statusResult.getOrNull().orEmpty()
                    parseAndApplyTelemetryPayload(payload, latencyMs = rtt)

                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.CONNECTED,
                            statusDetailMessage = "Conectado a ESP32 ($espIp) • HTTP RTT ${rtt}ms"
                        )
                    }
                } else {
                    consecutiveFailures++
                    val err = statusResult.exceptionOrNull()?.javaClass?.simpleName ?: "IOException"
                    if (consecutiveFailures == 1 || consecutiveFailures % 5 == 0) {
                        appendLog(
                            "ESP32-RETRY",
                            "Sin respuesta HTTP de $espIp ($err). Reintentando automáticamente...",
                            isWarning = true
                        )
                    }
                    _uiState.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.RECONECTANDO,
                            statusDetailMessage = "Desconectado - Reintentando HTTP a http://$espIp (Intento $consecutiveFailures)..."
                        )
                    }
                }

                delay(if (consecutiveFailures == 0) 400L else 2000L)
            }
        }
    }

    private fun fetchEsp32Telemetry(espIp: String): Result<String> {
        // Primero intenta el endpoint ligero /status (JSON) y si retorna 404 cae al endpoint raíz / (Unidad 1)
        val statusEndpointResult = executeHttpGet("http://$espIp/status", timeoutMs = 1800)
        if (statusEndpointResult.isSuccess) {
            return statusEndpointResult
        }
        return executeHttpGet("http://$espIp/", timeoutMs = 1800)
    }

    private fun sendHttpCommandToEsp32(command: String) {
        val espIp = _uiState.value.targetIpAddress.trim().ifEmpty { DEFAULT_ESP32_IP }
        val endpointPath = when (command) {
            CMD_ADELANTE -> "/adelante"
            CMD_ATRAS -> "/atras"
            else -> "/parar"
        }

        viewModelScope.launch(Dispatchers.IO) {
            val url = "http://$espIp$endpointPath"
            val start = System.currentTimeMillis()
            val response = executeHttpGet(url, timeoutMs = 2000)
            response.onSuccess { body ->
                val rtt = System.currentTimeMillis() - start
                appendLog("CMD-HTTP", "GET $endpointPath -> 200 OK (${rtt}ms)")
                if (body.isNotBlank()) {
                    parseAndApplyTelemetryPayload(body, rtt)
                } else {
                    // Actualización optimista segura
                    val optimisticMotor = when (command) {
                        CMD_ADELANTE -> if (_uiState.value.distanciaCm <= DISTANCIA_SEGURIDAD) MOTOR_DETENIDO else MOTOR_AVANZANDO
                        CMD_ATRAS -> MOTOR_RETROCEDIENDO
                        else -> MOTOR_DETENIDO
                    }
                    _uiState.update { it.copy(estadoMotor = optimisticMotor) }
                }
            }.onFailure { err ->
                appendLog(
                    "HTTP-ERROR",
                    "Error en GET $endpointPath ($espIp): ${err.javaClass.simpleName}. Reintentando...",
                    isWarning = true
                )
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.RECONECTANDO,
                        statusDetailMessage = "Desconectado - Reintentando comando a $espIp..."
                    )
                }
            }
        }
    }

    private fun executeHttpGet(urlString: String, timeoutMs: Int): Result<String> {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                useCaches = false
                setRequestProperty("Accept", "application/json, text/html, text/plain")
                setRequestProperty("Connection", "close")
            }

            val code = connection.responseCode
            if (code in 200..299) {
                val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                Result.success(body)
            } else {
                Result.failure(IOException("HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            runCatching { connection?.disconnect() }
        }
    }

    /**
     * Parsea tanto el JSON estructurado:
     *   {"distancia": 50, "estado": "DETENIDO", "alerta": false}
     * como el HTML/texto original del ESP32 de la Unidad 1.
     */
    private fun parseAndApplyTelemetryPayload(rawPayload: String, latencyMs: Long) {
        val trimmed = rawPayload.trim()
        if (trimmed.isEmpty()) return

        try {
            if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)
                val rawDist = json.optInt("distancia", _uiState.value.distanciaCm)
                    .coerceIn(MIN_DISTANCIA_CM, MAX_DISTANCIA_CM)
                val rawEstado = json.optString("estado", _uiState.value.estadoMotor)
                    .uppercase(Locale.ROOT)

                val isBrakeActive = rawDist <= DISTANCIA_SEGURIDAD || json.optBoolean("alerta", false)
                val safeEstado = if (isBrakeActive && rawEstado == MOTOR_AVANZANDO) {
                    MOTOR_DETENIDO
                } else {
                    rawEstado
                }

                _uiState.update { state ->
                    state.copy(
                        distanciaCm = rawDist,
                        estadoMotor = safeEstado,
                        frenoEmergenciaActivo = isBrakeActive,
                        ledAlertaEncendido = isBrakeActive,
                        packetsCount = state.packetsCount + 1,
                        lastLatencyMs = latencyMs
                    )
                }
            } else {
                // Fallback parser para el HTML del ESP32 de la Evaluación 1
                val regexDist = Regex("""(?i)(?:distancia|sensor)[^0-9]{0,20}(\d{1,3})\s*cm""")
                val matchDist = regexDist.find(trimmed)?.groupValues?.getOrNull(1)?.toIntOrNull()
                val newDist = (matchDist ?: _uiState.value.distanciaCm).coerceIn(MIN_DISTANCIA_CM, MAX_DISTANCIA_CM)

                val upper = trimmed.uppercase(Locale.ROOT)
                val parsedMotor = when {
                    upper.contains("AVANZANDO") -> MOTOR_AVANZANDO
                    upper.contains("RETROCEDIENDO") -> MOTOR_RETROCEDIENDO
                    upper.contains("DETENIDO") -> MOTOR_DETENIDO
                    else -> _uiState.value.estadoMotor
                }

                val isBrake = newDist <= DISTANCIA_SEGURIDAD
                val finalMotor = if (isBrake && parsedMotor == MOTOR_AVANZANDO) MOTOR_DETENIDO else parsedMotor

                _uiState.update { state ->
                    state.copy(
                        distanciaCm = newDist,
                        estadoMotor = finalMotor,
                        frenoEmergenciaActivo = isBrake,
                        ledAlertaEncendido = isBrake,
                        packetsCount = state.packetsCount + 1,
                        lastLatencyMs = latencyMs
                    )
                }
            }
        } catch (e: Exception) {
            appendLog("PARSE", "Trama recibida no estándar: ${trimmed.take(45)}", isWarning = true)
        }
    }

    fun stopClientConnection() {
        _uiState.update {
            it.copy(
                isNetworkLoopActive = false,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                statusDetailMessage = "Conexión detenida por el operador."
            )
        }
        clientJob?.cancel()
        clientJob = null
        viewModelScope.launch(Dispatchers.IO) {
            clientWriter = null
            runCatching { clientSocket?.close() }
            clientSocket = null
        }
        appendLog("RED", "Cliente desconectado y sockets cerrados limpiamente.")
    }

    private fun stopAllNetworkOperations() {
        serverJob?.cancel()
        serverJob = null
        clientJob?.cancel()
        clientJob = null
        _uiState.update {
            it.copy(
                isNetworkLoopActive = false,
                connectedClientsCount = 0,
                connectionStatus = ConnectionStatus.DISCONNECTED
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            closeServerResourcesSafely()
            clientWriter = null
            runCatching { clientSocket?.close() }
            clientSocket = null
        }
    }

    private fun obtainDeviceLocalIpAddress(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // Prioriza wlan0 / ap0 (SoftAP o Wi-Fi local)
            val prioritized = interfaces.sortedBy { netIf ->
                when {
                    netIf.name.startsWith("wlan", ignoreCase = true) -> 0
                    netIf.name.startsWith("ap", ignoreCase = true) -> 1
                    netIf.name.startsWith("eth", ignoreCase = true) -> 2
                    else -> 3
                }
            }
            for (networkInterface in prioritized) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val host = address.hostAddress
                        if (!host.isNullOrBlank()) return host
                    }
                }
            }
            "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    private fun appendLog(tag: String, message: String, isWarning: Boolean = false) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val entry = TelemetryLogEntry(
            id = ++logSequenceId,
            timestamp = time,
            tag = tag,
            message = message,
            isWarning = isWarning
        )
        _uiState.update { state ->
            val updated = (listOf(entry) + state.logs).take(40)
            state.copy(logs = updated)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        ultrasonicSimJob?.cancel()
        serverJob?.cancel()
        clientJob?.cancel()
        closeServerResourcesSafely()
        clientWriter = null
        runCatching { clientSocket?.close() }
        clientSocket = null
    }
}
