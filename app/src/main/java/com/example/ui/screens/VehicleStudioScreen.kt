package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.A2ATab
import com.example.data.model.CarPaintFinish
import com.example.data.model.DefaultGlassSwatches
import com.example.data.model.DefaultPaintSwatches
import com.example.data.model.VehicleModel3D
import com.example.ui.components.A2ACard
import com.example.ui.components.CarStudioViewer
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.A2ABackground
import com.example.ui.theme.A2ABorder
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASecondary
import com.example.ui.theme.A2ASecondaryContainer
import com.example.ui.theme.A2ASurface
import com.example.ui.theme.A2ATextMuted
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.TelemetryMonoFontFamily
import com.example.ui.viewmodel.A2AUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleStudioScreen(
    state: A2AUiState,
    onNavigateTab: (A2ATab) -> Unit,
    onSelectModel3D: (VehicleModel3D) -> Unit,
    onUpdateCarStyle: (Long, CarPaintFinish, Long) -> Unit,
    onToggleLiveTracking: (Boolean) -> Unit,
    onToggleDrivingSim: (Boolean) -> Unit,
    onThrottleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicle = state.vehicle
    val telemetry = state.telemetry
    val model = VehicleModel3D.fromId(vehicle?.model3dId ?: "")
    val finish = CarPaintFinish.fromId(vehicle?.paintFinishId ?: "")
    val paintColor = vehicle?.paintColorLong ?: 0xFF1E293B
    val glassColor = vehicle?.glassColorLong ?: 0xFF0F172A

    var autoOrbit by remember { mutableStateOf(false) }
    var showModelPicker by remember { mutableStateOf(false) }
    var showCarStudio by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(A2ABackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vehicle Header & Voltage + Quick Actions (matching 00:37 & 01:05)
        item {
            A2ACard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${vehicle?.licensePlate ?: "1กฮ3069"} • ${vehicle?.subtitle ?: "EV / Sedan"}",
                                fontSize = 11.sp,
                                color = A2ATextSecondary
                            )
                            Text(
                                text = vehicle?.name ?: "Honda Civic FB 1.8",
                                style = MaterialTheme.typography.titleLarge,
                                color = A2ATextPrimary
                            )
                        }
                        StatusPillBadge(
                            text = if (telemetry.isLiveFromHardware) "ESP32 Live" else "A2A Live",
                            color = A2APrimary,
                            backgroundColor = A2APrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "แรงดันไฟจากกล่อง ECU",
                                fontSize = 11.sp,
                                color = A2ATextSecondary
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", telemetry.ecuVoltage),
                                    fontFamily = TelemetryMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = A2ATextPrimary
                                )
                                Text(
                                    text = " V",
                                    fontFamily = TelemetryMonoFontFamily,
                                    fontSize = 15.sp,
                                    color = A2ATextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = "OBD2 Live • แสดงผลเรียลไทม์",
                                fontSize = 11.sp,
                                color = A2APrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickCircleShortcut(
                                icon = Icons.Default.Navigation,
                                label = "นำทาง",
                                onClick = {
                                    val uri = Uri.parse("geo:${telemetry.latitude},${telemetry.longitude}?q=${telemetry.latitude},${telemetry.longitude}(A2A Vehicle)")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    runCatching { context.startActivity(intent) }
                                }
                            )
                            QuickCircleShortcut(
                                icon = Icons.Default.History,
                                label = "ประวัติ",
                                onClick = { onNavigateTab(A2ATab.HISTORY) }
                            )
                            QuickCircleShortcut(
                                icon = Icons.Default.Tune,
                                label = "จูนกล่อง",
                                onClick = { onNavigateTab(A2ATab.OBD_ECU) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4G LTE + SIM A7670E Connection Bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = telemetry.simModuleInfo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = A2APrimary
                            )
                            Text(
                                text = "เชื่อมต่อกับรถ • เรียลไทม์",
                                fontSize = 11.sp,
                                color = A2ATextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Interactive 3D Vehicle Stage & Car Studio Buttons
        item {
            A2ACard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    CarStudioViewer(
                        model = model,
                        paintColorLong = paintColor,
                        finish = finish,
                        glassColorLong = glassColor,
                        autoOrbit = autoOrbit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { autoOrbit = !autoOrbit },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_3d_orbit"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "มุมมอง 3D",
                                modifier = Modifier.size(16.dp),
                                tint = if (autoOrbit) A2APrimary else A2ATextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (autoOrbit) "กำลังหมุน 3D" else "มุมมอง 3D",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        FilledTonalButton(
                            onClick = { showCarStudio = true },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("open_car_studio"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("แต่งสี & กระจก", fontSize = 11.sp, maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = { showModelPicker = true },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("open_model_picker"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = A2APrimaryContainer,
                                contentColor = A2APrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("เปลี่ยนโมเดลรถ", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // RPM & Distance Today Side-by-Side Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                A2ACard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("รอบเครื่องยนต์ (RPM)", fontSize = 12.sp, color = A2ATextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${telemetry.rpm}",
                                fontFamily = TelemetryMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = A2ATextPrimary
                            )
                            Text(
                                text = " rpm",
                                fontSize = 12.sp,
                                color = A2ATextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (telemetry.rpm / 7500f).coerceIn(0.05f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (telemetry.rpm > 5500) Color(0xFFEF4444) else A2APrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "OBD2 Live • แสดงผลเรียลไทม์",
                            fontSize = 10.sp,
                            color = A2APrimary
                        )
                    }
                }

                A2ACard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ระยะทางวันนี้", fontSize = 12.sp, color = A2ATextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.1f", telemetry.distanceTodayKm),
                                fontFamily = TelemetryMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = A2ATextPrimary
                            )
                            Text(
                                text = " km",
                                fontSize = 12.sp,
                                color = A2ATextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "จากรอบบันทึก 2 เที่ยววันนี้",
                            fontSize = 11.sp,
                            color = A2ATextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.4f, %.4f", telemetry.latitude, telemetry.longitude),
                            fontFamily = TelemetryMonoFontFamily,
                            fontSize = 10.sp,
                            color = A2ATextMuted
                        )
                    }
                }
            }
        }

        // Live Tracking + Interactive Throttle Test Control
        item {
            A2ACard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ติดตามสด & สถานะจอดนิ่ง",
                                style = MaterialTheme.typography.titleMedium,
                                color = A2ATextPrimary
                            )
                            Text(
                                text = "อัปเดตทุก 5 วินาที • GPS ${telemetry.gpsSatellites} ดาว",
                                fontSize = 11.sp,
                                color = A2ATextSecondary
                            )
                        }
                        Switch(
                            checked = vehicle?.isLiveTracking ?: true,
                            onCheckedChange = onToggleLiveTracking,
                            colors = SwitchDefaults.colors(checkedTrackColor = A2APrimary)
                        )
                    }

                    if (vehicle?.esp32EndpointUrl.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, A2ABorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ทดสอบคันเร่ง / จำลองการขับขี่",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = A2ATextPrimary
                                    )
                                    FilledTonalButton(
                                        onClick = { onToggleDrivingSim(!state.isDrivingSimulation) },
                                        modifier = Modifier.testTag("sim_drive_toggle"),
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (state.isDrivingSimulation) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (state.isDrivingSimulation) "หยุดวิ่ง" else "จำลองขับรถ",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Slider(
                                    value = state.simulatedThrottlePct,
                                    onValueChange = onThrottleChange,
                                    valueRange = 0f..100f
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "คันเร่ง: ${state.simulatedThrottlePct.toInt()}%",
                                        fontFamily = TelemetryMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = A2ATextSecondary
                                    )
                                    Text(
                                        text = "ความเร็ว: ${String.format("%.1f", telemetry.gpsSpeedKmh)} km/h",
                                        fontFamily = TelemetryMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = A2ASecondary
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateTab(A2ATab.OBD_ECU) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("studio_to_obd_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ดูข้อมูล ECU, OBD2 และจูนกล่องควบคุม")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    // Bottom Sheet 1: เลือกโมเดลรถ 3D (matches 01:10 of video)
    if (showModelPicker) {
        ModalBottomSheet(
            onDismissRequest = { showModelPicker = false },
            containerColor = A2ASurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "เลือกโมเดลรถ 3D",
                    style = MaterialTheme.typography.titleLarge,
                    color = A2ATextPrimary
                )
                Text(
                    text = "ดาวน์โหลดเก็บไว้ในเครื่องแล้ว • พร้อมแต่งสีใน Car Studio",
                    fontSize = 12.sp,
                    color = A2ATextSecondary
                )

                VehicleModel3D.entries.forEach { itemModel ->
                    val selected = itemModel == model
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("model_option_${itemModel.id}")
                            .clickable {
                                onSelectModel3D(itemModel)
                                showModelPicker = false
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) A2APrimaryContainer.copy(alpha = 0.45f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) A2APrimary else A2ABorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = itemModel.imageRes),
                                contentDescription = itemModel.displayName,
                                modifier = Modifier
                                    .size(width = 76.dp, height = 50.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = itemModel.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = A2ATextPrimary
                                )
                                Text(
                                    text = itemModel.subtitle,
                                    fontSize = 11.sp,
                                    color = A2ATextSecondary
                                )
                            }
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(A2APrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "เลือกอยู่",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Bottom Sheet 2: Car Studio - แต่งสีรถ & กระจก (matches 01:17 of video)
    if (showCarStudio) {
        ModalBottomSheet(
            onDismissRequest = { showCarStudio = false },
            containerColor = A2ASurface
        ) {
            var activeTab by remember { mutableIntStateOf(0) } // 0 = Paint, 1 = Glass
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Car Studio • แต่งสีรถ & กระจก",
                    style = MaterialTheme.typography.titleLarge,
                    color = A2ATextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { activeTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (activeTab == 0) A2APrimaryContainer else Color(0xFFF1F5F9)
                        )
                    ) { Text("ตัวถัง (Paint)") }
                    FilledTonalButton(
                        onClick = { activeTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (activeTab == 1) A2ASecondaryContainer else Color(0xFFF1F5F9)
                        )
                    ) { Text("ฟิล์มกระจก (Glass)") }
                }

                if (activeTab == 0) {
                    Text("ผิวสี (Finish)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = A2ATextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CarPaintFinish.entries.forEach { f ->
                            val selected = f == finish
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateCarStyle(paintColor, f, glassColor) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = f.label.substringBefore(" "),
                                    color = if (selected) Color.White else A2ATextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                )
                            }
                        }
                    }

                    Text("เฉดสีตัวถัง", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = A2ATextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(DefaultPaintSwatches) { swatch ->
                            val isSelected = swatch.colorLong == paintColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("paint_swatch_${swatch.name}")
                                    .clickable { onUpdateCarStyle(swatch.colorLong, finish, glassColor) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) A2APrimary else Color(0xFFCBD5E1),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = swatch.name,
                                            tint = if (swatch.colorLong == 0xFFF8FAFC || swatch.colorLong == 0xFFCBD5E1) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(swatch.name, fontSize = 10.sp, color = A2ATextSecondary)
                            }
                        }
                    }
                } else {
                    Text("ระดับความเข้มฟิล์มกระจก", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = A2ATextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(DefaultGlassSwatches) { swatch ->
                            val isSelected = swatch.colorLong == glassColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    onUpdateCarStyle(paintColor, finish, swatch.colorLong)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) A2ASecondary else Color(0xFFCBD5E1),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(swatch.name, fontSize = 10.sp, color = A2ATextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun QuickCircleShortcut(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, A2ABorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = A2ATextPrimary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, color = A2ATextSecondary)
    }
}
