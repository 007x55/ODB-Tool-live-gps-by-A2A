package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface A2ADao {
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    fun observeVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM ecu_tune_profiles WHERE vehicleId = :vehicleId ORDER BY id ASC")
    fun observeTuneProfiles(vehicleId: Int): Flow<List<EcuTuneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuneProfile(profile: EcuTuneEntity): Long

    @Update
    suspend fun updateTuneProfile(profile: EcuTuneEntity)

    @Query("UPDATE ecu_tune_profiles SET isActive = (id = :activeId) WHERE vehicleId = :vehicleId")
    suspend fun setActiveTuneProfile(vehicleId: Int, activeId: Int)

    @Query("SELECT * FROM trip_records WHERE vehicleId = :vehicleId ORDER BY dayNumber DESC")
    fun observeTripRecords(vehicleId: Int): Flow<List<TripRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripRecords(records: List<TripRecordEntity>)

    @Query("SELECT * FROM iot_nodes ORDER BY id ASC")
    fun observeIoTNodes(): Flow<List<IoTNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIoTNode(node: IoTNodeEntity): Long

    @Query("DELETE FROM iot_nodes WHERE id = :nodeId")
    suspend fun deleteIoTNode(nodeId: Int)
}
