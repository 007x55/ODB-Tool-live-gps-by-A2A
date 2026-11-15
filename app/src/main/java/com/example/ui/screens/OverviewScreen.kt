package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IoTNodeEntity
import com.example.data.model.A2ATab
import com.example.data.model.VehicleModel3D
import com.example.ui.components.A2ACard
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.A2ABackground
import com.example.ui.theme.A2ADanger
import com.example.ui.theme.A2ADangerContainer
import com.example.ui.theme.A2AOnDangerContainer
import com.example.ui.theme.A2AOnPrimaryContainer
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASecondary
import com.example.ui.theme.A2ASecondaryContainer
import com.example.ui.theme.A2ATertiary
import com.example.ui.theme.A2ATertiaryContainer
import com.example.ui.theme.A2ATextMuted
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.A2AWarning
import com.example.ui.theme.A2AWarningContainer
import com.example.ui.theme.TelemetryMonoFontFamily
import com.example.ui.viewmodel.A2AUiState

@Composable
fun OverviewScreen(
    state: A2AUiState,
    onNavigateTab: (A2ATab) -> Unit,
    onAddDevice: (title: String, chip: String, category: String, endpointUrl: String, plate: String?) -> Unit,
    onUpdateEsp32Endpoint: (String) -> Unit,
    onDeleteNode: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showEndpointDialog by remember { mutableStateOf(false) }

    val vehicle = state.vehicle
    val telemetry = state.telemetry
    val activeTune = state.tuneProfiles.firstOrNull { it.isActive }
    val model3d = VehicleModel3D.fromId(vehicle?.model3dId ?: "")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(A2ABackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A2A",
                            color = A2APrimary,
                            fontFamily = TelemetryMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            text = "A2A TELEMETRY",
                            style = MaterialTheme.typography.titleLarge,
                            color = A2ATextPrimary
                        )
                        Text(
                            text = "Smart Vehicle & aekboy2010 ECU Monitor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = A2ATextSecondary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showEndpointDialog = true },
                    modifier = Modifier.testTag("esp32_config_button"),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsEthernet,
                        contentDescription = "ตั้งค่า ESP32",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (vehicle?.esp32EndpointUrl.isNullOrBlank()) "ESP32 Demo" else "ESP32 Live",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top 4 Mini Summary KPI Chips (like Solar Guard top row in video)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniKpiTile(
                    icon = Icons.Default.Memory,
                    iconColor = A2APrimary,
                    iconBg = A2APrimaryContainer,
                    value = "${state.iotNodes.count { it.isOnline }}/${state.iotNodes.size.coerceAtLeast(1)}",
                    label = "อุปกรณ์",
                    modifier = Modifier.weight(1f)
                )
                MiniKpiTile(
                    icon = Icons.Default.Speed,
                    iconColor = A2ASecondary,
                    iconBg = A2ASecondaryContainer,
                    value = String.format("%.0f", telemetry.gpsSpeedKmh),
                    label = "km/h",
                    modifier = Modifier.weight(1f)
                )
                MiniKpiTile(
                    icon = Icons.Default.BatteryChargingFull,
                    iconColor = A2AWarning,
                    iconBg = A2AWarningContainer,
                    value = "${telemetry.fuelLevelPct.toInt()}%",
                    label = "เชื้อเพลิง",
                    modifier = Modifier.weight(1f)
                )
                MiniKpiTile(
                    icon = Icons.Default.CheckCircle,
                    iconColor = if (telemetry.dtcCount == 0) A2APrimary else A2ADanger,
                    iconBg = if (telemetry.dtcCount == 0) A2APrimaryContainer else A2ADangerContainer,
                    value = "${telemetry.dtcCount}",
                    label = "DTC โค้ด",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Hardware / Simulation Info Banner
        item {
            val hasError = state.hardwareError != null
            A2ACard(
                containerColor = if (hasError) A2ADangerContainer else Color(0xFFECFDF5),
                borderColor = if (hasError) A2ADanger.copy(alpha = 0.3f) else A2APrimary.copy(alpha = 0.28f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (hasError) Icons.Default.WarningAmber else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (hasError) A2ADanger else A2APrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.hardwareError
                                ?: if (telemetry.isLiveFromHardware)
                                    "เชื่อมต่อฮาร์ดแวร์ ESP32 + SIM A7670E สำเร็จ"
                                else
                                    "โหมดจำลองสัญญาณ ESP32 + OBD2 (พร้อมสลับเป็น URL จริงได้)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasError) A2AOnDangerContainer else A2AOnPrimaryContainer
                        )
                        Text(
                            text = "แมพ ECU ปัจจุบัน: ${activeTune?.profileName ?: "aekboy2010 Stage 1"}",
                            fontSize = 11.sp,
                            color = A2ATextSecondary
                        )
                    }
                }
            }
        }

        // Section: Primary Vehicle Tracking Card
        item {
            Text(
                text = "นำเดิน • ยานพาหนะที่ติดตาม",
                style = MaterialTheme.typography.titleMedium,
                color = A2ATextPrimary
            )
        }

        item {
            A2ACard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overview_vehicle_card"),
                onClick = { onNavigateTab(A2ATab.STUDIO) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusPillBadge(
                                    text = if (telemetry.gpsSpeedKmh > 1.0)
                                        "กำลังวิ่ง ${String.format("%.1f", telemetry.gpsSpeedKmh)} km/h"
                                    else
                                        "จอดอยู่ 0.0 km/h",
                                    color = if (telemetry.gpsSpeedKmh > 1.0) A2ASecondary else A2APrimary,
                                    backgroundColor = if (telemetry.gpsSpeedKmh > 1.0) A2ASecondaryContainer else A2APrimaryContainer
                                )
                                Text(
                                    text = vehicle?.licensePlate ?: "1กฮ3069",
                                    fontFamily = TelemetryMonoFontFamily,
                                    fontSize = 12.sp,
                                    color = A2ATextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = vehicle?.name ?: "Honda Civic FB 1.8",
                                style = MaterialTheme.typography.titleLarge,
                                color = A2ATextPrimary
                            )
                            Text(
                                text = "${telemetry.ecuVoltage} V ECU • รอบ ${telemetry.rpm} rpm • ${telemetry.locationName}",
                                fontSize = 12.sp,
                                color = A2ATextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = model3d.imageRes),
                            contentDescription = model3d.displayName,
                            modifier = Modifier
                                .size(width = 98.dp, height = 64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onNavigateTab(A2ATab.STUDIO) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("go_studio_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("หน้าปัด 3D", fontSize = 12.sp)
                        }
                        FilledTonalButton(
                            onClick = { onNavigateTab(A2ATab.LIVE_MAP) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("go_map_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("แผนที่สด", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onNavigateTab(A2ATab.OBD_ECU) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("go_tuning_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = A2ATertiary),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("จูนกล่อง", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: IoT / BMS / ESP32 Hardware Nodes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "อุปกรณ์ IoT & ระบบไฟ (ESP32)",
                    style = MaterialTheme.typography.titleMedium,
                    color = A2ATextPrimary
                )
                TextButton(
                    onClick = { showAddDeviceDialog = true },
                    modifier = Modifier.testTag("add_device_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "เพิ่มอุปกรณ์", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เพิ่มอุปกรณ์", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(state.iotNodes, key = { it.id }) { node ->
            IoTNodeCard(
                node = node,
                onDelete = { onDeleteNode(node.id) }
            )
        }

        item {
            OutlinedButton(
                onClick = { showAddDeviceDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_device_full_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("เพิ่มอุปกรณ์ ESP32 / CAN Bus / BMS", fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onConfirm = { title, chip, cat, url, plate ->
                onAddDevice(title, chip, cat, url, plate)
                showAddDeviceDialog = false
            }
        )
    }

    if (showEndpointDialog) {
        Esp32EndpointDialog(
            currentUrl = vehicle?.esp32EndpointUrl.orEmpty(),
            onDismiss = { showEndpointDialog = false },
            onSave = { url ->
                onUpdateEsp32Endpoint(url)
                showEndpointDialog = false
            }
        )
    }
}

@Composable
private fun MiniKpiTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    A2ACard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = TelemetryMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = A2ATextPrimary,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = A2ATextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun IoTNodeCard(
    node: IoTNodeEntity,
    onDelete: () -> Unit
) {
    A2ACard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (node.category == "VEHICLE") A2ATertiaryContainer else A2APrimaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (node.category == "VEHICLE") Icons.Default.DirectionsCar else Icons.Default.BatteryChargingFull,
                        contentDescription = node.title,
                        tint = if (node.category == "VEHICLE") A2ATertiary else A2APrimary
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = A2ATextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        StatusPillBadge(
                            text = if (node.isOnline) "ออนไลน์" else "ออฟไลน์",
                            color = A2APrimary,
                            backgroundColor = A2APrimaryContainer
                        )
                    }
                    Text(
                        text = node.hardwareModel,
                        fontSize = 11.sp,
                        color = A2ATextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SOC ${node.socOrBatteryPct}% • ${String.format("%.2f", node.voltage)} V • ${node.temperatureC}°C",
                        fontFamily = TelemetryMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = A2ATextPrimary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "ลบอุปกรณ์",
                    tint = A2ATextMuted
                )
            }
        }
    }
}

@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("ESP32 + A7670E Node #2") }
    var chip by remember { mutableStateOf("ESP32-S3 + SIM A7670E + CAN MCP2515") }
    var endpoint by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("VEHICLE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("เพิ่มอุปกรณ์ A2A IoT", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ชื่ออุปกรณ์") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chip,
                    onValueChange = { chip = it },
                    label = { Text("ฮาร์ดแวร์ / บอร์ด") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { category = "VEHICLE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (category == "VEHICLE") A2APrimaryContainer else Color(0xFFE2E8F0)
                        )
                    ) { Text("กล่องรถ OBD2", fontSize = 12.sp) }
                    FilledTonalButton(
                        onClick = { category = "POWER" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (category == "POWER") A2APrimaryContainer else Color(0xFFE2E8F0)
                        )
                    ) { Text("BMS / Power", fontSize = 12.sp) }
                }
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("ESP32 JSON URL (เว้นว่างเพื่อจำลอง)") },
                    placeholder = { Text("http://192.168.4.1/telemetry") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (category == "VEHICLE") {
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("ทะเบียนรถ (ถ้ามี)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), chip.trim(), category, endpoint.trim(), plate.trim())
                    }
                }
            ) { Text("บันทึกอุปกรณ์") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ยกเลิก") }
        }
    )
}

@Composable
private fun Esp32EndpointDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("เชื่อมต่อ ESP32 / ThingsBoard", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "กรอก URL ของบอร์ด ESP32 + SIM A7670E ที่ส่งค่า JSON (rpm, lat, lng, volt, throttle, map, maf) หรือเว้นว่างเพื่อใช้โหมดจำลอง",
                    fontSize = 12.sp,
                    color = A2ATextSecondary
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Telemetry Endpoint URL") },
                    placeholder = { Text("http://192.168.4.1/api/telemetry") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(url) }) { Text("บันทึก") }
        },
        dismissButton = {
            TextButton(onClick = { onSave("") }) { Text("ใช้โหมดจำลอง") }
        }
    )
}
