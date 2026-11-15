package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.A2ADatabase
import com.example.data.local.EcuTuneEntity
import com.example.data.local.IoTNodeEntity
import com.example.data.local.TripRecordEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.CarPaintFinish
import com.example.data.model.LiveTelemetry
import com.example.data.model.ObdPidMetric
import com.example.data.model.VehicleModel3D
import com.example.data.repository.A2ARepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class A2AUiState(
    val vehicle: VehicleEntity? = null,
    val telemetry: LiveTelemetry = LiveTelemetry(),
    val tuneProfiles: List<EcuTuneEntity> = emptyList(),
    val tripRecords: List<TripRecordEntity> = emptyList(),
    val iotNodes: List<IoTNodeEntity> = emptyList(),
    val selectedTripDay: Int = 15,
    val simulatedThrottlePct: Float = 0f,
    val isDrivingSimulation: Boolean = false,
    val statusBannerMessage: String? = null,
    val hardwareError: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class A2AViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: A2ARepository

    private val _telemetry = MutableStateFlow(LiveTelemetry())
    private val _selectedTripDay = MutableStateFlow(15)
    private val _simulatedThrottle = MutableStateFlow(0f)
    private val _isDrivingSim = MutableStateFlow(false)
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _hardwareError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<A2AUiState>

    init {
        val dao = A2ADatabase.getInstance(application).a2aDao()
        repository = A2ARepository(dao)

        viewModelScope.launch {
            repository.ensureSeeded()
        }

        val vehiclesFlow = repository.vehicles
        val activeVehicleFlow = vehiclesFlow.flatMapLatest { list ->
            flowOf(list.firstOrNull())
        }
        val tunesFlow = activeVehicleFlow.flatMapLatest { vehicle ->
            if (vehicle != null) repository.observeTuneProfiles(vehicle.id) else flowOf(emptyList())
        }
        val tripsFlow = activeVehicleFlow.flatMapLatest { vehicle ->
            if (vehicle != null) repository.observeTripRecords(vehicle.id) else flowOf(emptyList())
        }

        uiState = combine(
            activeVehicleFlow,
            _telemetry,
            tunesFlow,
            tripsFlow,
            repository.iotNodes,
            _selectedTripDay,
            _simulatedThrottle,
            _isDrivingSim,
            _statusBanner,
            _hardwareError
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            A2AUiState(
                vehicle = values[0] as VehicleEntity?,
                telemetry = values[1] as LiveTelemetry,
                tuneProfiles = values[2] as List<EcuTuneEntity>,
                tripRecords = values[3] as List<TripRecordEntity>,
                iotNodes = values[4] as List<IoTNodeEntity>,
                selectedTripDay = values[5] as Int,
                simulatedThrottlePct = values[6] as Float,
                isDrivingSimulation = values[7] as Boolean,
                statusBannerMessage = values[8] as String?,
                hardwareError = values[9] as String?
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = A2AUiState()
        )

        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm:ss 'น.'", Locale.getDefault())
            var tick = 0
            while (true) {
                delay(1000)
                tick++
                val currentVehicle = uiState.value.vehicle
                val endpoint = currentVehicle?.esp32EndpointUrl?.trim().orEmpty()
                val activeTune = uiState.value.tuneProfiles.firstOrNull { it.isActive }
                val timeStr = timeFormat.format(Date())

                if (endpoint.isNotBlank()) {
                    val result = repository.pollLiveHardware(endpoint, _telemetry.value)
                    result.onSuccess { live ->
                        _hardwareError.value = null
                        _telemetry.value = live.copy(
                            updatedTimeText = timeStr,
                            simModuleInfo = "4G LTE ออนไลน์ • ${currentVehicle?.deviceSimId ?: "Sim:03 A7670E"}"
                        )
                    }.onFailure { err ->
                        _hardwareError.value = "เชื่อมต่อ ESP32 ไม่สำเร็จ (${err.localizedMessage ?: "Timeout"}) • ใช้ค่าจำลองชั่วคราว"
                        updateSimulatedTelemetry(tick, timeStr, activeTune)
                    }
                } else {
                    _hardwareError.value = null
                    updateSimulatedTelemetry(tick, timeStr, activeTune)
                }
            }
        }
    }

    private fun updateSimulatedTelemetry(
        tick: Int,
        timeStr: String,
        activeTune: EcuTuneEntity?
    ) {
        val driving = _isDrivingSim.value
        val manualThrottle = _simulatedThrottle.value
        val tuneThrottleBoost = (activeTune?.throttleResponsePct ?: 100f) / 100f
        val tuneTimingOffset = activeTune?.ignitionOffsetDeg ?: 0f
        val tuneFuelTrim = activeTune?.fuelTrimPct ?: 0f
        val revLimit = activeTune?.revLimiterRpm ?: 7200

        val current = _telemetry.value
        val targetThrottle = if (driving) {
            (18.0 + 14.0 * sin(tick * 0.25) + manualThrottle * 0.4).coerceIn(5.0, 95.0)
        } else {
            (2.5 + manualThrottle * 0.85).coerceIn(0.0, 95.0)
        }

        val targetSpeed = if (driving) {
            (72.0 + 22.0 * sin(tick * 0.18) + manualThrottle * 0.6).coerceIn(15.0, 145.0)
        } else {
            0.0
        }

        val baseRpm = if (driving) {
            (1650 + (targetSpeed * 18) + (targetThrottle * 25 * tuneThrottleBoost)).roundToInt()
        } else {
            (820 + (manualThrottle * 48 * tuneThrottleBoost) + Random.nextInt(-18, 22)).roundToInt()
        }.coerceIn(760, revLimit)

        val newLat = if (driving) {
            current.latitude + 0.00018 * cos(Math.toRadians(current.headingDeg.toDouble()))
        } else current.latitude

        val newLng = if (driving) {
            current.longitude + 0.00018 * sin(Math.toRadians(current.headingDeg.toDouble()))
        } else current.longitude

        val newHeading = if (driving) {
            (current.headingDeg + (4 * sin(tick * 0.15)).roundToInt() + 360) % 360
        } else 7

        val compassDir = when {
            newHeading < 23 || newHeading >= 338 -> "N (เหนือ)"
            newHeading < 68 -> "NE (ตะวันออกเฉียงเหนือ)"
            newHeading < 113 -> "E (ตะวันออก)"
            newHeading < 158 -> "SE (ตะวันออกเฉียงใต้)"
            newHeading < 203 -> "S (ใต้)"
            newHeading < 248 -> "SW (ตะวันตกเฉียงใต้)"
            newHeading < 293 -> "W (ตะวันตก)"
            else -> "NW (ตะวันตกเฉียงเหนือ)"
        }

        val trail = if (driving && tick % 2 == 0) {
            (current.routeTrail + (newLat to newLng)).takeLast(40)
        } else current.routeTrail

        val mapVal = (34 + (targetThrottle * 0.72)).roundToInt().coerceIn(28, 105)
        val loadVal = (16.5 + targetThrottle * 0.75).coerceIn(14.0, 98.0)
        val mafVal = (3.4 + (baseRpm / 1000.0) * 3.8 * (targetThrottle / 30.0 + 0.5)).coerceIn(2.8, 130.0)
        val timingVal = (14.5 + (baseRpm / 400.0) + tuneTimingOffset).coerceIn(5.0, 42.0)

        _telemetry.value = current.copy(
            latitude = newLat,
            longitude = newLng,
            headingDeg = newHeading,
            headingLabel = "$newHeading° - $compassDir",
            gpsSpeedKmh = (targetSpeed * 10).roundToInt() / 10.0,
            obdSpeedKmh = targetSpeed.roundToInt(),
            rpm = baseRpm,
            engineLoadPct = (loadVal * 10).roundToInt() / 10.0,
            coolantTempC = if (driving) 84 else 58 + (tick % 3),
            ecuVoltage = if (baseRpm > 1000) 14.4 else 14.3,
            throttlePct = (targetThrottle * 10).roundToInt() / 10.0,
            mapKpa = mapVal,
            mafGps = (mafVal * 10).roundToInt() / 10.0,
            timingAdvDeg = (timingVal * 10).roundToInt() / 10.0,
            stftPct = ((tuneFuelTrim * 0.25) + Random.nextDouble(-0.8, 0.8)).let { (it * 10).roundToInt() / 10.0 },
            distanceTodayKm = if (driving) ((current.distanceTodayKm + targetSpeed / 3600.0) * 10).roundToInt() / 10.0 else current.distanceTodayKm,
            updatedTimeText = timeStr,
            isLiveFromHardware = false,
            isDrivingSimulated = driving,
            routeTrail = trail
        )
    }

    fun selectModel3D(model: VehicleModel3D) {
        val current = uiState.value.vehicle ?: return
        viewModelScope.launch {
            repository.updateVehicle(current.copy(model3dId = model.id))
            showBanner("เปลี่ยนโมเดล 3D เป็น ${model.displayName} แล้ว")
        }
    }

    fun updateCarStudioStyle(paintColorLong: Long, finish: CarPaintFinish, glassColorLong: Long) {
        val current = uiState.value.vehicle ?: return
        viewModelScope.launch {
            repository.updateVehicle(
                current.copy(
                    paintColorLong = paintColorLong,
                    paintFinishId = finish.id,
                    glassColorLong = glassColorLong
                )
            )
        }
    }

    fun toggleLiveTracking(enabled: Boolean) {
        val current = uiState.value.vehicle ?: return
        viewModelScope.launch {
            repository.updateVehicle(current.copy(isLiveTracking = enabled))
        }
    }

    fun updateHudMetric(slotIndex: Int, metric: ObdPidMetric) {
        val current = uiState.value.vehicle ?: return
        viewModelScope.launch {
            val updated = when (slotIndex) {
                1 -> current.copy(hudSlot1Key = metric.key)
                2 -> current.copy(hudSlot2Key = metric.key)
                else -> current.copy(hudSlot3Key = metric.key)
            }
            repository.updateVehicle(updated)
            showBanner("อัปเดต HUD ช่องที่ $slotIndex เป็น ${metric.thaiTitle}")
        }
    }

    fun setDrivingSimulation(enabled: Boolean) {
        _isDrivingSim.value = enabled
        if (!enabled) {
            _simulatedThrottle.value = 0f
        }
    }

    fun setSimulatedThrottle(pct: Float) {
        _simulatedThrottle.value = pct.coerceIn(0f, 100f)
    }

    fun selectTripDay(dayNumber: Int) {
        _selectedTripDay.value = dayNumber
    }

    fun activateTuneProfile(profile: EcuTuneEntity) {
        val vehicle = uiState.value.vehicle ?: return
        viewModelScope.launch {
            repository.activateTuneProfile(vehicle.id, profile.id)
            showBanner("เปิดใช้งานแมพจูน: ${profile.profileName}")
        }
    }

    fun saveAndFlashTune(profile: EcuTuneEntity) {
        val vehicle = uiState.value.vehicle ?: return
        viewModelScope.launch {
            val result = repository.saveTuneProfile(
                profile = profile.copy(vehicleId = vehicle.id, isActive = true),
                endpointUrl = vehicle.esp32EndpointUrl
            )
            result.onSuccess {
                if (vehicle.esp32EndpointUrl.isNotBlank()) {
                    showBanner("แฟลชแมพ ${profile.profileName} ไปยังกล่อง ECU ผ่าน ESP32 สำเร็จ!")
                } else {
                    showBanner("บันทึกแมพ ${profile.profileName} ลงหน่วยความจำสำเร็จ (โหมดออฟไลน์)")
                }
            }.onFailure {
                showBanner("บันทึกแมพในเครื่องแล้ว (ไม่พบ ESP32 ปลายทาง)")
            }
        }
    }

    fun scanAndClearDtc(clearCodes: Boolean) {
        if (clearCodes) {
            _telemetry.update { it.copy(dtcCount = 0, dtcCodes = emptyList()) }
            showBanner("ล้างรหัสข้อผิดพลาด ECU (DTC Clear) เรียบร้อย: 0 count")
        } else {
            showBanner("สแกน ECU OBD2 เสร็จสิ้น: ไม่พบรหัสผิดปกติ (0 DTC)")
        }
    }

    fun addHardwareDevice(
        title: String,
        hardwareModel: String,
        category: String,
        endpointUrl: String,
        vehiclePlate: String? = null
    ) {
        viewModelScope.launch {
            repository.addIoTNode(
                IoTNodeEntity(
                    title = title,
                    hardwareModel = hardwareModel,
                    category = category,
                    isOnline = true,
                    socOrBatteryPct = if (category == "POWER") 85 else 100,
                    voltage = if (category == "POWER") 52.8 else 14.4,
                    powerKw = 0.0,
                    temperatureC = 28,
                    endpointUrl = endpointUrl
                )
            )
            if (category == "VEHICLE" && endpointUrl.isNotBlank()) {
                uiState.value.vehicle?.let { v ->
                    repository.updateVehicle(
                        v.copy(
                            esp32EndpointUrl = endpointUrl,
                            licensePlate = vehiclePlate?.takeIf { it.isNotBlank() } ?: v.licensePlate
                        )
                    )
                }
            }
            showBanner("เพิ่มอุปกรณ์ $title เรียบร้อยแล้ว")
        }
    }

    fun updateEsp32Endpoint(url: String) {
        val current = uiState.value.vehicle ?: return
        viewModelScope.launch {
            repository.updateVehicle(current.copy(esp32EndpointUrl = url.trim()))
            showBanner(
                if (url.isBlank()) "สลับเป็นโหมดจำลองสัญญาณ ESP32 / OBD2"
                else "ตั้งค่าปลายทาง ESP32: $url"
            )
        }
    }

    fun deleteIoTNode(nodeId: Int) {
        viewModelScope.launch {
            repository.deleteIoTNode(nodeId)
        }
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    private fun showBanner(msg: String) {
        _statusBanner.value = msg
    }
}
