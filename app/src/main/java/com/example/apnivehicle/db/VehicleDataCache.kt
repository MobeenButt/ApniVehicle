package com.example.apnivehicle.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ===== Entities =====

@Entity(tableName = "cached_makes")
data class CachedMake(
    @PrimaryKey val makeId: String,
    val makeDisplay: String,
    val makeCountry: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_models")
data class CachedModel(
    @PrimaryKey val id: String,   // makeId_modelName
    val makeId: String,
    val modelName: String,
    val cachedAt: Long = System.currentTimeMillis()
)

// ===== DAOs =====

@Dao
interface MakeDao {
    @Query("SELECT * FROM cached_makes ORDER BY makeDisplay ASC")
    suspend fun getAllMakes(): List<CachedMake>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(makes: List<CachedMake>)

    @Query("SELECT MAX(cachedAt) FROM cached_makes")
    suspend fun getLastCachedAt(): Long?

    @Query("DELETE FROM cached_makes")
    suspend fun clearAll()
}

@Dao
interface ModelDao {
    @Query("SELECT * FROM cached_models WHERE makeId = :makeId ORDER BY modelName ASC")
    suspend fun getModelsForMake(makeId: String): List<CachedModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<CachedModel>)

    @Query("SELECT MAX(cachedAt) FROM cached_models WHERE makeId = :makeId")
    suspend fun getLastCachedAt(makeId: String): Long?

    @Query("DELETE FROM cached_models WHERE makeId = :makeId")
    suspend fun clearForMake(makeId: String)
}

// ===== Database =====

@Database(entities = [CachedMake::class, CachedModel::class], version = 1, exportSchema = false)
abstract class VehicleDataCache : RoomDatabase() {
    abstract fun makeDao(): MakeDao
    abstract fun modelDao(): ModelDao

    companion object {
        @Volatile private var INSTANCE: VehicleDataCache? = null

        fun getInstance(context: Context): VehicleDataCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VehicleDataCache::class.java,
                    "vehicle_data_cache"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
