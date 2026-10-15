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
interface CameraBodyDao {
    @Query("SELECT * FROM camera_bodies ORDER BY brand ASC, modelName ASC")
    fun getAllCameraBodies(): Flow<List<CameraBodyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameraBody(camera: CameraBodyEntity): Long

    @Update
    suspend fun updateCameraBody(camera: CameraBodyEntity)

    @Delete
    suspend fun deleteCameraBody(camera: CameraBodyEntity)

    @Query("UPDATE camera_bodies SET inBag = :inBag WHERE id = :id")
    suspend fun toggleInBag(id: Long, inBag: Boolean)

    @Query("SELECT COUNT(*) FROM camera_bodies")
    suspend fun getCount(): Int
}

@Dao
interface LensDao {
    @Query("SELECT * FROM lenses ORDER BY brand ASC, lensName ASC")
    fun getAllLenses(): Flow<List<LensEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLens(lens: LensEntity): Long

    @Update
    suspend fun updateLens(lens: LensEntity)

    @Delete
    suspend fun deleteLens(lens: LensEntity)

    @Query("UPDATE lenses SET inBag = :inBag WHERE id = :id")
    suspend fun toggleInBag(id: Long, inBag: Boolean)

    @Query("SELECT COUNT(*) FROM lenses")
    suspend fun getCount(): Int
}

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
    entities = [
        CameraBodyEntity::class,
        LensEntity::class,
        GearEntity::class,
        PresetEntity::class,
        ExposureLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cameraBodyDao(): CameraBodyDao
    abstract fun lensDao(): LensDao
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
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
