package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subtitle: String,
    val licensePlate: String,
    val model3dId: String,
    val paintColorLong: Long,
    val paintFinishId: String,
    val glassColorLong: Long,
    val isLiveTracking: Boolean,
    val esp32EndpointUrl: String,
    val deviceSimId: String,
    val hudSlot1Key: String,
    val hudSlot2Key: String,
    val hudSlot3Key: String
)

@Entity(tableName = "ecu_tune_profiles")
data class EcuTuneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val profileName: String,
    val authorTag: String,
    val fuelTrimPct: Float,
    val ignitionOffsetDeg: Float,
    val throttleResponsePct: Float,
    val revLimiterRpm: Int,
    val vtecEngageRpm: Int,
    val fanTriggerTempC: Int,
    val launchControlRpm: Int,
    val popAndBangEnabled: Boolean,
    val isActive: Boolean,
    val notes: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_records")
data class TripRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val dayNumber: Int,
    val dayLabel: String,
    val fullDateThai: String,
    val startLocation: String,
    val endLocation: String,
    val startTime: String,
    val endTime: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val maxSpeedKmh: Double,
    val avgSpeedKmh: Double,
    val segmentLabel: String
)

@Entity(tableName = "iot_nodes")
data class IoTNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val hardwareModel: String,
    val category: String, // "VEHICLE" or "POWER"
    val isOnline: Boolean,
    val socOrBatteryPct: Int,
    val voltage: Double,
    val powerKw: Double,
    val temperatureC: Int,
    val endpointUrl: String
)
