package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Esp32TelemetryResponse(
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lng") val lng: Double? = null,
    @Json(name = "heading") val heading: Int? = null,
    @Json(name = "alt") val altitude: Double? = null,
    @Json(name = "sat") val satellites: Int? = null,
    @Json(name = "speed_gps") val speedGps: Double? = null,
    @Json(name = "speed_obd") val speedObd: Int? = null,
    @Json(name = "rpm") val rpm: Int? = null,
    @Json(name = "load") val engineLoad: Double? = null,
    @Json(name = "fuel") val fuelLevel: Double? = null,
    @Json(name = "coolant") val coolantTemp: Int? = null,
    @Json(name = "volt") val voltage: Double? = null,
    @Json(name = "throttle") val throttle: Double? = null,
    @Json(name = "map") val mapKpa: Int? = null,
    @Json(name = "maf") val mafGps: Double? = null,
    @Json(name = "timing") val timingAdv: Double? = null,
    @Json(name = "stft") val stft: Double? = null,
    @Json(name = "dtc") val dtcCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class EcuFlashPayload(
    @Json(name = "profile_name") val profileName: String,
    @Json(name = "author") val author: String,
    @Json(name = "fuel_trim_pct") val fuelTrimPct: Float,
    @Json(name = "ignition_offset_deg") val ignitionOffsetDeg: Float,
    @Json(name = "throttle_response_pct") val throttleResponsePct: Float,
    @Json(name = "rev_limiter_rpm") val revLimiterRpm: Int,
    @Json(name = "vtec_rpm") val vtecEngageRpm: Int,
    @Json(name = "fan_temp_c") val fanTriggerTempC: Int,
    @Json(name = "launch_control_rpm") val launchControlRpm: Int,
    @Json(name = "pop_and_bang") val popAndBangEnabled: Boolean
)

interface Esp32Api {
    @GET
    suspend fun fetchTelemetry(@Url fullUrl: String): Response<Esp32TelemetryResponse>

    @POST
    suspend fun sendEcuTune(
        @Url fullUrl: String,
        @Body payload: EcuFlashPayload
    ): Response<Unit>
}

object Esp32TelemetryClient {
    private val okHttpClient: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
    }

    val api: Esp32Api by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Esp32Api::class.java)
    }
}
