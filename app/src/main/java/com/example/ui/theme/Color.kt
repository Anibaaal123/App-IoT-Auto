package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETA AUTOMOTRIZ PROFESIONAL // TABLERO DE DIAGNÓSTICO OEM (BMW / OBD2)
// Cero cian, cero turquesa, cero bordes neón ni efectos cyberpunk
// ============================================================================

// 1. Fondo General (Canvas) y Superficies de Contenedores
val OemCanvasBackground = Color(0xFF191C21)       // Gris antracita / pizarra muy oscuro mate (#191C21)
val OemCardSurface = Color(0xFF262B33)            // Contenedores y Tarjetas: Gris grafito (#262B33)
val OemSurfaceElevated = Color(0xFF2F3642)        // Superficie gris grafito destacada para acciones principales (#2F3642)
val OemBorderSubtle = Color(0xFF313742)           // Separadores y bordes sutiles en gris oscuro (#313742)
val OemBorderProminent = Color(0xFF3E4656)        // Bordes de componentes destacados
val OemConsoleBg = Color(0xFF14161B)              // Consola de telemetría: Fondo (#14161B)

// 2. Sistema de Color Funcional (Semáforo de Diagnóstico Automotriz)
val OemCriticalRed = Color(0xFFE02E2E)            // Crítico / Peligro / Detener: Rojo escarlata sobrio (#E02E2E / #EF4444)
val OemWarningAmber = Color(0xFFF59E0B)           // Precaución / Atención / Alertas intermedias: Ámbar automotriz (#F59E0B)
val OemOptimalGreen = Color(0xFF10B981)           // Vía libre / Conectado / Estado óptimo: Verde esmeralda suave (#10B981)

// 3. Tipografía
val OemTextWhite = Color(0xFFFFFFFF)              // Títulos principales, estados clave y sensor: Blanco puro (#FFFFFF)
val OemTextColdWhite = Color(0xFFF3F4F6)          // Blanco frío (#F3F4F6)
val OemTextSecondary = Color(0xFF8C95A6)          // Subtítulos, etiquetas y textos secundarios: Gris medio/ceniza (#8C95A6)
val OemTextConsole = Color(0xFF9CA3AF)            // Tipografía monoespaciada de consola: Gris suave (#9CA3AF)
val OemTextDim = Color(0xFF6B7280)                // Textos atenuados / placeholders

// 4. Botones y Mandos de Dirección
val ForwardBtnBg = Color(0xFF262B33)              // AVANZAR: Fondo oscuro neutro (#262B33)
val ForwardBtnBorder = Color(0xFF313742)          // AVANZAR: Borde técnico sutil (#313742)
val ForwardBtnText = Color(0xFFF3F4F6)            // AVANZAR: Texto claro e íconos legibles
val ForwardBtnDisabledBg = Color(0xFF1E2229)      // AVANZAR bloqueado: Fondo pizarra tenue
val ForwardBtnDisabledBorder = Color(0xFF262B33)  // AVANZAR bloqueado: Borde
val ForwardBtnDisabledText = Color(0xFF6B7280)    // AVANZAR bloqueado: Texto

val CrimsonRedStop = OemCriticalRed               // DETENER: Rojo escarlata sobrio (#E02E2E)
val NeutralCharcoalReverse = Color(0xFF262B33)    // ATRÁS: Fondo oscuro neutro (#262B33)

// 5. Sensor de Proximidad y Alertas
val DangerRed = OemCriticalRed                    // Alerta obstáculo crítico <= 15 cm: Rojo (#E02E2E)
val SafePink = OemTextWhite                       // Distancia normal: Blanco limpio (#FFFFFF)

// 6. Textos
val PureWhiteText = OemTextWhite
val BoneWhiteText = OemTextColdWhite
val LightGrayTerminalText = OemTextConsole
val MutedGraphiteText = OemTextSecondary
val DimGraphiteText = OemTextDim

// 7. Señales Lógicas de Pines GPIO
val GpioHighActive = OemOptimalGreen              // Pin activo: Verde esmeralda (#10B981)
val GpioLowInactive = OemBorderSubtle             // Pin inactivo: Gris oscuro (#313742)

// ============================================================================
// MAPEO Y COMPATIBILIDAD CON VISTAS EXISTENTES (REEMPLAZO TOTAL DE TONOS ANTIGUOS)
// ============================================================================
val TacticalDarkBackground = OemCanvasBackground
val TacticalSurfaceCard = OemCardSurface
val TacticalSurfaceElevated = OemSurfaceElevated
val TacticalBorderGlow = OemBorderSubtle

// Eliminación de cualquier residuo de rosa neón, cian, turquesa o azul brillante:
val CyberCyan = OemTextColdWhite
val CyberCyanBright = OemOptimalGreen
val CyberEmerald = OemOptimalGreen
val CyberRedAlert = OemCriticalRed
val CyberAmber = OemWarningAmber
val CyberCobalt = OemSurfaceElevated
val CyberPinkNeon = OemSurfaceElevated
val CyberPinkLight = OemWarningAmber

val TextCockpitWhite = OemTextColdWhite
val TextCockpitMuted = OemTextSecondary
val TextCockpitDim = OemTextDim

// Aliases
val OledPureBlack = OemCanvasBackground
val OledDarkGraphiteCard = OemCardSurface
val OledElevatedSurface = OemSurfaceElevated
val OledBorder = OemBorderSubtle
val OledTerminalBg = OemConsoleBg

val IoTBackgroundDark = OemCanvasBackground
val IoTSurfaceCard = OemCardSurface
val IoTSurfaceElevated = OemSurfaceElevated
val IoTCyanAccent = OemOptimalGreen
val IoTCyanBright = OemOptimalGreen
val IoTGreenForward = OemOptimalGreen
val IoTRedStop = OemCriticalRed
val IoTBlueReverse = NeutralCharcoalReverse
val IoTAmberWarning = OemWarningAmber
val IoTTextPrimary = OemTextWhite
val IoTTextSecondary = OemTextSecondary
