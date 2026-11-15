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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.data.model.VehicleModel3D
import com.example.ui.components.A2ACard
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.A2ABackground
import com.example.ui.theme.A2ABorder
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASecondary
import com.example.ui.theme.A2ASecondaryContainer
import com.example.ui.theme.A2ATextMuted
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.A2AWarning
import com.example.ui.theme.A2AWarningContainer
import com.example.ui.theme.TelemetryMonoFontFamily
import com.example.ui.viewmodel.A2AUiState

@Composable
fun TripHistoryScreen(
    state: A2AUiState,
    onSelectTripDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicle = state.vehicle
    val model = VehicleModel3D.fromId(vehicle?.model3dId ?: "")
    val trips = state.tripRecords
    val selectedTrip = trips.firstOrNull { it.dayNumber == state.selectedTripDay } ?: trips.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(A2ABackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header matching 01:56 of video
        item {
            Column {
                Text(
                    text = "ประวัติการเดินทางย้อนหลัง",
                    style = MaterialTheme.typography.headlineMedium,
                    color = A2ATextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = model.imageRes),
                        contentDescription = model.displayName,
                        modifier = Modifier
                            .size(width = 38.dp, height = 24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "${vehicle?.name ?: "Honda Civic FB 1.8"} • ทะเบียน ${vehicle?.licensePlate ?: "1กฮ3069"}",
                        fontSize = 12.sp,
                        color = A2ATextSecondary
                    )
                }
                Text(
                    text = "ดึงข้อมูลย้อนหลังจาก ESP32 + SIM A7670E (เก็บในเครื่อง ${trips.size} วัน)",
                    fontSize = 11.sp,
                    color = A2APrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Date Selector Strip
        item {
            A2ACard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("วันที่แสดงผลทั้งหมด", fontSize = 11.sp, color = A2ATextMuted)
                            Text(
                                text = selectedTrip?.fullDateThai ?: "วันอาทิตย์ที่ 15 พฤศจิกายน 2569",
                                style = MaterialTheme.typography.titleMedium,
                                color = A2ATextPrimary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = A2APrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = A2APrimary, modifier = Modifier.size(14.dp))
                                Text("ปฏิทิน", color = A2APrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(trips, key = { it.id }) { trip ->
                            val isSelected = trip.dayNumber == state.selectedTripDay
                            Surface(
                                modifier = Modifier
                                    .width(68.dp)
                                    .testTag("trip_day_${trip.dayNumber}")
                                    .clickable { onSelectTripDay(trip.dayNumber) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) A2APrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSelected) A2APrimary else A2ABorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = trip.dayLabel.substringBefore(" "),
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else A2ATextSecondary
                                    )
                                    Text(
                                        text = trip.dayNumber.toString(),
                                        fontFamily = TelemetryMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isSelected) Color.White else A2ATextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Stats 2x2 Grid
        if (selectedTrip != null) {
            item {
                Text("สรุปสถิติการเดินทางในวัน", style = MaterialTheme.typography.titleMedium, color = A2ATextPrimary)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryStatTile(
                            icon = Icons.Default.Route,
                            iconTint = A2APrimary,
                            iconBg = A2APrimaryContainer,
                            title = "ระยะทางรวม",
                            value = "${selectedTrip.distanceKm} km",
                            subtitle = "GPS Track Log",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryStatTile(
                            icon = Icons.Default.AccessTime,
                            iconTint = A2ASecondary,
                            iconBg = A2ASecondaryContainer,
                            title = "เวลาขับขี่",
                            value = "${selectedTrip.durationMinutes} นาที",
                            subtitle = "${selectedTrip.startTime} - ${selectedTrip.endTime}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryStatTile(
                            icon = Icons.Default.Speed,
                            iconTint = Color(0xFFEF4444),
                            iconBg = Color(0xFFFEE2E2),
                            title = "ความเร็วสูงสุด",
                            value = "${selectedTrip.maxSpeedKmh} km/h",
                            subtitle = "OBD2 Peak Speed",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryStatTile(
                            icon = Icons.Default.Speed,
                            iconTint = A2AWarning,
                            iconBg = A2AWarningContainer,
                            title = "ความเร็วเฉลี่ย",
                            value = "${selectedTrip.avgSpeedKmh} km/h",
                            subtitle = "Smooth Cruise",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Route Summary & Timeline Card
            item {
                A2ACard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ภาพรวมเส้นทาง (จากจุดเริ่มต้นถึงปลายทาง)",
                            style = MaterialTheme.typography.titleMedium,
                            color = A2ATextPrimary
                        )

                        // Vector Route Graphic
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            val w = size.width
                            val h = size.height
                            val path = Path().apply {
                                moveTo(w * 0.1f, h * 0.65f)
                                cubicTo(w * 0.35f, h * 0.15f, w * 0.62f, h * 0.85f, w * 0.9f, h * 0.35f)
                            }
                            drawPath(path, color = A2APrimary, style = Stroke(width = 6f, cap = StrokeCap.Round))
                            drawCircle(Color(0xFF34D399), radius = 10f, center = Offset(w * 0.1f, h * 0.65f))
                            drawCircle(Color(0xFF38BDF8), radius = 10f, center = Offset(w * 0.9f, h * 0.35f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(A2APrimary))
                                Text("จุดเริ่มต้น: ${selectedTrip.startLocation}", fontSize = 12.sp, color = A2ATextPrimary)
                            }
                            Text(selectedTrip.startTime, fontFamily = TelemetryMonoFontFamily, fontSize = 12.sp, color = A2ATextSecondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(A2ASecondary))
                                Text("ปลายทาง: ${selectedTrip.endLocation}", fontSize = 12.sp, color = A2ATextPrimary)
                            }
                            Text(selectedTrip.endTime, fontFamily = TelemetryMonoFontFamily, fontSize = 12.sp, color = A2ATextSecondary)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, A2ABorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = A2APrimary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("รายการผ่านการเดินทาง (Timeline)", fontSize = 11.sp, color = A2ATextMuted)
                                    Text(selectedTrip.segmentLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = A2ATextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }
    }
}
