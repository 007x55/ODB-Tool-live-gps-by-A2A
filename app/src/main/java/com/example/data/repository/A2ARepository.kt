package com.example.data.repository

import com.example.data.local.A2ADao
import com.example.data.local.EcuTuneEntity
import com.example.data.local.IoTNodeEntity
import com.example.data.local.TripRecordEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.CarPaintFinish
import com.example.data.model.LiveTelemetry
import com.example.data.model.ObdPidMetric
import com.example.data.model.VehicleModel3D
import com.example.data.remote.EcuFlashPayload
import com.example.data.remote.Esp32TelemetryClient
import kotlinx.coroutines.flow.Flow

class A2ARepository(private val dao: A2ADao) {

    val vehicles: Flow<List<VehicleEntity>> = dao.observeVehicles()
    val iotNodes: Flow<List<IoTNodeEntity>> = dao.observeIoTNodes()

    fun observeTuneProfiles(vehicleId: Int): Flow<List<EcuTuneEntity>> =
        dao.observeTuneProfiles(vehicleId)

    fun observeTripRecords(vehicleId: Int): Flow<List<TripRecordEntity>> =
        dao.observeTripRecords(vehicleId)

    suspend fun ensureSeeded() {
        if (dao.getVehicleCount() > 0) return

        val vehicleId = dao.insertVehicle(
            VehicleEntity(
                name = "Honda Civic FB 1.8",
                subtitle = "รถยนต์ส่วนตัว (EV / Sedan Profile)",
                licensePlate = "1กฮ3069",
                model3dId = VehicleModel3D.CIVIC_FB.id,
                paintColorLong = 0xFF1E293B,
                paintFinishId = CarPaintFinish.GLOSS.id,
                glassColorLong = 0xFF0F172A,
                isLiveTracking = true,
                esp32EndpointUrl = "",
                deviceSimId = "Sim:03 A7670E",
                hudSlot1Key = ObdPidMetric.RPM.key,
                hudSlot2Key = ObdPidMetric.FUEL_LEVEL.key,
                hudSlot3Key = ObdPidMetric.THROTTLE.key
            )
        ).toInt()

        // Seed aekboy2010 ECU tuning presets
        dao.insertTuneProfile(
            EcuTuneEntity(
                vehicleId = vehicleId,
                profileName = "aekboy2010 Stage 1 (95 / E20)",
                authorTag = "aekboy2010",
                fuelTrimPct = 4.5f,
                ignitionOffsetDeg = 3.0f,
                throttleResponsePct = 115f,
                revLimiterRpm = 7200,
                vtecEngageRpm = 4800,
                fanTriggerTempC = 88,
                launchControlRpm = 3800,
                popAndBangEnabled = false,
                isActive = true,
                notes = "จูนเก็บรายละเอียดรอบกลาง-ปลาย ตอบสนองคันเร่งไวขึ้น ประหยัดน้ำมัน"
            )
        )
        dao.insertTuneProfile(
            EcuTuneEntity(
                vehicleId = vehicleId,
                profileName = "aekboy2010 Track E85 Race",
                authorTag = "aekboy2010",
                fuelTrimPct = 16.0f,
                ignitionOffsetDeg = 6.5f,
                throttleResponsePct = 135f,
                revLimiterRpm = 7800,
                vtecEngageRpm = 4400,
                fanTriggerTempC = 83,
                launchControlRpm = 4500,
                popAndBangEnabled = true,
                isActive = false,
                notes = "แมพซิ่ง E85 เต็มระบบ เปิดรอบ 7,800 rpm พร้อม Launch Control & Pop/Bang"
            )
        )
        dao.insertTuneProfile(
            EcuTuneEntity(
                vehicleId = vehicleId,
                profileName = "Street Eco 91 (เดินทางไกล)",
                authorTag = "aekboy2010",
                fuelTrimPct = -2.5f,
                ignitionOffsetDeg = 1.0f,
                throttleResponsePct = 95f,
                revLimiterRpm = 6800,
                vtecEngageRpm = 5300,
                fanTriggerTempC = 92,
                launchControlRpm = 3200,
                popAndBangEnabled = false,
                isActive = false,
                notes = "เน้นความนุ่มนวลและประหยัดเชื้อเพลิงสำหรับขับทางไกล"
            )
        )

        // Seed 5 days of trip history
        dao.insertTripRecords(
            listOf(
                TripRecordEntity(
                    vehicleId = vehicleId,
                    dayNumber = 15,
                    dayLabel = "อาทิตย์ 15",
                    fullDateThai = "วันอาทิตย์ที่ 15 พฤศจิกายน 2569",
                    startLocation = "บ้านเดิม (ตาก)",
                    endLocation = "บ้านตาก / อ.เมืองตาก",
                    startTime = "07:10 น.",
                    endTime = "08:52 น.",
                    distanceKm = 117.5,
                    durationMinutes = 102,
                    maxSpeedKmh = 118.4,
                    avgSpeedKmh = 69.1,
                    segmentLabel = "จุด A บ้านเดิม -> จุด B เมืองตาก (ผ่าน ถ.พหลโยธิน)"
                ),
                TripRecordEntity(
                    vehicleId = vehicleId,
                    dayNumber = 14,
                    dayLabel = "เสาร์ 14",
                    fullDateThai = "วันเสาร์ที่ 14 พฤศจิกายน 2569",
                    startLocation = "บ้านเดิม (ตาก)",
                    endLocation = "เขื่อนภูมิพล",
                    startTime = "09:15 น.",
                    endTime = "10:08 น.",
                    distanceKm = 46.8,
                    durationMinutes = 53,
                    maxSpeedKmh = 104.0,
                    avgSpeedKmh = 53.0,
                    segmentLabel = "จุด A บ้านเดิม -> จุด B สันเขื่อนภูมิพล"
                ),
                TripRecordEntity(
                    vehicleId = vehicleId,
                    dayNumber = 13,
                    dayLabel = "ศุกร์ 13",
                    fullDateThai = "วันศุกร์ที่ 13 พฤศจิกายน 2569",
                    startLocation = "อ.เมืองตาก",
                    endLocation = "บ้านเดิม (ตาก)",
                    startTime = "17:25 น.",
                    endTime = "18:05 น.",
                    distanceKm = 28.4,
                    durationMinutes = 40,
                    maxSpeedKmh = 96.2,
                    avgSpeedKmh = 42.6,
                    segmentLabel = "จุด A ตัวเมือง -> จุด B บ้านเดิม"
                ),
                TripRecordEntity(
                    vehicleId = vehicleId,
                    dayNumber = 12,
                    dayLabel = "พฤหัส 12",
                    fullDateThai = "วันพฤหัสบดีที่ 12 พฤศจิกายน 2569",
                    startLocation = "บ้านเดิม (ตาก)",
                    endLocation = "อ.แม่สอด",
                    startTime = "08:00 น.",
                    endTime = "09:30 น.",
                    distanceKm = 84.2,
                    durationMinutes = 90,
                    maxSpeedKmh = 112.0,
                    avgSpeedKmh = 56.1,
                    segmentLabel = "จุด A บ้านเดิม -> จุด B อ.แม่สอด (ทางหลวง 12)"
                ),
                TripRecordEntity(
                    vehicleId = vehicleId,
                    dayNumber = 11,
                    dayLabel = "พุธ 11",
                    fullDateThai = "วันพุธที่ 11 พฤศจิกายน 2569",
                    startLocation = "บ้านเดิม (ตาก)",
                    endLocation = "ศูนย์จูน aekboy2010",
                    startTime = "13:10 น.",
                    endTime = "13:38 น.",
                    distanceKm = 18.6,
                    durationMinutes = 28,
                    maxSpeedKmh = 125.0,
                    avgSpeedKmh = 48.0,
                    segmentLabel = "จุด A บ้านเดิม -> จุด B ทดสอบจูนกล่อง & ไดโน"
                )
            )
        )

        // Seed auxiliary IoT / BMS / ESP32 nodes matching video
        dao.insertIoTNode(
            IoTNodeEntity(
                title = "1 BMS • LiFePO4 Auxiliary",
                hardwareModel = "ESP32-S3 + JK-BMS RS485",
                category = "POWER",
                isOnline = true,
                socOrBatteryPct = 43,
                voltage = 53.42,
                powerKw = 0.00,
                temperatureC = 23,
                endpointUrl = ""
            )
        )
        dao.insertIoTNode(
            IoTNodeEntity(
                title = "A2A OBD2 CAN Node",
                hardwareModel = "ESP32 + SIM A7670E + MCP2515",
                category = "VEHICLE",
                isOnline = true,
                socOrBatteryPct = 100,
                voltage = 14.40,
                powerKw = 0.42,
                temperatureC = 34,
                endpointUrl = ""
            )
        )
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) = dao.updateVehicle(vehicle)

    suspend fun saveTuneProfile(profile: EcuTuneEntity, endpointUrl: String): Result<Boolean> {
        val insertedId = dao.insertTuneProfile(profile).toInt()
        val finalId = if (profile.id == 0) insertedId else profile.id
        if (profile.isActive) {
            dao.setActiveTuneProfile(profile.vehicleId, finalId)
        }

        if (endpointUrl.isNotBlank()) {
            return try {
                val url = endpointUrl.trimEnd('/') + "/ecu/tune"
                val response = Esp32TelemetryClient.api.sendEcuTune(
                    fullUrl = url,
                    payload = EcuFlashPayload(
                        profileName = profile.profileName,
                        author = profile.authorTag,
                        fuelTrimPct = profile.fuelTrimPct,
                        ignitionOffsetDeg = profile.ignitionOffsetDeg,
                        throttleResponsePct = profile.throttleResponsePct,
                        revLimiterRpm = profile.revLimiterRpm,
                        vtecEngageRpm = profile.vtecEngageRpm,
                        fanTriggerTempC = profile.fanTriggerTempC,
                        launchControlRpm = profile.launchControlRpm,
                        popAndBangEnabled = profile.popAndBangEnabled
                    )
                )
                Result.success(response.isSuccessful)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    suspend fun activateTuneProfile(vehicleId: Int, profileId: Int) {
        dao.setActiveTuneProfile(vehicleId, profileId)
    }

    suspend fun addIoTNode(node: IoTNodeEntity) {
        dao.insertIoTNode(node)
    }

    suspend fun deleteIoTNode(nodeId: Int) {
        dao.deleteIoTNode(nodeId)
    }

    suspend fun pollLiveHardware(endpointUrl: String, current: LiveTelemetry): Result<LiveTelemetry> {
        return try {
            val cleanUrl = if (endpointUrl.startsWith("http")) endpointUrl else "http://$endpointUrl"
            val response = Esp32TelemetryClient.api.fetchTelemetry(cleanUrl)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val newLat = body.lat ?: current.latitude
                val newLng = body.lng ?: current.longitude
                val updatedTrail = (current.routeTrail + (newLat to newLng)).takeLast(40)
                Result.success(
                    current.copy(
                        latitude = newLat,
                        longitude = newLng,
                        headingDeg = body.heading ?: current.headingDeg,
                        altitudeM = body.altitude ?: current.altitudeM,
                        gpsSatellites = body.satellites ?: current.gpsSatellites,
                        gpsSpeedKmh = body.speedGps ?: current.gpsSpeedKmh,
                        obdSpeedKmh = body.speedObd ?: current.obdSpeedKmh,
                        rpm = body.rpm ?: current.rpm,
                        engineLoadPct = body.engineLoad ?: current.engineLoadPct,
                        fuelLevelPct = body.fuelLevel ?: current.fuelLevelPct,
                        coolantTempC = body.coolantTemp ?: current.coolantTempC,
                        ecuVoltage = body.voltage ?: current.ecuVoltage,
                        throttlePct = body.throttle ?: current.throttlePct,
                        mapKpa = body.mapKpa ?: current.mapKpa,
                        mafGps = body.mafGps ?: current.mafGps,
                        timingAdvDeg = body.timingAdv ?: current.timingAdvDeg,
                        stftPct = body.stft ?: current.stftPct,
                        dtcCount = body.dtcCount ?: current.dtcCount,
                        isLiveFromHardware = true,
                        routeTrail = updatedTrail
                    )
                )
            } else {
                Result.failure(IllegalStateException("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
