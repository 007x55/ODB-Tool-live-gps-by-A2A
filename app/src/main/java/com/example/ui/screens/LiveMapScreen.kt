package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.A2ATab
import com.example.data.model.ObdPidMetric
import com.example.data.model.VehicleModel3D
import com.example.ui.components.CircularHudGauge
import com.example.ui.components.PillHudGauge
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.A2ABorder
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASecondary
import com.example.ui.theme.A2ASecondaryContainer
import com.example.ui.theme.A2ASurface
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.HudDarkBackground
import com.example.ui.theme.HudNeonAmber
import com.example.ui.theme.HudNeonGreen
import com.example.ui.theme.TelemetryMonoFontFamily
import com.example.ui.viewmodel.A2AUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMapScreen(
    state: A2AUiState,
    onSelectHudMetric: (slotIndex: Int, metric: ObdPidMetric) -> Unit,
    onToggleDrivingSim: (Boolean) -> Unit,
    onNavigateTab: (A2ATab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicle = state.vehicle
    val telemetry = state.telemetry
    val model = VehicleModel3D.fromId(vehicle?.model3dId ?: "")

    val slot1Metric = ObdPidMetric.fromKey(vehicle?.hudSlot1Key ?: ObdPidMetric.RPM.key)
    val slot2Metric = ObdPidMetric.fromKey(vehicle?.hudSlot2Key ?: ObdPidMetric.FUEL_LEVEL.key)
    val slot3Metric = ObdPidMetric.fromKey(vehicle?.hudSlot3Key ?: ObdPidMetric.THROTTLE.key)

    var editingHudSlot by remember { mutableIntStateOf(0) }
    var zoomLevel by remember { mutableIntStateOf(16) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val centerTile = remember(telemetry.latitude, telemetry.longitude, zoomLevel) {
        latLngToTile(telemetry.latitude, telemetry.longitude, zoomLevel)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A261D))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffsetX = (panOffsetX + dragAmount.x).coerceIn(-240f, 240f)
                    panOffsetY = (panOffsetY + dragAmount.y).coerceIn(-240f, 240f)
                }
            }
    ) {
        // Offline-resilient satellite terrain backdrop + Esri World Imagery center tile
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2B3D2C), Color(0xFF1D2920), Color(0xFF131B15)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension * 0.7f
                )
            )
            // Subtle satellite parcel grid lines
            val step = 72f
            var x = (panOffsetX % step)
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.05f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = (panOffsetY % step)
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        // Real Esri World Imagery satellite tile layer (3x3 grid around vehicle)
        val tileUrl = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$zoomLevel/${centerTile.second}/${centerTile.first}"
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(tileUrl)
                .crossfade(true)
                .build(),
            contentDescription = "แผนที่ดาวเทียมตำแหน่งรถ",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )

        // Route Polyline + Vehicle Marker Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f + panOffsetX
            val cy = size.height * 0.44f + panOffsetY
            val scaleFactor = 2.0.pow((zoomLevel - 15).toDouble()).toFloat() * 140000f

            // Draw route trail
            val trail = telemetry.routeTrail
            if (trail.size >= 2) {
                val path = Path()
                trail.forEachIndexed { index, (lat, lng) ->
                    val dx = ((lng - telemetry.longitude) * scaleFactor).toFloat()
                    val dy = ((telemetry.latitude - lat) * scaleFactor).toFloat()
                    val px = cx + dx
                    val py = cy + dy
                    if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF10B981).copy(alpha = 0.38f),
                    style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF34D399),
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // GPS Radar accuracy ring
            drawCircle(
                color = A2APrimary.copy(alpha = 0.18f),
                radius = 58f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = A2APrimary.copy(alpha = 0.55f),
                radius = 58f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            )

            // Directional vehicle heading indicator
            rotate(degrees = telemetry.headingDeg.toFloat(), pivot = Offset(cx, cy)) {
                val carRectW = 30f
                val carRectH = 52f
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(cx - carRectW / 2f - 3f, cy - carRectH / 2f - 3f),
                    size = androidx.compose.ui.geometry.Size(carRectW + 6f, carRectH + 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - carRectW / 2f, cy - carRectH / 2f),
                    size = androidx.compose.ui.geometry.Size(carRectW, carRectH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                // Windshield accent
                drawRoundRect(
                    color = A2ASecondary,
                    topLeft = Offset(cx - carRectW * 0.36f, cy - carRectH * 0.25f),
                    size = androidx.compose.ui.geometry.Size(carRectW * 0.72f, carRectH * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }

        // Top-Left Customizable HUD Telemetry Slots (exact video layout 02:17 - 02:29)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularHudGauge(
                    metric = slot1Metric,
                    telemetry = telemetry,
                    accentColor = HudNeonGreen,
                    slotTag = "hud_slot_1",
                    onClick = { editingHudSlot = 1 }
                )
                CircularHudGauge(
                    metric = slot2Metric,
                    telemetry = telemetry,
                    accentColor = HudNeonAmber,
                    slotTag = "hud_slot_2",
                    onClick = { editingHudSlot = 2 }
                )
            }
            PillHudGauge(
                metric = slot3Metric,
                telemetry = telemetry,
                slotTag = "hud_slot_3",
                onClick = { editingHudSlot = 3 }
            )
            Surface(
                shape = CircleShape,
                color = HudDarkBackground.copy(alpha = 0.75f)
            ) {
                Text(
                    text = "แตะเกจเพื่อเลือกค่าแสดงผล",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Right-Side Floating Map Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 14.dp, top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            FilledIconButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = HudDarkBackground.copy(alpha = 0.88f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "จัดกึ่งกลางรถ")
            }
            FilledIconButton(
                onClick = { zoomLevel = (zoomLevel + 1).coerceAtMost(18) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = HudDarkBackground.copy(alpha = 0.88f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "ซูมเข้า")
            }
            FilledIconButton(
                onClick = { zoomLevel = (zoomLevel - 1).coerceAtLeast(12) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = HudDarkBackground.copy(alpha = 0.88f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "ซูมออก")
            }
            if (vehicle?.esp32EndpointUrl.isNullOrBlank()) {
                FilledIconButton(
                    onClick = { onToggleDrivingSim(!state.isDrivingSimulation) },
                    modifier = Modifier.testTag("map_sim_drive_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (state.isDrivingSimulation) A2APrimary else HudDarkBackground.copy(alpha = 0.88f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (state.isDrivingSimulation) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "จำลองขับขี่"
                    )
                }
            }
        }

        // Bottom Floating Vehicle Summary Sheet (matching 02:18 of video)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            shape = RoundedCornerShape(22.dp),
            color = A2ASurface.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, A2ABorder),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = model.imageRes),
                        contentDescription = model.displayName,
                        modifier = Modifier
                            .size(width = 66.dp, height = 44.dp)
                            .clip(RoundedCornerShape(10.dp)),
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
                                text = if (telemetry.gpsSpeedKmh > 1.0) "กำลังวิ่ง" else "จอดนิ่ง",
                                color = if (telemetry.gpsSpeedKmh > 1.0) A2ASecondary else A2APrimary,
                                backgroundColor = if (telemetry.gpsSpeedKmh > 1.0) A2ASecondaryContainer else A2APrimaryContainer
                            )
                        }
                        Text(
                            text = "${telemetry.locationName} • ทะเบียน ${vehicle?.licensePlate ?: "1กฮ3069"}",
                            fontSize = 11.sp,
                            color = A2ATextSecondary
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .clickable { onNavigateTab(A2ATab.OBD_ECU) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "OBD2", tint = A2APrimary, modifier = Modifier.size(14.dp))
                            Text("OBD2", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MapBottomMetric(
                        label = "ความเร็ว GPS",
                        value = "${String.format("%.1f", telemetry.gpsSpeedKmh)} km/h"
                    )
                    MapBottomMetric(
                        label = "ระยะทางวันนี้",
                        value = "${String.format("%.1f", telemetry.distanceTodayKm)} km"
                    )
                    MapBottomMetric(
                        label = "สัญญาณดาวเทียม",
                        value = "GPS ${telemetry.gpsSatellites} ดาว",
                        icon = Icons.Default.SatelliteAlt
                    )
                    MapBottomMetric(
                        label = "RPM",
                        value = "${telemetry.rpm}",
                        icon = Icons.Default.DirectionsCar
                    )
                }
            }
        }
    }

    // Bottom Sheet: เลือกค่าแสดงผล (ช่องที่ 1/2/3) — exact match to 02:22-02:26
    if (editingHudSlot in 1..3) {
        val currentKey = when (editingHudSlot) {
            1 -> slot1Metric.key
            2 -> slot2Metric.key
            else -> slot3Metric.key
        }
        ModalBottomSheet(
            onDismissRequest = { editingHudSlot = 0 },
            containerColor = A2ASurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "เลือกค่าแสดงผล (ช่องที่ $editingHudSlot)",
                    style = MaterialTheme.typography.titleLarge,
                    color = A2ATextPrimary
                )
                Text(
                    text = "เลือกพารามิเตอร์จาก OBD2 / Telemetry ที่ต้องการแสดงผลบนหน้าจอแผนที่",
                    fontSize = 12.sp,
                    color = A2ATextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.height(420.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ObdPidMetric.entries) { metric ->
                        val selected = metric.key == currentKey
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hud_metric_${metric.key}")
                                .clickable {
                                    onSelectHudMetric(editingHudSlot, metric)
                                    editingHudSlot = 0
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) A2APrimaryContainer.copy(alpha = 0.45f) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) A2APrimary else A2ABorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = metric.thaiTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = A2ATextPrimary
                                    )
                                    Text(
                                        text = "Key: ${metric.key} • ค่าล่าสุด: ${telemetry.formatPidValue(metric)}",
                                        fontFamily = TelemetryMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = A2ATextSecondary
                                    )
                                }
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(A2APrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapBottomMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column {
        Text(text = label, fontSize = 10.sp, color = A2ATextSecondary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = A2APrimary, modifier = Modifier.size(12.dp))
            }
            Text(
                text = value,
                fontFamily = TelemetryMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = A2ATextPrimary
            )
        }
    }
}

private fun latLngToTile(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
    val n = 2.0.pow(zoom.toDouble())
    val x = floor((lon + 180.0) / 360.0 * n).toInt()
    val latRad = lat * PI / 180.0
    val y = floor((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt()
    return x to y
}
