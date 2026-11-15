package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R

enum class VehicleModel3D(
    val id: String,
    val displayName: String,
    val subtitle: String,
    @DrawableRes val imageRes: Int
) {
    CIVIC_FB(
        id = "civic_fb",
        displayName = "Honda Civic FB / Si",
        subtitle = "Honda • 1.8 MB • เก็บในเครื่องแล้ว",
        imageRes = R.drawable.img_car_civic_fb
    ),
    BYD_SEAL(
        id = "byd_seal",
        displayName = "BYD Seal Performance AWD",
        subtitle = "BYD • 7.7 MB • เก็บในเครื่องแล้ว",
        imageRes = R.drawable.img_car_byd_seal
    ),
    PORSCHE_MISSION_R(
        id = "porsche_mission_r",
        displayName = "Porsche Mission R Concept",
        subtitle = "Porsche • 12.6 MB • เก็บในเครื่องแล้ว",
        imageRes = R.drawable.img_car_porsche_mission
    ),
    PORSCHE_992_GT3(
        id = "porsche_992_gt3",
        displayName = "Porsche 992 GT3 R",
        subtitle = "Porsche • 34.9 MB • เก็บในเครื่องแล้ว",
        imageRes = R.drawable.img_car_porsche_gt3
    );

    companion object {
        fun fromId(id: String): VehicleModel3D =
            entries.firstOrNull { it.id == id } ?: CIVIC_FB
    }
}

enum class CarPaintFinish(val id: String, val label: String) {
    GLOSS("gloss", "เงา (Gloss)"),
    METALLIC("metallic", "เมทัลลิค (Metallic)"),
    MATTE("matte", "ด้าน (Matte)");

    companion object {
        fun fromId(id: String): CarPaintFinish =
            entries.firstOrNull { it.id == id } ?: GLOSS
    }
}

data class CarPaintSwatch(
    val name: String,
    val colorLong: Long
) {
    val color: Color get() = Color(colorLong)
}

val DefaultPaintSwatches = listOf(
    CarPaintSwatch("ดำเงา", 0xFF1E293B),
    CarPaintSwatch("ขาวมุก", 0xFFF8FAFC),
    CarPaintSwatch("เงินเมทัลลิค", 0xFFCBD5E1),
    CarPaintSwatch("เทาดำ", 0xFF475569),
    CarPaintSwatch("แดงสด", 0xFFDC2626),
    CarPaintSwatch("น้ำเงิน", 0xFF2563EB),
    CarPaintSwatch("ทอง GT", 0xFFD97706),
    CarPaintSwatch("เขียวมรกต", 0xFF059669)
)

val DefaultGlassSwatches = listOf(
    CarPaintSwatch("ใสมาตรฐาน", 0xFF94A3B8),
    CarPaintSwatch("ฟิล์มดำ 40%", 0xFF475569),
    CarPaintSwatch("ฟิล์มดำ 60%", 0xFF1E293B),
    CarPaintSwatch("ฟิล์มดำ 80%", 0xFF0F172A),
    CarPaintSwatch("ปรอทฟ้า", 0xFF0284C7),
    CarPaintSwatch("ปรอทเขียว", 0xFF0D9488)
)

enum class ObdPidMetric(
    val key: String,
    val thaiTitle: String,
    val shortLabel: String,
    val unit: String
) {
    VOLTAGE("_volt", "แรงดันไฟ (Voltage)", "VOLT", "V"),
    GPS_SPEED("_speed", "ความเร็ว GPS (Speed)", "GPS SPD", "km/h"),
    GPS_FIX("_gps", "พิกัด (GPS Fix)", "GPS", ""),
    RPM("rpm", "รอบเครื่องยนต์ (RPM)", "RPM", "rpm"),
    OBD_SPEED("speed_obd", "ความเร็ว OBD2", "SPEED", "km/h"),
    THROTTLE("throttle_pct", "ตำแหน่งลิ้นปีกผีเสื้อ", "THROTTLE", "%"),
    ENGINE_LOAD("engine_load_pct", "โหลดเครื่องยนต์", "LOAD", "%"),
    MAP_KPA("map_kpa", "แรงดันท่อร่วมไอดี (MAP)", "MAP", "kPa"),
    MAF_GPS("maf_gps", "อัตราการไหลอากาศ (MAF)", "MAF", "g/s"),
    TIMING_ADV("timing_adv", "องศาไฟจุดระเบิด", "TIMING", "°"),
    STFT_PCT("stft_pct", "ชดเชยน้ำมันระยะสั้น (STFT)", "STFT", "%"),
    COOLANT_TEMP("coolant_c", "อุณหภูมิน้ำหล่อเย็น", "COOLANT", "°C"),
    FUEL_LEVEL("fuel_pct", "ระดับน้ำมันเชื้อเพลิง", "FUEL", "%");

    companion object {
        fun fromKey(key: String): ObdPidMetric =
            entries.firstOrNull { it.key == key } ?: RPM
    }
}

data class LiveTelemetry(
    val latitude: Double = 17.043119,
    val longitude: Double = 99.073534,
    val headingDeg: Int = 7,
    val headingLabel: String = "7° - N (เหนือ)",
    val altitudeM: Double = 113.2,
    val gpsSatellites: Int = 22,
    val gpsSpeedKmh: Double = 0.0,
    val obdSpeedKmh: Int = 0,
    val rpm: Int = 828,
    val engineLoadPct: Double = 19.2,
    val fuelLevelPct: Double = 22.8,
    val coolantTempC: Int = 58,
    val ecuVoltage: Double = 14.4,
    val throttlePct: Double = 16.5,
    val mapKpa: Int = 50,
    val mafGps: Double = 4.4,
    val timingAdvDeg: Double = 26.5,
    val stftPct: Double = 1.6,
    val dtcCount: Int = 0,
    val dtcCodes: List<String> = emptyList(),
    val simModuleInfo: String = "4G LTE ออนไลน์ • Sim:03 A7670E",
    val locationName: String = "บ้านเดิม (ตาก)",
    val distanceFromHomeKm: Double = 0.0,
    val distanceTodayKm: Double = 117.5,
    val updatedTimeText: String = "07:37:10 น.",
    val isLiveFromHardware: Boolean = false,
    val isDrivingSimulated: Boolean = false,
    val routeTrail: List<Pair<Double, Double>> = listOf(
        17.0398 to 99.0715,
        17.0406 to 99.0721,
        17.0414 to 99.0726,
        17.0422 to 99.0730,
        17.043119 to 99.073534
    )
) {
    fun formatPidValue(metric: ObdPidMetric): String = when (metric) {
        ObdPidMetric.VOLTAGE -> String.format("%.2f V", ecuVoltage)
        ObdPidMetric.GPS_SPEED -> String.format("%.1f km/h", gpsSpeedKmh)
        ObdPidMetric.GPS_FIX -> String.format("%.4f, %.4f", latitude, longitude)
        ObdPidMetric.RPM -> "$rpm rpm"
        ObdPidMetric.OBD_SPEED -> "$obdSpeedKmh km/h"
        ObdPidMetric.THROTTLE -> String.format("%.1f %%", throttlePct)
        ObdPidMetric.ENGINE_LOAD -> String.format("%.1f %%", engineLoadPct)
        ObdPidMetric.MAP_KPA -> "$mapKpa kPa"
        ObdPidMetric.MAF_GPS -> String.format("%.1f g/s", mafGps)
        ObdPidMetric.TIMING_ADV -> String.format("%.1f deg", timingAdvDeg)
        ObdPidMetric.STFT_PCT -> String.format("%+.1f %%", stftPct)
        ObdPidMetric.COOLANT_TEMP -> "$coolantTempC °C"
        ObdPidMetric.FUEL_LEVEL -> String.format("%.1f %%", fuelLevelPct)
    }

    fun formatCompactValue(metric: ObdPidMetric): String = when (metric) {
        ObdPidMetric.VOLTAGE -> String.format("%.1fV", ecuVoltage)
        ObdPidMetric.GPS_SPEED -> String.format("%.0f", gpsSpeedKmh)
        ObdPidMetric.GPS_FIX -> "$gpsSatellites SAT"
        ObdPidMetric.RPM -> "$rpm"
        ObdPidMetric.OBD_SPEED -> "$obdSpeedKmh"
        ObdPidMetric.THROTTLE -> String.format("%.1f%%", throttlePct)
        ObdPidMetric.ENGINE_LOAD -> String.format("%.0f%%", engineLoadPct)
        ObdPidMetric.MAP_KPA -> "$mapKpa kPa"
        ObdPidMetric.MAF_GPS -> String.format("%.1f", mafGps)
        ObdPidMetric.TIMING_ADV -> String.format("%.1f°", timingAdvDeg)
        ObdPidMetric.STFT_PCT -> String.format("%+.1f%%", stftPct)
        ObdPidMetric.COOLANT_TEMP -> "$coolantTempC°C"
        ObdPidMetric.FUEL_LEVEL -> String.format("%.0f%%", fuelLevelPct)
    }

    fun progressFraction(metric: ObdPidMetric): Float = when (metric) {
        ObdPidMetric.VOLTAGE -> ((ecuVoltage - 11.0) / 4.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.GPS_SPEED -> (gpsSpeedKmh / 180.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.GPS_FIX -> (gpsSatellites / 24f).coerceIn(0f, 1f)
        ObdPidMetric.RPM -> (rpm / 8000f).coerceIn(0f, 1f)
        ObdPidMetric.OBD_SPEED -> (obdSpeedKmh / 180f).coerceIn(0f, 1f)
        ObdPidMetric.THROTTLE -> (throttlePct / 100.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.ENGINE_LOAD -> (engineLoadPct / 100.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.MAP_KPA -> (mapKpa / 220f).coerceIn(0f, 1f)
        ObdPidMetric.MAF_GPS -> (mafGps / 150.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.TIMING_ADV -> ((timingAdvDeg + 10.0) / 55.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.STFT_PCT -> ((stftPct + 25.0) / 50.0).toFloat().coerceIn(0f, 1f)
        ObdPidMetric.COOLANT_TEMP -> ((coolantTempC - 20f) / 100f).coerceIn(0f, 1f)
        ObdPidMetric.FUEL_LEVEL -> (fuelLevelPct / 100.0).toFloat().coerceIn(0f, 1f)
    }
}

enum class A2ATab(val route: String, val label: String) {
    OVERVIEW("overview", "ภาพรวม"),
    STUDIO("studio", "รถ & 3D"),
    LIVE_MAP("live_map", "แผนที่สด"),
    OBD_ECU("obd_ecu", "OBD2 & จูน"),
    HISTORY("history", "ประวัติ")
}
