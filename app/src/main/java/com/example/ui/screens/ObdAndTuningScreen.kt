package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EcuTuneEntity
import com.example.data.model.A2ATab
import com.example.data.model.ObdPidMetric
import com.example.data.model.VehicleModel3D
import com.example.ui.components.A2ACard
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.A2ABackground
import com.example.ui.theme.A2ABorder
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASecondary
import com.example.ui.theme.A2ATertiary
import com.example.ui.theme.A2ATertiaryContainer
import com.example.ui.theme.A2ATextMuted
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.HudDarkBackground
import com.example.ui.theme.HudDarkBorder
import com.example.ui.theme.HudNeonAmber
import com.example.ui.theme.HudNeonCyan
import com.example.ui.theme.HudNeonGreen
import com.example.ui.theme.TelemetryMonoFontFamily
import com.example.ui.viewmodel.A2AUiState
import kotlin.math.roundToInt

@Composable
fun ObdAndTuningScreen(
    state: A2AUiState,
    onActivateTune: (EcuTuneEntity) -> Unit,
    onSaveAndFlashTune: (EcuTuneEntity) -> Unit,
    onScanClearDtc: (Boolean) -> Unit,
    onNavigateTab: (A2ATab) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = OBD2 & GPS, 1 = จูนกล่อง (aekboy2010)
    var showOnlyBasicPids by remember { mutableStateOf(false) }
    var showNewMapDialog by remember { mutableStateOf(false) }

    val vehicle = state.vehicle
    val telemetry = state.telemetry
    val model = VehicleModel3D.fromId(vehicle?.model3dId ?: "")
    val activeTune = state.tuneProfiles.firstOrNull { it.isActive } ?: state.tuneProfiles.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(A2ABackground)
    ) {
        // Vehicle Header Card
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, A2ABorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = model.imageRes),
                        contentDescription = model.displayName,
                        modifier = Modifier
                            .size(width = 58.dp, height = 38.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = vehicle?.name ?: "Honda Civic FB 1.8",
                                style = MaterialTheme.typography.titleMedium,
                                color = A2ATextPrimary
                            )
                            StatusPillBadge(
                                text = "ออนไลน์",
                                color = A2APrimary,
                                backgroundColor = A2APrimaryContainer
                            )
                        }
                        Text(
                            text = "${telemetry.locationName} • ทะเบียน ${vehicle?.licensePlate ?: "1กฮ3069"}",
                            fontSize = 11.sp,
                            color = A2ATextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%.1f", telemetry.gpsSpeedKmh)} km/h",
                            fontFamily = TelemetryMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = A2ATextPrimary
                        )
                        Text(
                            text = "ECU ${telemetry.ecuVoltage} V",
                            fontFamily = TelemetryMonoFontFamily,
                            fontSize = 11.sp,
                            color = A2APrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Switch: OBD2 vs aekboy2010 ECU Tuning
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_obd2_diagnostics")
                            .clickable { selectedSubTab = 0 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSubTab == 0) Color.White else Color.Transparent,
                        shadowElevation = if (selectedSubTab == 0) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedSubTab == 0) A2APrimary else A2ATextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ข้อมูล OBD2 & GPS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedSubTab == 0) A2ATextPrimary else A2ATextSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_ecu_tuning")
                            .clickable { selectedSubTab = 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSubTab == 1) Color(0xFF0F172A) else Color.Transparent,
                        shadowElevation = if (selectedSubTab == 1) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selectedSubTab == 1) HudNeonGreen else A2ATextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "จูนกล่อง (aekboy2010)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedSubTab == 1) Color.White else A2ATextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            // TAB 0: Full OBD2 & GPS Telemetry Table matching video (00:33 & 01:45)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    A2ACard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            TelemetryRow("ตำแหน่งสถานะ", "${telemetry.locationName} (${String.format("%.1f", telemetry.distanceFromHomeKm)} km)")
                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))
                            TelemetryRow("ทะเบียนรถ", vehicle?.licensePlate ?: "1กฮ3069")
                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))
                            TelemetryRow("พิกัดล่าสุด", String.format("%.6f, %.6f", telemetry.latitude, telemetry.longitude), mono = true)
                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))
                            TelemetryRow("ทิศทาง (Heading)", telemetry.headingLabel)
                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))
                            TelemetryRow("ระดับความสูง (Altitude)", "${telemetry.altitudeM} m เหนือระดับน้ำทะเล")
                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))
                            TelemetryRow("อัปเดตข้อมูลล่าสุด", telemetry.updatedTimeText, mono = true)
                        }
                    }
                }

                item {
                    A2ACard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ค่า PID จากกล่องรถยนต์ (OBD2)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = A2ATextPrimary
                                )
                                FilledTonalButton(
                                    onClick = { showOnlyBasicPids = !showOnlyBasicPids },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (showOnlyBasicPids) "แสดงทั้งหมด" else "เลือก PID ย่อ",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            val pidList = if (showOnlyBasicPids) {
                                listOf(
                                    ObdPidMetric.RPM,
                                    ObdPidMetric.OBD_SPEED,
                                    ObdPidMetric.ENGINE_LOAD,
                                    ObdPidMetric.FUEL_LEVEL,
                                    ObdPidMetric.COOLANT_TEMP,
                                    ObdPidMetric.VOLTAGE
                                )
                            } else {
                                listOf(
                                    ObdPidMetric.RPM,
                                    ObdPidMetric.OBD_SPEED,
                                    ObdPidMetric.ENGINE_LOAD,
                                    ObdPidMetric.FUEL_LEVEL,
                                    ObdPidMetric.COOLANT_TEMP,
                                    ObdPidMetric.VOLTAGE,
                                    ObdPidMetric.THROTTLE,
                                    ObdPidMetric.MAP_KPA,
                                    ObdPidMetric.MAF_GPS,
                                    ObdPidMetric.TIMING_ADV,
                                    ObdPidMetric.STFT_PCT
                                )
                            }

                            pidList.forEachIndexed { idx, pid ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pid.thaiTitle,
                                        fontSize = 13.sp,
                                        color = A2ATextSecondary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = telemetry.formatPidValue(pid),
                                            fontFamily = TelemetryMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = A2ATextPrimary
                                        )
                                        Text(
                                            text = "OBD2",
                                            fontSize = 10.sp,
                                            color = A2APrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (idx < pidList.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }

                            HorizontalDivider(color = A2ABorder, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("รหัสปัญหาเครื่องยนต์ (DTC)", fontSize = 13.sp, color = A2ATextSecondary)
                                    Text(
                                        text = "${telemetry.dtcCount} count",
                                        fontFamily = TelemetryMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (telemetry.dtcCount == 0) A2APrimary else Color(0xFFEF4444)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { onScanClearDtc(false) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("สแกนโค้ด", fontSize = 11.sp)
                                    }
                                    FilledTonalButton(
                                        onClick = { onScanClearDtc(true) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("ล้าง DTC", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateTab(A2ATab.LIVE_MAP) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = A2APrimary)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("นำทางไปยังรถ", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { onNavigateTab(A2ATab.HISTORY) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ประวัติเส้นทาง", fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // TAB 1: aekboy2010 ECU Tuning Console ("จูนกล่องควบคุม")
            EcuTuningConsole(
                tuneProfiles = state.tuneProfiles,
                activeTune = activeTune,
                onActivateTune = onActivateTune,
                onSaveAndFlashTune = onSaveAndFlashTune,
                onOpenNewMapDialog = { showNewMapDialog = true }
            )
        }
    }

    if (showNewMapDialog && activeTune != null) {
        var newName by remember { mutableStateOf("aekboy2010 Custom #${state.tuneProfiles.size + 1}") }
        var newNotes by remember { mutableStateOf("แมพจูนกำหนดเองสำหรับรถคันนี้") }
        AlertDialog(
            onDismissRequest = { showNewMapDialog = false },
            title = { Text("สร้างแมพจูนใหม่ (aekboy2010)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("ชื่อแมพจูน") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("หมายเหตุการจูน") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onSaveAndFlashTune(
                                activeTune.copy(
                                    id = 0,
                                    profileName = newName.trim(),
                                    notes = newNotes.trim(),
                                    isActive = true
                                )
                            )
                            showNewMapDialog = false
                        }
                    }
                ) { Text("สร้างและใช้งาน") }
            },
            dismissButton = {
                TextButton(onClick = { showNewMapDialog = false }) { Text("ยกเลิก") }
            }
        )
    }
}

@Composable
private fun EcuTuningConsole(
    tuneProfiles: List<EcuTuneEntity>,
    activeTune: EcuTuneEntity?,
    onActivateTune: (EcuTuneEntity) -> Unit,
    onSaveAndFlashTune: (EcuTuneEntity) -> Unit,
    onOpenNewMapDialog: () -> Unit
) {
    var fuelTrim by remember(activeTune?.id) { mutableFloatStateOf(activeTune?.fuelTrimPct ?: 4.5f) }
    var ignitionOffset by remember(activeTune?.id) { mutableFloatStateOf(activeTune?.ignitionOffsetDeg ?: 3.0f) }
    var throttleResponse by remember(activeTune?.id) { mutableFloatStateOf(activeTune?.throttleResponsePct ?: 115f) }
    var revLimiter by remember(activeTune?.id) { mutableIntStateOf(activeTune?.revLimiterRpm ?: 7200) }
    var vtecRpm by remember(activeTune?.id) { mutableIntStateOf(activeTune?.vtecEngageRpm ?: 4800) }
    var fanTemp by remember(activeTune?.id) { mutableIntStateOf(activeTune?.fanTriggerTempC ?: 88) }
    var launchRpm by remember(activeTune?.id) { mutableIntStateOf(activeTune?.launchControlRpm ?: 3800) }
    var popBang by remember(activeTune?.id) { mutableStateOf(activeTune?.popAndBangEnabled ?: false) }

    LaunchedEffect(activeTune) {
        activeTune?.let {
            fuelTrim = it.fuelTrimPct
            ignitionOffset = it.ignitionOffsetDeg
            throttleResponse = it.throttleResponsePct
            revLimiter = it.revLimiterRpm
            vtecRpm = it.vtecEngageRpm
            fanTemp = it.fanTriggerTempC
            launchRpm = it.launchControlRpm
            popBang = it.popAndBangEnabled
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dark Telemetry ECU Curve Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = HudDarkBackground,
                border = BorderStroke(1.dp, HudDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "aekboy2010 • จูนกล่องควบคุม ECU",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = activeTune?.profileName ?: "Stage 1 Remap",
                                color = HudNeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = A2ATertiary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "CAN-BUS MAP",
                                color = HudNeonCyan,
                                fontFamily = TelemetryMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live ECU Fuel & Ignition Curve Graph
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Grid
                        for (i in 1..3) {
                            val gy = h * i / 4f
                            drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, gy), Offset(w, gy), 1f)
                        }
                        for (i in 1..5) {
                            val gx = w * i / 6f
                            drawLine(Color.White.copy(alpha = 0.08f), Offset(gx, 0f), Offset(gx, h), 1f)
                        }

                        // Ignition timing curve (Cyan)
                        val ignPath = Path()
                        for (step in 0..20) {
                            val t = step / 20f
                            val x = t * w
                            val normalizedIgn = ((0.35f + t * 0.42f + (ignitionOffset / 25f)) - (if (t > (revLimiter / 9000f)) 0.25f else 0f)).coerceIn(0.1f, 0.92f)
                            val y = h * (1f - normalizedIgn)
                            if (step == 0) ignPath.moveTo(x, y) else ignPath.lineTo(x, y)
                        }
                        drawPath(ignPath, color = HudNeonCyan, style = Stroke(width = 4.5f, cap = StrokeCap.Round))

                        // Fuel trim curve (Emerald Green)
                        val fuelPath = Path()
                        for (step in 0..20) {
                            val t = step / 20f
                            val x = t * w
                            val vtecBoost = if (t * 8500f >= vtecRpm) 0.1f else 0f
                            val normalizedFuel = (0.28f + t * 0.36f + (fuelTrim / 45f) + vtecBoost).coerceIn(0.1f, 0.94f)
                            val y = h * (1f - normalizedFuel)
                            if (step == 0) fuelPath.moveTo(x, y) else fuelPath.lineTo(x, y)
                        }
                        drawPath(fuelPath, color = HudNeonGreen, style = Stroke(width = 4.5f, cap = StrokeCap.Round))

                        // Rev limiter vertical line
                        val limitX = ((revLimiter - 1000f) / 7800f).coerceIn(0.2f, 0.98f) * w
                        drawLine(
                            color = HudNeonAmber,
                            start = Offset(limitX, 0f),
                            end = Offset(limitX, h),
                            strokeWidth = 3f
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("— น้ำมัน (${String.format("%+.1f", fuelTrim)}%)", color = HudNeonGreen, fontSize = 11.sp, fontFamily = TelemetryMonoFontFamily)
                        Text("— ไฟจุดระเบิด (${String.format("%+.1f", ignitionOffset)}°)", color = HudNeonCyan, fontSize = 11.sp, fontFamily = TelemetryMonoFontFamily)
                        Text("| ตัดรอบ $revLimiter", color = HudNeonAmber, fontSize = 11.sp, fontFamily = TelemetryMonoFontFamily)
                    }
                }
            }
        }

        // Saved Map Presets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("โปรไฟล์แมพจูน (Saved Maps)", style = MaterialTheme.typography.titleMedium, color = A2ATextPrimary)
                TextButton(onClick = onOpenNewMapDialog) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("สร้างแมพใหม่", fontSize = 12.sp)
                }
            }
        }

        items(tuneProfiles, key = { it.id }) { profile ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tune_profile_${profile.id}")
                    .clickable { onActivateTune(profile) },
                shape = RoundedCornerShape(16.dp),
                color = if (profile.isActive) A2ATertiaryContainer.copy(alpha = 0.45f) else Color.White,
                border = BorderStroke(if (profile.isActive) 1.5.dp else 1.dp, if (profile.isActive) A2ATertiary else A2ABorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(profile.profileName, style = MaterialTheme.typography.titleMedium, color = A2ATextPrimary)
                            if (profile.isActive) {
                                StatusPillBadge("ใช้งานอยู่", A2ATertiary, A2ATertiaryContainer)
                            }
                        }
                        Text(profile.notes, fontSize = 11.sp, color = A2ATextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fuel ${String.format("%+.1f", profile.fuelTrimPct)}% • Ign ${String.format("%+.1f", profile.ignitionOffsetDeg)}° • Rev ${profile.revLimiterRpm}",
                            fontFamily = TelemetryMonoFontFamily,
                            fontSize = 11.sp,
                            color = A2ATextMuted
                        )
                    }
                    if (profile.isActive) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = A2ATertiary)
                    }
                }
            }
        }

        // Fine-Tuning Sliders
        item {
            A2ACard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ปรับค่าจูนละเอียด (Live Parameter Remap)",
                        style = MaterialTheme.typography.titleMedium,
                        color = A2ATextPrimary
                    )

                    TuneSliderRow(
                        label = "ชดเชยน้ำมัน (Fuel Trim)",
                        valueText = String.format("%+.1f %%", fuelTrim),
                        value = fuelTrim,
                        range = -15f..25f,
                        onValueChange = { fuelTrim = (it * 2f).roundToInt() / 2f }
                    )
                    TuneSliderRow(
                        label = "องศาไฟจุดระเบิด (Ignition Offset)",
                        valueText = String.format("%+.1f °", ignitionOffset),
                        value = ignitionOffset,
                        range = -5f..10f,
                        onValueChange = { ignitionOffset = (it * 2f).roundToInt() / 2f }
                    )
                    TuneSliderRow(
                        label = "ตอบสนองคันเร่ง (Throttle Gain)",
                        valueText = "${throttleResponse.roundToInt()} %",
                        value = throttleResponse,
                        range = 70f..150f,
                        onValueChange = { throttleResponse = it.roundToInt().toFloat() }
                    )
                    TuneSliderRow(
                        label = "รอบตัดเครื่องยนต์ (Rev Limiter)",
                        valueText = "$revLimiter rpm",
                        value = revLimiter.toFloat(),
                        range = 6200f..8500f,
                        onValueChange = { revLimiter = (it / 100f).roundToInt() * 100 }
                    )
                    TuneSliderRow(
                        label = "เปิดแคม/วาล์วแปรผัน (VTEC Point)",
                        valueText = "$vtecRpm rpm",
                        value = vtecRpm.toFloat(),
                        range = 4000f..6000f,
                        onValueChange = { vtecRpm = (it / 100f).roundToInt() * 100 }
                    )
                    TuneSliderRow(
                        label = "อุณหภูมิพัดลมทำงาน (Fan Temp)",
                        valueText = "$fanTemp °C",
                        value = fanTemp.toFloat(),
                        range = 80f..98f,
                        onValueChange = { fanTemp = it.roundToInt() }
                    )
                    TuneSliderRow(
                        label = "รอบออกตัว (Launch Control)",
                        valueText = "$launchRpm rpm",
                        value = launchRpm.toFloat(),
                        range = 3000f..5500f,
                        onValueChange = { launchRpm = (it / 100f).roundToInt() * 100 }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pop & Bang (Overrun Fuel Cut)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = A2ATextPrimary)
                            Text("ยิงท่อเบาๆ เมื่อถอนคันเร่งที่รอบสูง", fontSize = 11.sp, color = A2ATextSecondary)
                        }
                        Switch(
                            checked = popBang,
                            onCheckedChange = { popBang = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = A2ATertiary)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val base = activeTune ?: EcuTuneEntity(
                                vehicleId = 1,
                                profileName = "aekboy2010 Stage 1",
                                authorTag = "aekboy2010",
                                fuelTrimPct = fuelTrim,
                                ignitionOffsetDeg = ignitionOffset,
                                throttleResponsePct = throttleResponse,
                                revLimiterRpm = revLimiter,
                                vtecEngageRpm = vtecRpm,
                                fanTriggerTempC = fanTemp,
                                launchControlRpm = launchRpm,
                                popAndBangEnabled = popBang,
                                isActive = true,
                                notes = "จูนผ่านแอป A2A"
                            )
                            onSaveAndFlashTune(
                                base.copy(
                                    fuelTrimPct = fuelTrim,
                                    ignitionOffsetDeg = ignitionOffset,
                                    throttleResponsePct = throttleResponse,
                                    revLimiterRpm = revLimiter,
                                    vtecEngageRpm = vtecRpm,
                                    fanTriggerTempC = fanTemp,
                                    launchControlRpm = launchRpm,
                                    popAndBangEnabled = popBang,
                                    isActive = true,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("flash_ecu_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2ATertiary)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("บันทึกและแฟลชลงกล่อง (Flash ECU)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun TuneSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = A2ATextSecondary)
            Text(
                text = valueText,
                fontFamily = TelemetryMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = A2ATextPrimary
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = A2ATertiary,
                activeTrackColor = A2ATertiary
            )
        )
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    mono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = A2ATextSecondary)
        Text(
            text = value,
            fontFamily = if (mono) TelemetryMonoFontFamily else MaterialTheme.typography.bodyMedium.fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = A2ATextPrimary
        )
    }
}
