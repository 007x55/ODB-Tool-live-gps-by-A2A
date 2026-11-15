package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LiveTelemetry
import com.example.data.model.ObdPidMetric
import com.example.data.model.VehicleModel3D
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app_name is A2A`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("A2A", appName)
    }

    @Test
    fun `telemetry formats OBD2 PID metrics accurately`() {
        val telemetry = LiveTelemetry(rpm = 1885, throttlePct = 16.5, mapKpa = 50, ecuVoltage = 14.4)
        assertEquals("1885 rpm", telemetry.formatPidValue(ObdPidMetric.RPM))
        assertEquals("16.5 %", telemetry.formatPidValue(ObdPidMetric.THROTTLE))
        assertEquals("50 kPa", telemetry.formatPidValue(ObdPidMetric.MAP_KPA))
        assertEquals("14.40 V", telemetry.formatPidValue(ObdPidMetric.VOLTAGE))
    }

    @Test
    fun `vehicle model 3D resolves from id`() {
        assertEquals(VehicleModel3D.PORSCHE_992_GT3, VehicleModel3D.fromId("porsche_992_gt3"))
        assertEquals(VehicleModel3D.CIVIC_FB, VehicleModel3D.fromId("unknown"))
    }
}
