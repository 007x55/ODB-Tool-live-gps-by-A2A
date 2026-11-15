package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VehicleEntity::class,
        EcuTuneEntity::class,
        TripRecordEntity::class,
        IoTNodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class A2ADatabase : RoomDatabase() {
    abstract fun a2aDao(): A2ADao

    companion object {
        @Volatile
        private var INSTANCE: A2ADatabase? = null

        fun getInstance(context: Context): A2ADatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    A2ADatabase::class.java,
                    "a2a_telemetry.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
