package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveTelemetry
import com.example.data.model.ObdPidMetric
import com.example.ui.theme.A2ABorder
import com.example.ui.theme.A2APrimary
import com.example.ui.theme.A2APrimaryContainer
import com.example.ui.theme.A2ASurface
import com.example.ui.theme.A2ATextPrimary
import com.example.ui.theme.A2ATextSecondary
import com.example.ui.theme.HudDarkBackground
import com.example.ui.theme.HudDarkBorder
import com.example.ui.theme.HudNeonAmber
import com.example.ui.theme.HudNeonCyan
import com.example.ui.theme.HudNeonGreen
import com.example.ui.theme.TelemetryMonoFontFamily

@Composable
fun A2ACard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = A2ASurface,
    borderColor: Color = A2ABorder,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
fun StatusPillBadge(
    text: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    dotColor: Color? = color
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CircularHudGauge(
    metric: ObdPidMetric,
    telemetry: LiveTelemetry,
    accentColor: Color,
    slotTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = telemetry.progressFraction(metric),
        animationSpec = tween(durationMillis = 350),
        label = "hud_gauge_progress"
    )
    Surface(
        modifier = modifier
            .size(76.dp)
            .minimumInteractiveComponentSize()
            .testTag(slotTag)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = HudDarkBackground.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, HudDarkBorder),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(66.dp)
                    .padding(3.dp)
            ) {
                drawArc(
                    color = Color.White.copy(alpha = 0.14f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = 270f * progress,
                    useCenter = false,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = telemetry.formatCompactValue(metric),
                    color = Color.White,
                    fontFamily = TelemetryMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = metric.shortLabel,
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PillHudGauge(
    metric: ObdPidMetric,
    telemetry: LiveTelemetry,
    slotTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = telemetry.progressFraction(metric),
        animationSpec = tween(durationMillis = 350),
        label = "pill_hud_progress"
    )
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(slotTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = HudDarkBackground.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, HudDarkBorder),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .width(142.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.shortLabel,
                    color = HudNeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = telemetry.formatCompactValue(metric),
                    color = Color.White,
                    fontFamily = TelemetryMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (progress > 0.78f) HudNeonAmber else HudNeonGreen)
                )
            }
        }
    }
}

@Composable
fun TelemetryStatTile(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    A2ACard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = A2ATextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontFamily = TelemetryMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = A2ATextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = A2APrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
