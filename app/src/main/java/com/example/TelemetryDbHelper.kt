package com.example

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TelemetryDbRecord(
    val id: Long,
    val timestamp: Long,
    val formattedTime: String,
    val distanceCm: Int,
    val motorState: String,
    val isAlert: Boolean,
    val isBrake: Boolean,
    val operator: String
)

/**
 * Base de datos local SQLite nativa de Android para persistencia de telemetría IoT
 * y credenciales según los requerimientos de la Evaluación Sumativa.
 */
class TelemetryDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "auto_iot_telemetry.db"
        const val DATABASE_VERSION = 1

        // Tabla de lecturas de sensores
        const val TABLE_TELEMETRY = "telemetry_records"
        const val COL_ID = "id"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_FORMATTED_TIME = "formatted_time"
        const val COL_DISTANCE = "distance_cm"
        const val COL_MOTOR = "motor_state"
        const val COL_ALERT = "is_alert"
        const val COL_BRAKE = "is_brake"
        const val COL_OPERATOR = "operator"

        // Tabla de operadores registrados
        const val TABLE_OPERATORS = "registered_operators"
        const val COL_OP_ID = "id"
        const val COL_OP_USERNAME = "username"
        const val COL_OP_PASSWORD_HASH = "password_sha256"
        const val COL_OP_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTelemetryTable = """
            CREATE TABLE $TABLE_TELEMETRY (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_FORMATTED_TIME TEXT NOT NULL,
                $COL_DISTANCE INTEGER NOT NULL,
                $COL_MOTOR TEXT NOT NULL,
                $COL_ALERT INTEGER NOT NULL,
                $COL_BRAKE INTEGER NOT NULL,
                $COL_OPERATOR TEXT NOT NULL
            )
        """.trimIndent()

        val createOperatorsTable = """
            CREATE TABLE $TABLE_OPERATORS (
                $COL_OP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_OP_USERNAME TEXT UNIQUE NOT NULL,
                $COL_OP_PASSWORD_HASH TEXT NOT NULL,
                $COL_OP_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTelemetryTable)
        db.execSQL(createOperatorsTable)

        // Insertar operador administrador por defecto
        val initialValues = ContentValues().apply {
            put(COL_OP_USERNAME, "admin")
            put(COL_OP_PASSWORD_HASH, SecurityManager.PRECALCULATED_LAB_HASH)
            put(COL_OP_CREATED_AT, System.currentTimeMillis())
        }
        db.insert(TABLE_OPERATORS, null, initialValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TELEMETRY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_OPERATORS")
        onCreate(db)
    }

    /**
     * Inserta una lectura telemétrica capturada por los sensores en la base de datos SQLite local.
     */
    fun insertTelemetryReading(
        distanceCm: Int,
        motorState: String,
        isAlert: Boolean,
        isBrake: Boolean,
        operator: String
    ): Long {
        val now = System.currentTimeMillis()
        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))
        val values = ContentValues().apply {
            put(COL_TIMESTAMP, now)
            put(COL_FORMATTED_TIME, timeString)
            put(COL_DISTANCE, distanceCm)
            put(COL_MOTOR, motorState)
            put(COL_ALERT, if (isAlert) 1 else 0)
            put(COL_BRAKE, if (isBrake) 1 else 0)
            put(COL_OPERATOR, operator)
        }

        return try {
            writableDatabase.insert(TABLE_TELEMETRY, null, values)
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Obtiene el conteo total de lecturas guardadas en la base de datos SQLite.
     */
    fun getTelemetryCount(): Long {
        return try {
            val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_TELEMETRY", null)
            var count = 0L
            if (cursor.moveToFirst()) {
                count = cursor.getLong(0)
            }
            cursor.close()
            count
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Consulta las últimas lecturas almacenadas en la base de datos SQLite.
     */
    fun getRecentTelemetry(limit: Int = 20): List<TelemetryDbRecord> {
        val list = mutableListOf<TelemetryDbRecord>()
        return try {
            val cursor = readableDatabase.query(
                TABLE_TELEMETRY,
                null,
                null,
                null,
                null,
                null,
                "$COL_ID DESC",
                limit.toString()
            )
            while (cursor.moveToNext()) {
                list.add(
                    TelemetryDbRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                        formattedTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_FORMATTED_TIME)),
                        distanceCm = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
                        motorState = cursor.getString(cursor.getColumnIndexOrThrow(COL_MOTOR)),
                        isAlert = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALERT)) == 1,
                        isBrake = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BRAKE)) == 1,
                        operator = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPERATOR))
                    )
                )
            }
            cursor.close()
            list
        } catch (e: Exception) {
            list
        }
    }

    /**
     * Limpia el historial telemétrico de SQLite.
     */
    fun clearTelemetryHistory(): Int {
        return try {
            writableDatabase.delete(TABLE_TELEMETRY, null, null)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Registra o actualiza un operador en la base de datos SQLite.
     */
    fun registerOperatorInDb(username: String, hashSha256: String): Boolean {
        return try {
            val values = ContentValues().apply {
                put(COL_OP_USERNAME, username)
                put(COL_OP_PASSWORD_HASH, hashSha256)
                put(COL_OP_CREATED_AT, System.currentTimeMillis())
            }
            val id = writableDatabase.insertWithOnConflict(
                TABLE_OPERATORS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            id != -1L
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida credenciales contra la tabla de operadores en SQLite.
     */
    fun validateOperatorInDb(username: String, hashSha256: String): Boolean {
        return try {
            val cursor = readableDatabase.query(
                TABLE_OPERATORS,
                arrayOf(COL_OP_PASSWORD_HASH),
                "$COL_OP_USERNAME = ?",
                arrayOf(username),
                null,
                null,
                null
            )
            var matches = false
            if (cursor.moveToFirst()) {
                val storedHash = cursor.getString(0)
                matches = storedHash.equals(hashSha256, ignoreCase = true)
            }
            cursor.close()
            matches
        } catch (e: Exception) {
            false
        }
    }
}
