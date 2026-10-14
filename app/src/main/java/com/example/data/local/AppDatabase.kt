package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GearDao {
    @Query("SELECT * FROM gear_inventory ORDER BY category ASC, brand ASC, name ASC")
    fun getAllGear(): Flow<List<GearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGear(gear: GearEntity): Long

    @Update
    suspend fun updateGear(gear: GearEntity)

    @Delete
    suspend fun deleteGear(gear: GearEntity)

    @Query("UPDATE gear_inventory SET inBag = :inBag WHERE id = :id")
    suspend fun toggleInBag(id: Long, inBag: Boolean)
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM shooting_presets ORDER BY isCustom DESC, name ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    @Update
    suspend fun updatePreset(preset: PresetEntity)

    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    @Query("SELECT COUNT(*) FROM shooting_presets")
    suspend fun getCount(): Int
}

@Dao
interface ExposureLogDao {
    @Query("SELECT * FROM exposure_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ExposureLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExposureLogEntity): Long

    @Delete
    suspend fun deleteLog(log: ExposureLogEntity)

    @Query("UPDATE exposure_logs SET isSyncedToCloud = 1")
    suspend fun markAllSynced()
}

@Database(
    entities = [GearEntity::class, PresetEntity::class, ExposureLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gearDao(): GearDao
    abstract fun presetDao(): PresetDao
    abstract fun exposureLogDao(): ExposureLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lightmeter_pro.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
