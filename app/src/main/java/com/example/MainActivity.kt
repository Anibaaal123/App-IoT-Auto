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
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.theme.CrimsonRedStop
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCobalt
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPinkLight
import com.example.ui.theme.CyberPinkNeon
import com.example.ui.theme.CyberRedAlert
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DimGraphiteText
import com.example.ui.theme.ForwardBtnBg
import com.example.ui.theme.ForwardBtnBorder
import com.example.ui.theme.ForwardBtnDisabledBg
import com.example.ui.theme.ForwardBtnDisabledBorder
import com.example.ui.theme.ForwardBtnDisabledText
import com.example.ui.theme.ForwardBtnText
import com.example.ui.theme.GpioHighActive
import com.example.ui.theme.GpioLowInactive
import com.example.ui.theme.LightGrayTerminalText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeutralCharcoalReverse
import com.example.ui.theme.OemBorderProminent
import com.example.ui.theme.OemBorderSubtle
import com.example.ui.theme.OemCanvasBackground
import com.example.ui.theme.OemCardSurface
import com.example.ui.theme.OemConsoleBg
import com.example.ui.theme.OemCriticalRed
import com.example.ui.theme.OemOptimalGreen
import com.example.ui.theme.OemSurfaceElevated
import com.example.ui.theme.OemTextColdWhite
import com.example.ui.theme.OemTextConsole
import com.example.ui.theme.OemTextDim
import com.example.ui.theme.OemTextSecondary
import com.example.ui.theme.OemTextWhite
import com.example.ui.theme.OemWarningAmber
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledDarkGraphiteCard
import com.example.ui.theme.OledElevatedSurface
import com.example.ui.theme.OledPureBlack
import com.example.ui.theme.OledTerminalBg
import com.example.ui.theme.SafePink
import com.example.ui.theme.TacticalBorderGlow
import com.example.ui.theme.TacticalDarkBackground
import com.example.ui.theme.TacticalSurfaceCard
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TextCockpitDim
import com.example.ui.theme.TextCockpitMuted
import com.example.ui.theme.TextCockpitWhite
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
            .background(TacticalDarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = TacticalDarkBackground
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
// 1. PANTALLA DE LOGIN Y SEGURIDAD CRIPTOGRÁFICA (ISO/IEC 27400)
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
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logotipo de la App
            Surface(
                color = OemSurfaceElevated,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, OemBorderProminent),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Logo Auto IoT",
                        tint = OemTextColdWhite,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Auto IoT Control",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = OemTextWhite,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tablero de Diagnóstico y Control Vehicular",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OemTextSecondary
                )
            }

            if (!uiState.permissionsGranted) {
                PermissionWarningBanner(
                    missingPermissions = uiState.missingPermissionsList,
                    onRequestPermissions = onRequestPermissions,
                    onOpenSettings = onOpenSettings
                )
            }

            // Tarjeta Principal de Inicio de Sesión
            Card(
                colors = CardDefaults.cardColors(containerColor = OemCardSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OemBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Crear Nuevo Operador" else "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OemTextWhite
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            onClearFeedback()
                        },
                        label = { Text("Usuario") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Security, contentDescription = null, tint = OemTextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OemTextColdWhite,
                            unfocusedBorderColor = OemBorderSubtle,
                            focusedLabelColor = OemTextColdWhite,
                            unfocusedLabelColor = OemTextSecondary,
                            focusedLeadingIconColor = OemTextColdWhite,
                            unfocusedLeadingIconColor = OemTextSecondary,
                            cursorColor = OemTextColdWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onClearFeedback()
                        },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = OemTextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar contraseña",
                                    tint = OemTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OemTextColdWhite,
                            unfocusedBorderColor = OemBorderSubtle,
                            focusedLabelColor = OemTextColdWhite,
                            unfocusedLabelColor = OemTextSecondary,
                            focusedLeadingIconColor = OemTextColdWhite,
                            unfocusedLeadingIconColor = OemTextSecondary,
                            cursorColor = OemTextColdWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mensaje de error
                    AnimatedVisibility(visible = uiState.authErrorMessage != null) {
                        Surface(
                            color = OemCriticalRed.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OemCriticalRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = OemCriticalRed)
                                Text(
                                    text = uiState.authErrorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OemTextWhite
                                )
                            }
                        }
                    }

                    // Botón Principal ('Entrar al panel')
                    Button(
                        onClick = {
                            if (isRegisterMode) onRegister(username, password)
                            else onLogin(username, password)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OemSurfaceElevated,
                            contentColor = OemTextWhite
                        ),
                        border = BorderStroke(1.dp, OemBorderProminent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) "CREAR CUENTA" else "ENTRAR AL PANEL",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Alternar entre Iniciar Sesión y Crear Cuenta
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            onClearFeedback()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión aquí" else "¿Registrar nuevo operador? Toca aquí",
                            color = OemTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. DASHBOARD PRINCIPAL IoT (DISEÑO ADAPTATIVO POR ROL)
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
            .background(TacticalDarkBackground)
    ) {
        val isLandscape = maxWidth >= 680.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TopStatusHeaderBar(
                uiState = uiState,
                onShowEsp32Code = onShowEsp32Code,
                onLogout = { viewModel.logout() }
            )

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

            if (isLandscape) {
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
                        if (uiState.activeRole == DeviceRole.CONTROL_STATION) {
                            MotorControlPadCard(uiState = uiState, viewModel = viewModel)
                        } else {
                            VehicleLocalTestCard(uiState = uiState, viewModel = viewModel)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleAndNetworkConfigCard(uiState = uiState, viewModel = viewModel)
                    UltrasonicTelemetryCard(uiState = uiState, viewModel = viewModel)
                    if (uiState.activeRole == DeviceRole.CONTROL_STATION) {
                        MotorControlPadCard(uiState = uiState, viewModel = viewModel)
                    } else {
                        VehicleLocalTestCard(uiState = uiState, viewModel = viewModel)
                    }
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
            ConnectionStatus.CONNECTED -> OemOptimalGreen
            ConnectionStatus.RECONECTANDO -> OemWarningAmber
            ConnectionStatus.DISCONNECTED -> OemCriticalRed
        },
        animationSpec = tween(250),
        label = "statusColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemBorderSubtle),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = OemSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, OemBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.activeRole == DeviceRole.SENSOR_NODE) Icons.Default.DirectionsCar else Icons.Default.SettingsInputAntenna,
                                contentDescription = null,
                                tint = OemTextColdWhite,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = uiState.activeRole.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OemTextColdWhite
                            )
                        }
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, statusColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = uiState.connectionStatus.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión segura",
                            tint = OemCriticalRed
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
                    color = OemTextSecondary,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Op: ${uiState.activeOperator}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OemTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ============================================================================
// 3. TARJETA DE ROL Y RED INALÁMBRICA (SERVIDOR TCP / CLIENTE ESP32)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleAndNetworkConfigCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemBorderSubtle),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "1. Rol Operativo del Smartphone en la Red IoT",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OemTextWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSensor = uiState.activeRole == DeviceRole.SENSOR_NODE
                Button(
                    onClick = { viewModel.selectDeviceRole(DeviceRole.SENSOR_NODE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSensor) OemSurfaceElevated else OemCardSurface,
                        contentColor = if (isSensor) OemTextWhite else OemTextSecondary
                    ),
                    border = BorderStroke(if (isSensor) 1.5.dp else 1.dp, if (isSensor) OemBorderProminent else OemBorderSubtle),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ROL A: EL AUTO (VEHÍCULO)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                val isStation = uiState.activeRole == DeviceRole.CONTROL_STATION
                Button(
                    onClick = { viewModel.selectDeviceRole(DeviceRole.CONTROL_STATION) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStation) OemSurfaceElevated else OemCardSurface,
                        contentColor = if (isStation) OemTextWhite else OemTextSecondary
                    ),
                    border = BorderStroke(if (isStation) 1.5.dp else 1.dp, if (isStation) OemBorderProminent else OemBorderSubtle),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ROL B: CONTROL REMOTO", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = OemBorderSubtle)

            if (uiState.activeRole == DeviceRole.SENSOR_NODE) {
                Surface(
                    color = OemCanvasBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OemBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NÚMERO DE ENLACE DE ESTE AUTO (IP):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OemTextSecondary
                            )
                            IconButton(onClick = { viewModel.refreshLocalIpAddress() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refrescar IP", tint = OemTextColdWhite)
                            }
                        }

                        Text(
                            text = "${uiState.localIpAddress}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = OemTextWhite
                        )

                        Text(
                            text = "💡 Escribe este número en el teléfono que usarás como Control Remoto para vincular ambos equipos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OemTextSecondary
                        )

                        if (uiState.connectedClientsCount > 0) {
                            Text(
                                text = "🟢 ¡Control remoto conectado con éxito! (${uiState.connectedClientsCount} enlazado)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OemOptimalGreen
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.toggleSensorNodeServer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isNetworkLoopActive) OemCriticalRed else OemSurfaceElevated,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (uiState.isNetworkLoopActive) OemCriticalRed else OemBorderProminent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isNetworkLoopActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isNetworkLoopActive) "DETENER AUTO" else "ACTIVAR AUTO",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                val isEsp32Http = uiState.connectionMode == ControlConnectionMode.ESP32_HTTP

                Text(
                    text = "¿A qué te vas a conectar?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OemTextWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isEsp32Http,
                        onClick = {
                            viewModel.setControlConnectionMode(ControlConnectionMode.ANDROID_P2P_TCP)
                            if (uiState.targetIpAddress == "192.168.4.1" && uiState.localIpAddress != "127.0.0.1") {
                                viewModel.updateTargetIpAddress(uiState.localIpAddress)
                            }
                        },
                        label = { Text("📱 A otro Celular (Auto)") },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OemSurfaceElevated,
                            selectedLabelColor = Color.White,
                            containerColor = OemCardSurface,
                            labelColor = OemTextSecondary
                        ),
                        border = BorderStroke(1.dp, if (!isEsp32Http) OemBorderProminent else OemBorderSubtle),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isEsp32Http,
                        onClick = {
                            viewModel.setControlConnectionMode(ControlConnectionMode.ESP32_HTTP)
                            viewModel.updateTargetIpAddress("192.168.4.1")
                        },
                        label = { Text("🚗 Al Auto Real (ESP32)") },
                        leadingIcon = {
                            Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OemSurfaceElevated,
                            selectedLabelColor = Color.White,
                            containerColor = OemCardSurface,
                            labelColor = OemTextSecondary
                        ),
                        border = BorderStroke(1.dp, if (isEsp32Http) OemBorderProminent else OemBorderSubtle),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = OemCanvasBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OemBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isEsp32Http) {
                            Text(
                                text = "📋 Pasos para conectar dos celulares:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OemTextColdWhite
                            )
                            Text(
                                text = "1. Conecta ambos celulares al mismo Wi-Fi (o comparte datos).\n2. En el celular del Auto pulsa 'ACTIVAR AUTO'.\n3. Escribe abajo el número IP que muestra ese celular:",
                                style = MaterialTheme.typography.bodySmall,
                                color = OemTextSecondary
                            )
                        } else {
                            Text(
                                text = "📋 Pasos para conectar al ESP32 real:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OemTextColdWhite
                            )
                            Text(
                                text = "1. Conéctate a la red Wi-Fi del vehículo ('Auto_IoT_Control').\n2. La dirección 192.168.4.1 ya está configurada.\n3. Presiona 'CONECTAR AL AUTO'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OemTextSecondary
                            )
                        }
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
                            Text(if (isEsp32Http) "IP del ESP32" else "IP que muestra el celular del Auto")
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = OemTextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OemTextColdWhite,
                            unfocusedBorderColor = OemBorderSubtle,
                            focusedLabelColor = OemTextColdWhite,
                            unfocusedLabelColor = OemTextSecondary,
                            focusedLeadingIconColor = OemTextColdWhite,
                            unfocusedLeadingIconColor = OemTextSecondary,
                            cursorColor = OemTextColdWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { viewModel.toggleControlStationConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isNetworkLoopActive) OemCriticalRed else OemSurfaceElevated,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, if (uiState.isNetworkLoopActive) OemCriticalRed else OemBorderProminent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isNetworkLoopActive) Icons.Default.Stop else Icons.Default.Sync,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isNetworkLoopActive) "CORTAR" else "CONECTAR",
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (!isEsp32Http) {
                    Text(
                        text = "Accesos rápidos:",
                        style = MaterialTheme.typography.labelSmall,
                        color = OemTextSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                viewModel.setControlConnectionMode(ControlConnectionMode.ANDROID_P2P_TCP)
                                viewModel.updateTargetIpAddress(uiState.localIpAddress)
                            },
                            label = { Text("Usar mi IP actual (${uiState.localIpAddress})", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = OemSurfaceElevated,
                                labelColor = OemTextSecondary
                            ),
                            border = BorderStroke(1.dp, OemBorderSubtle)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. SENSOR DE DISTANCIA Y FRENO AUTOMÁTICO A 15 CM
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UltrasonicTelemetryCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    val isEmergency = uiState.distanciaCm <= IoTViewModel.DISTANCIA_SEGURIDAD
    val alertAccentColor by animateColorAsState(
        targetValue = if (isEmergency) OemCriticalRed else OemOptimalGreen,
        animationSpec = tween(250),
        label = "alertAccent"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, if (isEmergency) OemCriticalRed else OemBorderSubtle),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = if (isEmergency) OemCriticalRed else OemOptimalGreen
                    )
                    Text(
                        text = "SENSOR DE DISTANCIA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OemTextColdWhite
                    )
                }

                Surface(
                    color = if (isEmergency) OemCriticalRed else OemOptimalGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isEmergency) OemCriticalRed else OemOptimalGreen)
                ) {
                    Text(
                        text = if (isEmergency) "⚠️ FRENO ACTIVO" else "VÍA DESPEJADA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmergency) Color.White else OemOptimalGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Surface(
                color = OemCanvasBackground,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isEmergency) OemCriticalRed else OemBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UltrasonicRadarCanvas(
                        distanceCm = uiState.distanciaCm,
                        isEmergency = isEmergency,
                        modifier = Modifier.size(92.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "DISTANCIA OBSTÁCULO",
                            style = MaterialTheme.typography.labelSmall,
                            color = OemTextSecondary
                        )
                        Text(
                            text = "${uiState.distanciaCm} cm",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isEmergency) OemCriticalRed else OemTextWhite
                        )
                        Text(
                            text = "Umbral Crítico: ${IoTViewModel.DISTANCIA_SEGURIDAD} cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEmergency) OemCriticalRed else OemTextSecondary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = OemSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, OemBorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "ESTADO MOTOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OemTextSecondary
                                )
                                val motorColor = when (uiState.estadoMotor) {
                                    IoTViewModel.MOTOR_AVANZANDO -> OemOptimalGreen
                                    IoTViewModel.MOTOR_RETROCEDIENDO -> OemWarningAmber
                                    else -> OemCriticalRed
                                }
                                Text(
                                    text = uiState.estadoMotor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = motorColor
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
                                    .background(if (uiState.ledAlertaEncendido) OemCriticalRed else OemBorderSubtle)
                            )
                            Text(
                                text = if (uiState.ledAlertaEncendido) "LED ALERTA: ON" else "LED ALERTA: OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.ledAlertaEncendido) OemCriticalRed else OemTextSecondary
                            )
                        }
                    }
                }
            }

            // Barra de Trayectoria Dinámica (2 a 400 cm)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trayectoria Dinámica (2 a 400 cm):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OemTextWhite
                    )

                    Text(
                        text = when (uiState.estadoMotor) {
                            IoTViewModel.MOTOR_AVANZANDO -> "▲ Acercándose (-3 cm / 200 ms)"
                            IoTViewModel.MOTOR_RETROCEDIENDO -> "▼ Alejándose (+3 cm / 200 ms)"
                            else -> "■ Posición Fija"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (uiState.estadoMotor) {
                            IoTViewModel.MOTOR_AVANZANDO -> OemOptimalGreen
                            IoTViewModel.MOTOR_RETROCEDIENDO -> OemWarningAmber
                            else -> OemTextSecondary
                        }
                    )
                }

                LinearProgressIndicator(
                    progress = { ((uiState.distanciaCm - 2f) / 398f).coerceIn(0.02f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = alertAccentColor,
                    trackColor = OemCanvasBackground
                )
            }

            // Señales Lógicas de Pines GPIO (ESP32 -> Driver L298N & LED)
            val motor = uiState.estadoMotor
            val enaState = if (motor == IoTViewModel.MOTOR_DETENIDO) "LOW" else "HIGH"
            val in1State = if (motor == IoTViewModel.MOTOR_AVANZANDO) "HIGH" else "LOW"
            val in2State = if (motor == IoTViewModel.MOTOR_RETROCEDIENDO) "HIGH" else "LOW"
            val ledPinState = if (isEmergency) "HIGH" else "LOW"

            Surface(
                color = OemCanvasBackground,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, OemBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Señales Lógicas en Pines GPIO (L298N & LED):",
                        style = MaterialTheme.typography.labelSmall,
                        color = OemTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PinIndicator(
                            label = "ENA (G14)",
                            value = enaState,
                            isActive = enaState == "HIGH",
                            modifier = Modifier.weight(1f)
                        )
                        PinIndicator(
                            label = "IN1 (G26)",
                            value = in1State,
                            isActive = in1State == "HIGH",
                            modifier = Modifier.weight(1f)
                        )
                        PinIndicator(
                            label = "IN2 (G27)",
                            value = in2State,
                            isActive = in2State == "HIGH",
                            modifier = Modifier.weight(1f)
                        )
                        PinIndicator(
                            label = "LED (G2)",
                            value = ledPinState,
                            isActive = isEmergency,
                            modifier = Modifier.weight(1f)
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
    val radarColor = if (isEmergency) OemCriticalRed else OemOptimalGreen
    val normalized = ((distanceCm - 2f) / 398f).coerceIn(0.05f, 1f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 3.dp.toPx()

        for (factor in listOf(0.35f, 0.65f, 0.92f)) {
            val arcSize = w * factor
            val topLeft = Offset((w - arcSize) / 2f, (h - arcSize) / 2f)
            drawArc(
                color = OemBorderSubtle,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

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

        drawCircle(
            color = radarColor,
            radius = 5.dp.toPx(),
            center = Offset(w / 2f, h * 0.78f)
        )
    }
}

// ============================================================================
// 5. INDICADOR DE PINES GPIO
// ============================================================================

@Composable
fun PinIndicator(
    label: String,
    value: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = if (label.startsWith("LED")) OemCriticalRed else OemOptimalGreen
    Surface(
        color = if (isActive) activeColor.copy(alpha = 0.15f) else OemSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isActive) activeColor else OemBorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = OemTextSecondary)
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isActive) activeColor else OemTextDim
            )
        }
    }
}

// ============================================================================
// 6. MODO PRUEBA LOCAL (EXCLUSIVO PARA ROL A: VEHÍCULO)
// ============================================================================

@Composable
fun VehicleLocalTestCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    val isBrake = uiState.distanciaCm <= IoTViewModel.DISTANCIA_SEGURIDAD
    val isAdvancing = uiState.estadoMotor == IoTViewModel.MOTOR_AVANZANDO
    val isReversing = uiState.estadoMotor == IoTViewModel.MOTOR_RETROCEDIENDO

    val forwardBg by animateColorAsState(
        targetValue = when {
            isBrake -> ForwardBtnDisabledBg
            isAdvancing -> OemOptimalGreen.copy(alpha = 0.22f)
            isReversing -> Color(0xFF1E2229)
            else -> ForwardBtnBg
        },
        animationSpec = tween(200),
        label = "testForwardBg"
    )
    val forwardBorderColor by animateColorAsState(
        targetValue = when {
            isBrake -> ForwardBtnDisabledBorder
            isAdvancing -> OemOptimalGreen
            isReversing -> OemBorderSubtle.copy(alpha = 0.5f)
            else -> ForwardBtnBorder
        },
        animationSpec = tween(200),
        label = "testForwardBorder"
    )
    val forwardContentColor by animateColorAsState(
        targetValue = when {
            isBrake -> ForwardBtnDisabledText
            isAdvancing -> OemOptimalGreen
            isReversing -> OemTextDim
            else -> ForwardBtnText
        },
        animationSpec = tween(200),
        label = "testForwardContent"
    )

    val reverseBg by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber.copy(alpha = 0.22f)
            isAdvancing -> Color(0xFF1E2229)
            else -> NeutralCharcoalReverse
        },
        animationSpec = tween(200),
        label = "testReverseBg"
    )
    val reverseBorderColor by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber
            isAdvancing -> OemBorderSubtle.copy(alpha = 0.5f)
            else -> OemBorderSubtle
        },
        animationSpec = tween(200),
        label = "testReverseBorder"
    )
    val reverseContentColor by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber
            isAdvancing -> OemTextDim
            else -> Color.White
        },
        animationSpec = tween(200),
        label = "testReverseContent"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemBorderSubtle),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = OemTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "MODO PRUEBA (SIMULACIÓN EN BANCO)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OemTextColdWhite
                    )
                    Text(
                        text = "Simulación local mientras no haya vehículo físico conectado",
                        style = MaterialTheme.typography.labelSmall,
                        color = OemTextSecondary
                    )
                }
            }

            Text(
                text = "Como no hay un vehículo físico conectado, use estos mandos de prueba para mover el auto y verificar el frenado automático a 15 cm:",
                style = MaterialTheme.typography.bodySmall,
                color = OemTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ADELANTE) },
                    enabled = !isBrake,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = forwardBg,
                        contentColor = forwardContentColor,
                        disabledContainerColor = ForwardBtnDisabledBg,
                        disabledContentColor = ForwardBtnDisabledText
                    ),
                    border = BorderStroke(if (isAdvancing) 2.dp else 1.dp, forwardBorderColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = if (isBrake) Icons.Default.Lock else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when {
                            isBrake -> "BLOQUEO"
                            isAdvancing -> "AVANZAR ●"
                            else -> "AVANZAR"
                        },
                        fontWeight = if (isAdvancing) FontWeight.Black else FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_DETENER) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRedStop,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, CrimsonRedStop),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "DETENER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ATRAS) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = reverseBg,
                        contentColor = reverseContentColor
                    ),
                    border = BorderStroke(if (isReversing) 2.dp else 1.dp, reverseBorderColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isReversing) "ATRÁS ●" else "ATRÁS",
                        fontWeight = if (isReversing) FontWeight.Black else FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ============================================================================
// 7. PANEL DE PILOTAJE (EXCLUSIVO PARA ROL B: ESTACIÓN DE CONTROL)
// ============================================================================

@Composable
fun MotorControlPadCard(
    uiState: IoTUiState,
    viewModel: IoTViewModel
) {
    val isForwardBlocked = uiState.distanciaCm <= IoTViewModel.DISTANCIA_SEGURIDAD
    val isAdvancing = uiState.estadoMotor == IoTViewModel.MOTOR_AVANZANDO
    val isReversing = uiState.estadoMotor == IoTViewModel.MOTOR_RETROCEDIENDO

    val padForwardBg by animateColorAsState(
        targetValue = when {
            isForwardBlocked -> ForwardBtnDisabledBg
            isAdvancing -> OemOptimalGreen.copy(alpha = 0.22f)
            isReversing -> Color(0xFF1E2229)
            else -> ForwardBtnBg
        },
        animationSpec = tween(200),
        label = "padForwardBg"
    )
    val padForwardBorderColor by animateColorAsState(
        targetValue = when {
            isForwardBlocked -> ForwardBtnDisabledBorder
            isAdvancing -> OemOptimalGreen
            isReversing -> OemBorderSubtle.copy(alpha = 0.5f)
            else -> ForwardBtnBorder
        },
        animationSpec = tween(200),
        label = "padForwardBorder"
    )
    val padForwardContentColor by animateColorAsState(
        targetValue = when {
            isForwardBlocked -> ForwardBtnDisabledText
            isAdvancing -> OemOptimalGreen
            isReversing -> OemTextDim
            else -> ForwardBtnText
        },
        animationSpec = tween(200),
        label = "padForwardContent"
    )

    val padReverseBg by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber.copy(alpha = 0.22f)
            isAdvancing -> Color(0xFF1E2229)
            else -> NeutralCharcoalReverse
        },
        animationSpec = tween(200),
        label = "padReverseBg"
    )
    val padReverseBorderColor by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber
            isAdvancing -> OemBorderSubtle.copy(alpha = 0.5f)
            else -> OemBorderSubtle
        },
        animationSpec = tween(200),
        label = "padReverseBorder"
    )
    val padReverseContentColor by animateColorAsState(
        targetValue = when {
            isReversing -> OemWarningAmber
            isAdvancing -> OemTextDim
            else -> Color.White
        },
        animationSpec = tween(200),
        label = "padReverseContent"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemBorderSubtle),
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
                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = OemTextSecondary)
                    Text(
                        text = "MANDOS DE CONDUCCIÓN REMOTA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OemTextWhite
                    )
                }

                if (isForwardBlocked) {
                    Text(
                        text = "⚠️ BLOQUEO (<= 15 CM)",
                        style = MaterialTheme.typography.labelSmall,
                        color = OemCriticalRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón ADELANTE (Grande - Iluminado en verde cuando avanza, atenuado cuando retrocede)
            Button(
                onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ADELANTE) },
                enabled = !isForwardBlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = padForwardBg,
                    contentColor = padForwardContentColor,
                    disabledContainerColor = ForwardBtnDisabledBg,
                    disabledContentColor = ForwardBtnDisabledText
                ),
                border = BorderStroke(
                    width = if (isAdvancing) 2.dp else 1.5.dp,
                    color = padForwardBorderColor
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(
                    imageVector = if (isForwardBlocked) Icons.Default.Lock else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when {
                        isForwardBlocked -> "ADELANTE (BLOQUEADO)"
                        isAdvancing -> "ADELANTE (EN MARCHA ●)"
                        else -> "ADELANTE"
                    },
                    fontSize = 16.sp,
                    fontWeight = if (isAdvancing) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_DETENER) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRedStop,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, CrimsonRedStop),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DETENER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.sendMotorCommand(IoTViewModel.CMD_ATRAS) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = padReverseBg,
                        contentColor = padReverseContentColor
                    ),
                    border = BorderStroke(
                        width = if (isReversing) 2.dp else 1.dp,
                        color = padReverseBorderColor
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isReversing) "ATRÁS (EN MARCHA ●)" else "ATRÁS",
                        fontSize = 16.sp,
                        fontWeight = if (isReversing) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// 7. BITÁCORA Y CONSOLA DE TELEMETRÍA (Dispatchers.IO)
// ============================================================================

@Composable
fun TelemetryLogConsoleCard(
    logs: List<TelemetryLogEntry>,
    packetsCount: Long,
    lastLatencyMs: Long,
    onClearLogs: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemBorderSubtle),
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
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = OemTextSecondary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "CONSOLA DE EVENTOS TELEMÉTRICOS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = OemTextColdWhite
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
                        color = OemTextSecondary
                    )
                    IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar bitácora",
                            tint = OemTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Surface(
                color = OemConsoleBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, OemBorderSubtle),
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
                            text = "Sin eventos registrados en la bitácora telemétrica.",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = OemTextDim
                        )
                    } else {
                        logs.take(8).forEach { entry ->
                            Text(
                                text = "[${entry.timestamp}] [${entry.tag}] ${entry.message}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (entry.isWarning) OemCriticalRed else OemTextConsole
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 8. BANNERS Y DIÁLOGOS DE PERMISOS Y FIRMWARE
// ============================================================================

@Composable
fun PermissionWarningBanner(
    missingPermissions: List<String>,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowDetails: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OemCardSurface),
        border = BorderStroke(1.dp, OemWarningAmber),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = OemWarningAmber)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aviso de Permisos en Runtime (ISO/IEC 27400)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = OemWarningAmber
                    )
                    Text(
                        text = "Se rechazaron ${missingPermissions.size} permisos de red/cercanía. La app continúa operando por IP sin interrupciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OemTextSecondary
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
                        Text("¿Por qué?", color = OemWarningAmber, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configuración", color = OemTextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = OemSurfaceElevated, contentColor = Color.White),
                    border = BorderStroke(1.dp, OemBorderProminent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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
        containerColor = OemCardSurface,
        icon = {
            Icon(Icons.Default.Shield, contentDescription = null, tint = OemWarningAmber, modifier = Modifier.size(36.dp))
        },
        title = {
            Text(
                text = "Seguridad IoT y Permisos (ISO/IEC 27400)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OemTextWhite,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Para auditar y validar de forma segura el entorno Wi-Fi/Bluetooth del auto en sala, Android requiere:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OemTextSecondary
                )
                missingPermissions.forEach { perm ->
                    val shortName = perm.substringAfterLast('.')
                    Text(
                        text = "• $shortName",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = OemTextColdWhite
                    )
                }
                Text(
                    text = "Resiliencia ante denegación: Puede seguir operando conectándose por IP manual sin bloqueos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OemOptimalGreen
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRetryRequest,
                colors = ButtonDefaults.buttonColors(containerColor = OemSurfaceElevated, contentColor = Color.White),
                border = BorderStroke(1.dp, OemBorderProminent)
            ) {
                Text("Solicitar Nuevamente", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) {
                    Text("Ajustes", color = OemTextSecondary)
                }
                TextButton(onClick = onDismiss) {
                    Text("Continuar", color = OemTextSecondary)
                }
            }
        }
    )
}

@Composable
fun Esp32FirmwareDialog(onDismiss: () -> Unit) {
    val snippet = remember {
        """
        // Endpoint JSON /status para Auto_IoT_Control
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
        containerColor = OemCardSurface,
        icon = {
            Icon(Icons.Default.Code, contentDescription = null, tint = OemTextColdWhite)
        },
        title = {
            Text(
                text = "Firmware ESP32 // Endpoint /status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OemTextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Código incluido en esp32_firmware/Auto_IoT_Control_ES2.ino con Cockpit Web moderno:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OemTextSecondary
                )
                Surface(
                    color = OemConsoleBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, OemBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = OemTextConsole,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OemSurfaceElevated, contentColor = Color.White),
                border = BorderStroke(1.dp, OemBorderProminent)
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        }
    )
}
