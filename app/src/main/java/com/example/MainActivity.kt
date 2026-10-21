package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.IoTAmberWarning
import com.example.ui.theme.IoTBackgroundDark
import com.example.ui.theme.IoTBlueReverse
import com.example.ui.theme.IoTCyanAccent
import com.example.ui.theme.IoTCyanBright
import com.example.ui.theme.IoTGreenForward
import com.example.ui.theme.IoTRedStop
import com.example.ui.theme.IoTSurfaceCard
import com.example.ui.theme.IoTSurfaceElevated
import com.example.ui.theme.IoTTextPrimary
import com.example.ui.theme.IoTTextSecondary
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AutoIoTControlApp()
            }
        }
    }
}

/**
 * Obtiene la lista de permisos en tiempo de ejecución requeridos según el nivel de API de Android
 * para el descubrimiento de red local y cercanía IoT (ISO/IEC 27400).
 */
fun getRequiredIoTPermissions(): Array<String> {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    return permissions.toTypedArray()
}

fun evaluateMissingPermissions(context: Context, permissions: Array<String>): List<String> {
    return permissions.filter { perm ->
        ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun AutoIoTControlApp(iotViewModel: IoTViewModel = viewModel()) {
    val uiState by iotViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val requiredPermissions = remember { getRequiredIoTPermissions() }

    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var showEsp32CodeDialog by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val missing = evaluateMissingPermissions(context, requiredPermissions)
        val allGranted = missing.isEmpty() || resultMap.values.all { it }
        iotViewModel.updatePermissionsState(allGranted = missing.isEmpty(), missing = missing)
        if (!allGranted && missing.isNotEmpty()) {
            showPermissionDialog = true
        }
    }

    // Verifica permisos al iniciar y al volver desde la pantalla de Configuración del sistema
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val missing = evaluateMissingPermissions(context, requiredPermissions)
                iotViewModel.updatePermissionsState(allGranted = missing.isEmpty(), missing = missing)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val missing = evaluateMissingPermissions(context, requiredPermissions)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            iotViewModel.updatePermissionsState(allGranted = true, missing = emptyList())
        }
    }

    if (showPermissionDialog && !uiState.permissionsGranted) {
        Iso27400PermissionRationaleDialog(
            missingPermissions = uiState.missingPermissionsList,
            onRetryRequest = {
                showPermissionDialog = false
                permissionLauncher.launch(requiredPermissions)
            },
            onOpenSettings = {
                showPermissionDialog = false
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            },
            onDismiss = { showPermissionDialog = false }
        )
    }

    if (showEsp32CodeDialog) {
        BackHandler { showEsp32CodeDialog = false }
        Esp32FirmwareDialog(onDismiss = { showEsp32CodeDialog = false })
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(IoTBackgroundDark)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = IoTBackgroundDark
    ) { innerPadding ->
        if (!uiState.isAuthenticated) {
            LoginScreenIso27400(
                uiState = uiState,
                onLogin = { user, pass -> iotViewModel.login(user, pass) },
                onRegister = { user, pass -> iotViewModel.registerNewOperator(user, pass) },
                onClearFeedback = { iotViewModel.clearAuthFeedback() },
                onRequestPermissions = { permissionLauncher.launch(requiredPermissions) },
                onOpenSettings = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            IoTDashboardScreen(
                uiState = uiState,
                viewModel = iotViewModel,
                onRequestPermissions = { permissionLauncher.launch(requiredPermissions) },
                onShowPermissionModal = { showPermissionDialog = true },
                onShowEsp32Code = { showEsp32CodeDialog = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ============================================================================
// 1. PANTALLA DE LOGIN Y CREDENCIALES SEGURAS (ISO/IEC 27400)
// ============================================================================

@Composable
fun LoginScreenIso27400(
    uiState: IoTUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onClearFeedback: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val securityHelper = remember { SecurityManager(context) }

    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf(uiState.registeredOperator) }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val liveInputSha256 = remember(password) {
        if (password.isBlank()) "Esperando entrada para calcular SHA-256..."
        else securityHelper.sha256(password)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encabezado Institucional TI3042
            Card(
                colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
                border = BorderStroke(1.dp, IoTCyanAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = IoTCyanAccent.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Escudo ISO 27400",
                                    tint = IoTCyanBright,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "AUTO_IOT_CONTROL • ES2",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = IoTCyanBright
                            )
                            Text(
                                text = "TI3042 — Aplicaciones Móviles para IoT",
                                style = MaterialTheme.typography.bodySmall,
                                color = IoTTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Arquitectura de Ciberseguridad IoT/OT bajo norma ISO/IEC 27400. Credenciales almacenadas en EncryptedSharedPreferences (AES-256-GCM) con validación por resumen criptográfico SHA-256.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IoTTextSecondary
                    )
                }
            }

            // Banner de estado de permisos en Runtime (No bloqueante)
            if (!uiState.permissionsGranted) {
                PermissionWarningBanner(
                    missingPermissions = uiState.missingPermissionsList,
                    onRequestPermissions = onRequestPermissions,
                    onOpenSettings = onOpenSettings
                )
            }

            // Tarjeta Principal de Login / Registro Cifrado
            Card(
                colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isRegisterMode,
                            onClick = {
                                isRegisterMode = false
                                onClearFeedback()
                            },
                            label = { Text("Iniciar Sesión") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IoTCyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = IoTCyanBright
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_login_chip")
                        )

                        FilterChip(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                onClearFeedback()
                            },
                            label = { Text("Registrar Clave") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IoTGreenForward.copy(alpha = 0.25f),
                                selectedLabelColor = IoTGreenForward
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_register_chip")
                        )
                    }

                    Text(
                        text = if (isRegisterMode) {
                            "Registro de Operador IoT (Genera nuevo Hash SHA-256)"
                        } else {
                            "Validación de Operador Registrado (${uiState.registeredOperator})"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IoTTextPrimary
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            onClearFeedback()
                        },
                        label = { Text("ID de Operador") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Security, contentDescription = null, tint = IoTCyanAccent)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IoTCyanAccent,
                            unfocusedBorderColor = IoTTextSecondary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onClearFeedback()
                        },
                        label = {
                            Text(
                                if (isRegisterMode) "Nueva Clave (mín. 4 caracteres)"
                                else "Clave de Seguridad"
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = IoTCyanAccent)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar visibilidad de clave"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IoTCyanAccent,
                            unfocusedBorderColor = IoTTextSecondary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Mensajes de error o éxito de autenticación
                    AnimatedVisibility(visible = uiState.authErrorMessage != null) {
                        Surface(
                            color = IoTRedStop.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, IoTRedStop),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = IoTRedStop)
                                Text(
                                    text = uiState.authErrorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                onRegister(username, password)
                            } else {
                                onLogin(username, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegisterMode) IoTGreenForward else IoTCyanAccent,
                            contentColor = if (isRegisterMode) Color.White else Color(0xFF00252B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        Icon(
                            imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) {
                                "CIFRAR CON SHA-256 Y ENTRAR"
                            } else {
                                "VALIDAR HASH SHA-256 E INICIAR SESIÓN"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Acceso rápido para demostración en laboratorio TI3042
                    HorizontalDivider(color = IoTTextSecondary.copy(alpha = 0.2f))
                    Text(
                        text = "Inspector Criptográfico ISO/IEC 27400 (Cero Texto Plano):",
                        style = MaterialTheme.typography.labelMedium,
                        color = IoTCyanBright,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        color = IoTBackgroundDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "• Vault: EncryptedSharedPreferences (AES256_SIV / AES256_GCM)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = IoTTextSecondary
                            )
                            Text(
                                text = "• Operador en Vault: ${uiState.registeredOperator}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = IoTTextSecondary
                            )
                            Text(
                                text = "• Hash Almacenado: ${uiState.storedHashPreview}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = IoTCyanBright
                            )
                            Text(
                                text = "• Hash Entrada (SHA-256): ${liveInputSha256.take(28)}...",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = IoTGreenForward
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nota Evaluación: Puede registrar su propia clave en la pestaña 'Registrar Clave' o usar el perfil pre-inicializado.",
                            style = MaterialTheme.typography.labelSmall,
                            color = IoTTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                // Registra/autentica un operador de prueba en vivo generando el hash SHA-256 en el acto
                                isRegisterMode = true
                                username = "operador_iot"
                            },
                            border = BorderStroke(1.dp, IoTCyanAccent.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag("switch_to_register_button")
                        ) {
                            Text("Configurar Clave", fontSize = 11.sp, color = IoTCyanBright)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. DASHBOARD PRINCIPAL IoT (SOPORTA ROTACIÓN PORTRAIT / LANDSCAPE)
// ============================================================================

@Composable
fun IoTDashboardScreen(
    uiState: IoTUiState,
    viewModel: IoTViewModel,
    onRequestPermissions: () -> Unit,
    onShowPermissionModal: () -> Unit,
    onShowEsp32Code: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(IoTBackgroundDark)
    ) {
        val isLandscapeOrExpanded = maxWidth >= 680.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Barra Superior de Rol Activo + Estado de Conexión + Cierre de Sesión Seguro
            TopStatusHeaderBar(
                uiState = uiState,
                onShowEsp32Code = onShowEsp32Code,
                onLogout = { viewModel.logout() }
            )

            // Banner de Permisos ISO 27400 si alguno fue denegado (Sin bloquear la aplicación)
            if (!uiState.permissionsGranted) {
                PermissionWarningBanner(
                    missingPermissions = uiState.missingPermissionsList,
                    onRequestPermissions = onRequestPermissions,
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    },
                    onShowDetails = onShowPermissionModal
                )
            }

            if (isLandscapeOrExpanded) {
                // Diseño Canónico de 2 Paneles para Modo Horizontal (Landscape) / Tablets
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleAndNetworkConfigCard(uiState = uiState, viewModel = viewModel)
                        TelemetryLogConsoleCard(
                            logs = uiState.logs,
                            packetsCount = uiState.packetsCount,
                            lastLatencyMs = uiState.lastLatencyMs,
                            onClearLogs = { viewModel.clearLogs() }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UltrasonicTelemetryCard(uiState = uiState, viewModel = viewModel)
                        MotorControlPadCard(uiState = uiState, viewModel = viewModel)
                    }
                }
            } else {
                // Diseño Vertical (Portrait)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleAndNetworkConfigCard(uiState = uiState, viewModel = viewModel)
                    UltrasonicTelemetryCard(uiState = uiState, viewModel = viewModel)
                    MotorControlPadCard(uiState = uiState, viewModel = viewModel)
                    TelemetryLogConsoleCard(
                        logs = uiState.logs,
                        packetsCount = uiState.packetsCount,
                        lastLatencyMs = uiState.lastLatencyMs,
                        onClearLogs = { viewModel.clearLogs() }
                    )
                }
            }
        }
    }
}

@Composable
fun TopStatusHeaderBar(
    uiState: IoTUiState,
    onShowEsp32Code: () -> Unit,
    onLogout: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (uiState.connectionStatus) {
            ConnectionStatus.CONNECTED -> IoTGreenForward
            ConnectionStatus.RECONECTANDO -> IoTAmberWarning
            ConnectionStatus.DISCONNECTED -> IoTRedStop
        },
        animationSpec = tween(250),
        label = "statusColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicador de Rol Activo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = IoTCyanAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, IoTCyanAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.activeRole == DeviceRole.SENSOR_NODE) {
                                    Icons.Default.Memory
                                } else {
                                    Icons.Default.SettingsInputAntenna
                                },
                                contentDescription = null,
                                tint = IoTCyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = uiState.activeRole.badge,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = IoTCyanBright,
                                modifier = Modifier.testTag("active_role_badge")
                            )
                        }
                    }

                    // Indicador de Estado de Conexión ("CONECTADO", "DESCONECTADO", "RECONECTANDO")
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, statusColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = uiState.connectionStatus.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor,
                                modifier = Modifier.testTag("connection_status_badge")
                            )
                        }
                    }
                }

                // Acciones rápidas: Ver Código ESP32 y Cerrar Sesión Segura
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onShowEsp32Code,
                        modifier = Modifier.testTag("esp32_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Ver código ESP32 /status",
                            tint = IoTCyanBright
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión segura",
                            tint = IoTRedStop
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.statusDetailMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = IoTTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Op: ${uiState.activeOperator}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IoTCyanBright,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ============================================================================
// 3. TARJETA DE SELECCIÓN DE ROL E INTERCONEXIÓN INALÁMBRICA (P2P TCP / ESP32)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleAndNetworkConfigCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "1. Selección de Rol de Interconexión Inalámbrica",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = IoTTextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSensor = uiState.activeRole == DeviceRole.SENSOR_NODE
                Button(
                    onClick = { viewModel.selectDeviceRole(DeviceRole.SENSOR_NODE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSensor) IoTCyanAccent else IoTSurfaceElevated,
                        contentColor = if (isSensor) Color(0xFF00252B) else IoTTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_role_sensor_button")
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ROL A: NODO SENSOR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                val isStation = uiState.activeRole == DeviceRole.CONTROL_STATION
                Button(
                    onClick = { viewModel.selectDeviceRole(DeviceRole.CONTROL_STATION) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStation) IoTCyanAccent else IoTSurfaceElevated,
                        contentColor = if (isStation) Color(0xFF00252B) else IoTTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_role_station_button")
                ) {
                    Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ROL B: ESTACIÓN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = IoTTextSecondary.copy(alpha = 0.2f))

            if (uiState.activeRole == DeviceRole.SENSOR_NODE) {
                // Configuración del ROL A: NODO SENSOR / VEHÍCULO (ServerSocket TCP Puerto 8888)
                Surface(
                    color = IoTBackgroundDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IoTCyanAccent.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "IP Local del Nodo Sensor (Ingrese esta IP en el Teléfono B):",
                                style = MaterialTheme.typography.labelSmall,
                                color = IoTTextSecondary
                            )
                            Text(
                                text = "${uiState.localIpAddress}:${uiState.tcpPort}",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = IoTCyanBright,
                                modifier = Modifier.testTag("sensor_local_ip_text")
                            )
                            Text(
                                text = "Clientes conectados: ${uiState.connectedClientsCount} • Tramas JSON cada 200 ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = IoTTextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.refreshLocalIpAddress() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar IP local",
                                tint = IoTCyanAccent
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.toggleSensorNodeServer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isNetworkLoopActive) IoTRedStop else IoTGreenForward,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_sensor_server_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isNetworkLoopActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isNetworkLoopActive) {
                            "DETENER SERVIDOR TCP (PUERTO ${uiState.tcpPort})"
                        } else {
                            "INICIAR SERVIDOR TCP NODO SENSOR (PUERTO ${uiState.tcpPort})"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Configuración del ROL B: ESTACIÓN DE CONTROL (Cliente TCP P2P o HTTP ESP32)
                val isEsp32Http = uiState.connectionMode == ControlConnectionMode.ESP32_HTTP
                Surface(
                    color = IoTBackgroundDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.connectionMode.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = IoTCyanBright
                            )
                            Text(
                                text = uiState.connectionMode.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = IoTTextSecondary
                            )
                        }
                        Switch(
                            checked = isEsp32Http,
                            onCheckedChange = { checked ->
                                val newMode = if (checked) {
                                    ControlConnectionMode.ESP32_HTTP
                                } else {
                                    ControlConnectionMode.ANDROID_P2P_TCP
                                }
                                viewModel.setControlConnectionMode(newMode)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IoTCyanBright,
                                checkedTrackColor = IoTCyanAccent.copy(alpha = 0.4f),
                                uncheckedThumbColor = IoTGreenForward,
                                uncheckedTrackColor = IoTGreenForward.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("connection_mode_switch")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.targetIpAddress,
                        onValueChange = { viewModel.updateTargetIpAddress(it) },
                        label = {
                            Text(
                                if (isEsp32Http) "IP ESP32 SoftAP (HTTP :80)"
                                else "IP del Teléfono Nodo Sensor (TCP :8888)"
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = IoTCyanAccent)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IoTCyanAccent,
                            unfocusedBorderColor = IoTTextSecondary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_ip_input")
                    )

                    Button(
                        onClick = { viewModel.toggleControlStationConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isNetworkLoopActive) IoTRedStop else IoTCyanAccent,
                            contentColor = if (uiState.isNetworkLoopActive) Color.White else Color(0xFF00252B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("toggle_station_connect_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isNetworkLoopActive) Icons.Default.Stop else Icons.Default.Sync,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isNetworkLoopActive) "CORTAR" else "CONECTAR",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Chips rápidos de IPs frecuentes para agilizar la demostración en sala
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = {
                            viewModel.setControlConnectionMode(ControlConnectionMode.ESP32_HTTP)
                            viewModel.updateTargetIpAddress("192.168.4.1")
                        },
                        label = { Text("ESP32 SoftAP (192.168.4.1)", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = IoTSurfaceElevated)
                    )
                    AssistChip(
                        onClick = {
                            viewModel.setControlConnectionMode(ControlConnectionMode.ANDROID_P2P_TCP)
                            viewModel.updateTargetIpAddress(uiState.localIpAddress)
                        },
                        label = { Text("Mi Subred (${uiState.localIpAddress})", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = IoTSurfaceElevated)
                    )
                    AssistChip(
                        onClick = { viewModel.useLocalhostForLoopbackTest() },
                        label = { Text("Loopback (127.0.0.1)", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = IoTSurfaceElevated)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. CAJA DE TELEMETRÍA ULTRASÓNICA HC-SR04 (#00BCD4) Y REGLA CRÍTICA DE 15 CM
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UltrasonicTelemetryCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    val isEmergency = uiState.distanciaCm <= IoTViewModel.DISTANCIA_SEGURIDAD
    val alertAccentColor by animateColorAsState(
        targetValue = if (isEmergency) IoTRedStop else IoTCyanAccent,
        animationSpec = tween(250),
        label = "alertAccent"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
        border = BorderStroke(2.dp, alertAccentColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ultrasonic_telemetry_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera de Telemetría y Alerta de Freno de Emergencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = IoTCyanBright
                    )
                    Text(
                        text = "SENSOR ULTRASÓNICO HC-SR04",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = IoTCyanBright
                    )
                }

                // Indicador de Freno de Seguridad (<= 15 cm: "⚠️ FRENO ACTIVO", > 15 cm: "VÍA DESPEJADA")
                Surface(
                    color = if (isEmergency) IoTRedStop else IoTGreenForward.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isEmergency) Color.White else IoTGreenForward)
                ) {
                    Text(
                        text = if (isEmergency) "⚠️ FRENO ACTIVO" else "VÍA DESPEJADA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEmergency) Color.White else IoTGreenForward,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("safety_brake_status_text")
                    )
                }
            }

            // Caja Central con Radar Canvas + Distancia en cm + Estado del Motor L298N
            Surface(
                color = IoTBackgroundDark,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, IoTCyanAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Radar Ultrasónico en Canvas
                    UltrasonicRadarCanvas(
                        distanceCm = uiState.distanciaCm,
                        isEmergency = isEmergency,
                        modifier = Modifier.size(92.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Lectura principal de distancia (rango 2 a 400 cm)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "DISTANCIA OBSTÁCULO",
                            style = MaterialTheme.typography.labelSmall,
                            color = IoTTextSecondary
                        )
                        Text(
                            text = "${uiState.distanciaCm} cm",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isEmergency) IoTRedStop else IoTCyanBright,
                            modifier = Modifier.testTag("distance_value_text")
                        )
                        Text(
                            text = "Umbral Freno Seguridad: ${IoTViewModel.DISTANCIA_SEGURIDAD} cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEmergency) IoTRedStop else IoTTextSecondary
                        )
                    }

                    // Estado actual del Driver L298N y LED de Alerta
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = IoTSurfaceElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "DRIVER L298N",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IoTTextSecondary
                                )
                                val motorColor = when (uiState.estadoMotor) {
                                    IoTViewModel.MOTOR_AVANZANDO -> IoTGreenForward
                                    IoTViewModel.MOTOR_RETROCEDIENDO -> IoTBlueReverse
                                    else -> IoTRedStop
                                }
                                Text(
                                    text = uiState.estadoMotor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = motorColor,
                                    modifier = Modifier.testTag("motor_state_text")
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.ledAlertaEncendido) IoTRedStop else Color.Gray)
                            )
                            Text(
                                text = if (uiState.ledAlertaEncendido) "LED ALERTA: ON" else "LED ALERTA: OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.ledAlertaEncendido) IoTRedStop else IoTTextSecondary
                            )
                        }
                    }
                }
            }

            // Controles del Sensor Ultrasónico (Disponibles en Nodo Sensor y también como Simulador Local)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.activeRole == DeviceRole.SENSOR_NODE) {
                            "Control de Distancia del Nodo Sensor (2 - 400 cm):"
                        } else {
                            "Simulador Local HC-SR04 / Prueba de Freno a 15 cm:"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = IoTTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Barrido Auto",
                            style = MaterialTheme.typography.labelSmall,
                            color = IoTTextSecondary
                        )
                        Switch(
                            checked = uiState.autoUltrasonicSim,
                            onCheckedChange = { viewModel.toggleAutoUltrasonicSimulation(it) },
                            modifier = Modifier.testTag("auto_ultrasonic_switch")
                        )
                    }
                }

                Slider(
                    value = uiState.distanciaCm.toFloat(),
                    onValueChange = { newVal ->
                        viewModel.updateUltrasonicDistance(newVal.roundToInt())
                    },
                    valueRange = IoTViewModel.MIN_DISTANCIA_CM.toFloat()..IoTViewModel.MAX_DISTANCIA_CM.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = alertAccentColor,
                        activeTrackColor = alertAccentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("distance_slider")
                )

                // Botones de distancia predefinida para demostrar el freno de emergencia al profesor en 1 toque
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf(
                        8 to "8 cm (Freno!)",
                        15 to "15 cm (Límite)",
                        25 to "25 cm (Seguro)",
                        80 to "80 cm",
                        250 to "250 cm"
                    )
                    presets.forEach { (cm, label) ->
                        AssistChip(
                            onClick = { viewModel.updateUltrasonicDistance(cm) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (cm <= 15) {
                                    IoTRedStop.copy(alpha = 0.22f)
                                } else {
                                    IoTSurfaceElevated
                                },
                                labelColor = if (cm <= 15) IoTRedStop else IoTTextPrimary
                            ),
                            modifier = Modifier.testTag("preset_dist_${cm}_chip")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UltrasonicRadarCanvas(
    distanceCm: Int,
    isEmergency: Boolean,
    modifier: Modifier = Modifier
) {
    val radarColor = if (isEmergency) IoTRedStop else IoTCyanBright
    val normalized = ((distanceCm - 2f) / 398f).coerceIn(0.05f, 1f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 3.dp.toPx()

        // Fondo circular de sonar
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(radarColor.copy(alpha = 0.25f), Color.Transparent)
            ),
            radius = w / 2f,
            center = Offset(w / 2f, h / 2f)
        )

        // Arcos concéntricos de referencia
        for (factor in listOf(0.35f, 0.65f, 0.92f)) {
            val arcSize = w * factor
            val topLeft = Offset((w - arcSize) / 2f, (h - arcSize) / 2f)
            drawArc(
                color = radarColor.copy(alpha = 0.35f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Arco activo proporcional a la distancia medida
        val activeSize = w * (0.25f + 0.67f * normalized)
        val activeTopLeft = Offset((w - activeSize) / 2f, (h - activeSize) / 2f)
        drawArc(
            color = radarColor,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = activeTopLeft,
            size = Size(activeSize, activeSize),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Nodo central del vehículo
        drawCircle(
            color = radarColor,
            radius = 5.dp.toPx(),
            center = Offset(w / 2f, h * 0.78f)
        )
    }
}

// ============================================================================
// 5. PANEL DE BOTONES DE CONTROL GRANDES (ADELANTE #4CAF50, DETENER #F44336, ATRÁS #2196F3)
// ============================================================================

@Composable
fun MotorControlPadCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    val isForwardBlocked = uiState.distanciaCm <= IoTViewModel.DISTANCIA_SEGURIDAD

    Card(
        colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = IoTCyanBright
                    )
                    Text(
                        text = "MANDOS DE TRACCIÓN L298N",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = IoTTextPrimary
                    )
                }

                Text(
                    text = if (isForwardBlocked) {
                        "Avance bloqueado (<= 15 cm)"
                    } else {
                        "Tracción habilitada"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isForwardBlocked) IoTRedStop else IoTGreenForward,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón ADELANTE (Verde #4CAF50 - Se deshabilita visual y funcionalmente si distancia <= 15 cm)
            Button(
                onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ADELANTE) },
                enabled = !isForwardBlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IoTGreenForward,
                    contentColor = Color.White,
                    disabledContainerColor = IoTGreenForward.copy(alpha = 0.20f),
                    disabledContentColor = Color.White.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("btn_cmd_adelante")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Comando Adelante",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isForwardBlocked) {
                        "ADELANTE (BLOQUEADO POR FRENO <= 15 CM)"
                    } else {
                        "ADELANTE (AVANZAR)"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón DETENER (Rojo #F44336)
                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_DETENER) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IoTRedStop,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .testTag("btn_cmd_detener")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Comando Detener",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DETENER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Botón ATRÁS (Azul #2196F3)
                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ATRAS) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IoTBlueReverse,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .testTag("btn_cmd_atras")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Comando Atrás",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ATRÁS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. BITÁCORA EN VIVO DE SOCKETS TCP / HTTP Y AUDITORÍA ISO/IEC 27400
// ============================================================================

@Composable
fun TelemetryLogConsoleCard(
    logs: List<TelemetryLogEntry>,
    packetsCount: Long,
    lastLatencyMs: Long,
    onClearLogs: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IoTSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = IoTCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CONSOLA DE TELEMETRÍA (Dispatchers.IO)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = IoTTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tramas: $packetsCount • RTT: ${lastLatencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = IoTCyanBright
                    )
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar bitácora",
                            tint = IoTTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Surface(
                color = IoTBackgroundDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (logs.isEmpty()) {
                        Text(
                            text = "Sin eventos registrados en la bitácora.",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = IoTTextSecondary
                        )
                    } else {
                        logs.take(8).forEach { entry ->
                            Text(
                                text = "[${entry.timestamp}] [${entry.tag}] ${entry.message}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (entry.isWarning) IoTAmberWarning else IoTTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7. BANNER Y DIÁLOGO EXPLICATIVO DE PERMISOS RUNTIME (ISO/IEC 27400)
// ============================================================================

@Composable
fun PermissionWarningBanner(
    missingPermissions: List<String>,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowDetails: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF33260D)),
        border = BorderStroke(1.dp, IoTAmberWarning),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permission_warning_banner")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = IoTAmberWarning
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aviso de Permisos en Runtime (ISO/IEC 27400)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = IoTAmberWarning
                    )
                    Text(
                        text = "Se rechazaron ${missingPermissions.size} permisos de cercanía/red. La app opera sin bloqueos, pero se recomiendan para auditoría de red Wi-Fi/Bluetooth.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IoTTextPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onShowDetails != null) {
                    TextButton(onClick = onShowDetails) {
                        Text("¿Por qué?", color = IoTCyanBright, fontSize = 12.sp)
                    }
                }
                TextButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("banner_open_settings_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configuración", color = IoTTextPrimary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IoTAmberWarning,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("banner_retry_permissions_button")
                ) {
                    Text("Volver a Solicitar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun Iso27400PermissionRationaleDialog(
    missingPermissions: List<String>,
    onRetryRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IoTSurfaceCard,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = IoTAmberWarning,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Seguridad IoT y Permisos de Cercanía (ISO/IEC 27400)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IoTTextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Para descubrir y verificar de forma segura el Nodo Sensor o el SoftAP del ESP32 (192.168.4.1), Android requiere autorización en tiempo de ejecución para:",
                    style = MaterialTheme.typography.bodySmall,
                    color = IoTTextSecondary
                )
                missingPermissions.forEach { perm ->
                    val shortName = perm.substringAfterLast('.')
                    Text(
                        text = "• $shortName",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = IoTCyanBright
                    )
                }
                Text(
                    text = "Tolerancia a Denegación: Puede continuar usando los Sockets TCP/HTTP por IP manual sin que la aplicación se cierre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IoTGreenForward
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRetryRequest,
                colors = ButtonDefaults.buttonColors(containerColor = IoTCyanAccent, contentColor = Color.Black)
            ) {
                Text("Solicitar Nuevamente", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) {
                    Text("Abrir Ajustes", color = IoTAmberWarning)
                }
                TextButton(onClick = onDismiss) {
                    Text("Continuar en Modo IP", color = IoTTextSecondary)
                }
            }
        }
    )
}

// ============================================================================
// 8. MODAL CON ACTUALIZACIÓN DE FIRMWARE ESP32 (ARDUINO C++ ENDPOINT /status)
// ============================================================================

@Composable
fun Esp32FirmwareDialog(onDismiss: () -> Unit) {
    val snippet = remember {
        """
        // Endpoint JSON ligero para Evaluación Sumativa 2 (Auto_IoT_Control)
        server.on("/status", HTTP_GET, []() {
          long dist = medirDistanciaCM();
          bool freno = (dist <= DISTANCIA_SEGURIDAD);
          if (freno && estadoMotor == "AVANZANDO") {
            detenerMotor();
            estadoMotor = "DETENIDO";
          }
          digitalWrite(PIN_LED_ALERTA, freno ? HIGH : LOW);

          String json = "{";
          json += "\"distancia\":" + String(dist) + ",";
          json += "\"estado\":\"" + estadoMotor + "\",";
          json += "\"alerta\":" + String(freno ? "true" : "false");
          json += "}";
          server.send(200, "application/json", json);
        });
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IoTSurfaceCard,
        icon = {
            Icon(Icons.Default.Code, contentDescription = null, tint = IoTCyanBright)
        },
        title = {
            Text(
                text = "Endpoint /status para ESP32 (Arduino C++)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IoTTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Agregue este bloque en setup() de su sketch ESP32 (también incluido completo en /esp32_firmware/Auto_IoT_Control_ES2.ino):",
                    style = MaterialTheme.typography.bodySmall,
                    color = IoTTextSecondary
                )
                Surface(
                    color = IoTBackgroundDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = IoTCyanBright,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IoTCyanAccent, contentColor = Color.Black)
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        }
    )
}
